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
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StuStatRepository;
import com.university.home.repository.StuSubDetailRepository;
import com.university.home.repository.StudentRepository;

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
                아래의 [학생 데이터]를 기반으로, [분석 기준]에 맞춰 위험도를 0~100점으로 예측하세요.
                
                [분석 기준]
                1. 성적(학점): 4.5 만점 기준입니다. 
                   - 2.0 미만은 '위험', 1.5 미만은 '매우 위험'으로 간주하세요.
                   - 성적이 낮을수록 학업 흥미를 잃었을 가능성이 큽니다.
                2. 출결(결석): 
                   - 결석이 0회에 가까우면 성실한 학생입니다.
                   - 과목당 결석이 누적되어 총 결석이 많아질수록 학교 생활 부적응 확률이 매우 높습니다.
                3. 종합 판단: 성적과 출결이 모두 나쁘면 90점 이상을 부여하세요.

                [학생 데이터]
                - 이름: %s
                - 이번 학기 평균 학점: %.2f / 4.5
                - 총 누적 결석 횟수: %d회
                - 현재 학적 상태: %s
                
                [요청사항]
                1. 첫 번째 줄에는 위험도 점수(0~100 사이 정수)만 적으세요.
                2. 두 번째 줄에는 판단의 근거를 '성적'과 '출결' 수치를 언급하며 한 줄로 요약하세요.
                
                (출력 예시: 
                88
                평점 1.8점으로 학사 경고 위험이 있고, 결석이 15회로 잦아 이탈 위험이 매우 높음)
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
        if (score >= 90) return "심각";
        if (score >= 70) return "경고";
        if (score >= 50) return "주의";
        return "정상";
    }

    private void sendAlert(Student student, String level, String reason) {
        try {
            String content = String.format("💬 [상담 권장] %s님, 학업에 어려움은 없으신가요? 상담 센터가 열려있습니다.", student.getName());
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
                notificationService.send(prof.getId(), content, "/professor/dashboard");
            }
        }
    }
}