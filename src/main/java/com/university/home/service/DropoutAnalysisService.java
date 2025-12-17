package com.university.home.service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional; // Service단 트랜잭션 필요시 사용

import com.university.home.entity.DropoutRisk;
import com.university.home.entity.Professor;
import com.university.home.entity.StuStat;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.entity.User;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StuStatRepository;
import com.university.home.repository.StuSubDetailRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropoutAnalysisService {

    private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final DropoutRiskRepository dropoutRiskRepository;
    private final ProfessorRepository professorRepository;
    private final GradeService gradeService; 
    
    // 알림 서비스
    private final NotificationService notificationService;
    
    private final StuSubDetailRepository stuSubDetailRepository;
    private final StuStatRepository stuStatRepository;
    private final UserRepository userRepository;
   
    public void analyzeAllStudents() {
        List<Student> students = studentRepository.findAll();
        log.info("총 {}명의 학생에 대한 위험 분석을 시작합니다.", students.size());

        for (Student student : students) {
            try {
                analyzeStudentRisk(student);
                // API 속도 제한 고려 (1초 대기)
                Thread.sleep(1000); 

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("학생({}) 건너뜀: {}", student.getName(), e.getMessage());
            }
        }
     // ★ [추가] 모든 분석이 끝난 후 직원들에게 '실행 완료' 알림 발송
        sendAnalysisCompletionAlertToStaff();
    }
 // ★ [신규 메서드] 직원 알림 전송 로직
    private void sendAnalysisCompletionAlertToStaff() {
        try {
            // 1. 'STAFF' 권한을 가진 모든 사용자 조회
            // (DB의 user_tb 테이블 role 컬럼값이 'STAFF'인 사용자를 찾습니다)
            // UserRepository에 List<User> findByRole(String role); 메서드가 있어야 합니다.
        	List<User> staffList = userRepository.findByUserRole("STAFF");
            
            String message = "✅ 전체 학생 위험군 분석이 완료되었습니다.";
            String targetUrl = "/admin/dashboard"; // 직원이 이동할 대시보드 URL

            for (User staff : staffList) {
                notificationService.send(staff.getId(), message, targetUrl);
            }
            log.info("직원 {}명에게 분석 완료 알림 전송 완료", staffList.size());
            
        } catch (Exception e) {
            log.error("직원 알림 전송 중 오류 발생", e);
        }
    }
    	private void analyzeStudentRisk(Student student) {
        
        // 1. 데이터 조회 (기존 로직 유지)
        Double avgGrade = gradeService.calculateCurrentSemesterAverageGrade(student.getId());

        List<StuSubDetail> details = stuSubDetailRepository.findByStudent_Id(student.getId());
        int absenceCount = details.stream()
                .mapToInt(detail -> detail.getAbsent() == null ? 0 : detail.getAbsent().intValue()) 
                .sum();
     
        List<StuStat> statHistory = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId());
        String status = statHistory.isEmpty() ? "재학" : statHistory.get(0).getStatus();
        
        // ★ [수정] 프롬프트 고도화: 성적, 학점, 출결을 구체적인 판단 기준으로 제시
        String analysisPrompt = """
                당신은 대학교의 '중도 이탈(자퇴) 위험 분석 AI'입니다.
                아래의 [학생 데이터]를 기반으로 위험도를 예측하세요.

                [학생 데이터]
                - 이름: %s
                - 이번 학기 평균 학점: %.2f / 4.5
                - 총 누적 결석 횟수: %d회
                - 현재 학적 상태: %s
                
                [⚠️ 절대 평가 규칙 (최우선 적용)]
                1. 학점이 1.0 미만인 경우: 다른 요소(출결 등)가 좋더라도 **무조건 95점 이상**을 부여하세요. (즉시 이탈 위험)
                2. 학점이 2.0 미만인 경우: **무조건 90점 이상**을 부여하세요.
                3. 결석이 5회 이상인 경우: 학점이 높아도 **80점 이상**을 부여하세요.

                [분석 기준]
                - 95점 이상 (심각): 당장 자퇴할 확률이 매우 높음 (학사 경고 등)
                - 70~89점 (경고): 학업에 흥미를 잃어가는 단계
                - 50~69점 (주의): 성적 하락세이거나 결석이 생기기 시작함
                - 50점 미만 (정상): 안정적인 학교 생활 중

                [요청사항]
                1. 첫 번째 줄: 위험도 점수(0~100) 숫자만 작성
                2. 두 번째 줄: "평점 X.X점(F등급 수준)으로 인한 자동 위험 분류" 와 같이 핵심 원인을 한 줄로 요약

                """.formatted(student.getName(), avgGrade, absenceCount, status);

        try {
            // Gemini 호출 및 결과 처리 로직 (기존과 동일)
            String result = geminiService.talk(analysisPrompt);

            if (result.contains("429") || result.contains("error") || result.contains("연결 실패")) {
                log.warn("API 한도 초과/에러 (학생: {}). 분석 중단.", student.getName());
                return; 
            }

            String[] lines = result.split("\n");
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
            Matcher matcher = pattern.matcher(lines[0]);

            Double riskScore = 0.0;
            if (matcher.find()) {
                riskScore = Double.parseDouble(matcher.group(1));
            } else {
                log.warn("점수 파싱 실패. 원본: {}", lines[0]);
                return; 
            }
            
            String reason = lines.length > 1 ? lines[1].trim() : "상세 분석 내용 없음";
            String riskLevel = determineLevel(riskScore);            

            // DB 저장
            DropoutRisk risk = DropoutRisk.builder()
                    .student(student)
                    .riskScore(riskScore)
                    .riskLevel(riskLevel)
                    .reason(reason)
                    .analyzedDate(LocalDate.now())
                    .build();
            
            dropoutRiskRepository.save(risk);
            log.info("분석 완료: {} ({}점/{})", student.getName(), riskScore, riskLevel);

            // 알림 발송
            if ("심각".equals(riskLevel)) {
                sendAlert(student, riskLevel, reason);
            }

        } catch (Exception e) {
            log.error("학생({}) 분석 로직 에러: {}", student.getName(), e.getMessage());
        }
    }

    private String determineLevel(Double score) {
        if (score >= 95) return "심각";
        if (score >= 70) return "경고";
        if (score >= 50) return "주의";
        return "정상";
    }

    private void sendAlert(Student student, String level, String reason) {
        try {
            String content = String.format("💬 [상담 권장] %s님, 학업에 어려움은 없으신가요? 챗봇과 대화해보세요.", student.getName());
            notificationService.send(student.getId(), content, "/student/chatbot");
        } catch (Exception e) {
            log.error("학생 알림 전송 실패", e);
        }

        if (student.getDepartment() != null) {
            Long deptId = student.getDepartment().getId();
            List<Professor> professors = professorRepository.findByDepartmentId(deptId);

            for (Professor prof : professors) {
                String content = String.format("🚨[위험 알림] %s 학생(%s) - %s 단계 (사유: %s)", 
                        student.getName(), student.getDepartment().getName(), level, reason);
                notificationService.send(prof.getId(), content, "/course?tab=danger");
            }
            
        }
    }
}