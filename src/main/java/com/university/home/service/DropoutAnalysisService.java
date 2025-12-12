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
        
        // ★ [수정 1] "이번 학기(최신)" 평균 학점 가져오기
        // (GradeService에서 최신 년도/학기를 자동으로 조회해서 계산함)
        Double avgGrade = gradeService.calculateCurrentSemesterAverageGrade(student.getId());

        // 결석 횟수 등 다른 데이터 조회
        List<StuSubDetail> details = stuSubDetailRepository.findByStudent_Id(student.getId());
        int absenceCount = details.stream()
                .mapToInt(detail -> detail.getAbsent() == null ? 0 : detail.getAbsent().intValue()) 
                .sum();
     
        List<StuStat> statHistory = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId());
        String status = statHistory.isEmpty() ? "재학" : statHistory.get(0).getStatus();
        
        // 프롬프트 구성 (최신 학기 성적임을 명시해주면 AI 판단에 더 도움이 됨)
        String analysisPrompt = """
                다음 학생의 데이터를 분석하여 '중도 이탈(자퇴) 위험도'를 예측해주세요.
                
                [학생 데이터]
                - 이름: %s
                - 이번 학기 평균 학점: %.2f / 4.5
                - 최근 결석 횟수: %d회
                - 학적 상태: %s
                
                [요청사항]
                1. 위험도를 0~100 사이의 숫자로만 답하세요. (높을수록 위험)
                2. 위험도(숫자) 뒤에 줄바꿈을 하고 원인을 한 줄로 요약해주세요.
                (형식 예시: 
                85
                최근 학기 성적 부진 및 잦은 결석으로 학업 흥미 상실 의심)
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