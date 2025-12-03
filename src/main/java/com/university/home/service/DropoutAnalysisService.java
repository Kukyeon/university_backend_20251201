package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.university.home.entity.DropoutRisk;
import com.university.home.entity.StuStat;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.StuStatRepository;
import com.university.home.repository.StuSubDetailRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropoutAnalysisService {

	private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final DropoutRiskRepository dropoutRiskRepository;
    
    // [추가 1] 실제 성적 계산을 위해 GradeService 주입
    private final GradeService gradeService; 
    
    // [추가 2] 알림 저장을 위한 Repository (필요 시 주석 해제 후 사용)
    // private final NotificationRepository notificationRepository;
    private final StuSubDetailRepository stuSubDetailRepository;
    private final StuStatRepository stuStatRepository;
    /**
     * 전체 학생에 대해 중도 이탈 위험 분석 실행 (배치 혹은 관리자 버튼용)
     */
    @Transactional
    public void analyzeAllStudents() {
        List<Student> students = studentRepository.findAll();
        log.info("총 {}명의 학생에 대한 위험 분석을 시작합니다.", students.size());

        for (Student student : students) {
            analyzeStudentRisk(student);
        }
    }

    private void analyzeStudentRisk(Student student) {
        // 1. [FUN-001] 데이터 수집 (실제 데이터 연동)
        // GradeService를 통해 학생의 실제 평균 학점을 가져옵니다.
        Double avgGrade = gradeService.calculateAverageGrade(student.getId());
        // 2. '결석 횟수' 자바로 계산하기 (기존 코드)
        List<StuSubDetail> details = stuSubDetailRepository.findByStudentId(student.getId());
        int absenceCount = details.stream()
                .mapToInt(detail -> detail.getAbsent() == null ? 0 : detail.getAbsent().intValue()) 
                .sum();
     // ---------------------------------------------------------
        // [수정] 3. '학적 상태' List에서 꺼내기 (Optional 제거!)
        // ---------------------------------------------------------
        // (1) 해당 학생의 모든 기록을 최신순으로 가져옵니다.
        List<StuStat> statHistory = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId());
        
        // (2) 리스트가 비어있는지 확인하고, 있으면 첫 번째(0번)를 씁니다.
        String status = "재학"; // 기본값 설정
        
        if (!statHistory.isEmpty()) {
            status = statHistory.get(0).getStatus(); // [핵심] 0번째가 가장 최신 글입니다.
        }

        // 2. [FUN-003] 분석용 프롬프트 작성
        String analysisPrompt = """
                다음 학생의 데이터를 분석하여 '중도 이탈(자퇴) 위험도'를 예측해주세요.
                
                [학생 데이터]
                - 이름: %s
                - 평균 학점: %.2f / 4.5
                - 최근 결석 횟수: %d회
                - 학적 상태: %s
                
                [요청사항]
                1. 위험도를 0~100 사이의 숫자로만 답하세요. (높을수록 위험)
                2. 위험도(숫자) 뒤에 줄바꿈을 하고 원인을 한 줄로 요약해주세요.
                (형식 예시: 
                85
                성적 하락세가 뚜렷하며 잦은 결석으로 학업 지속 의지가 낮음)
                """.formatted(student.getName(), avgGrade, absenceCount, status);

        try {
            // 3. Gemini 호출
            String result = geminiService.talk(analysisPrompt);

            // 4. 결과 파싱
            String[] parts = result.split("\n");
            Double riskScore = Double.parseDouble(parts[0].trim());
            String reason = parts.length > 1 ? parts[1].trim() : "분석된 원인 없음";
            
            // 5. [FUN-002] 위기 단계 설정 (점수에 따른 등급 부여)
            String riskLevel = determineLevel(riskScore);

            // 6. [FUN-002] 위기 학생 알림 (심각 단계 시 교수님께 알림)
            if ("심각".equals(riskLevel)) {
                sendAlertToProfessor(student, riskLevel, reason);
            }

            // 7. DB 저장 (DropoutRisk 엔티티)
            DropoutRisk risk = DropoutRisk.builder()
                    .student(student)
                    .riskScore(riskScore)
                    .riskLevel(riskLevel)
                    .reason(reason)
                    .analyzedDate(LocalDate.now())
                    .build();
            
            dropoutRiskRepository.save(risk);

        } catch (Exception e) {
            log.error("학생({}) 분석 중 오류 발생: {}", student.getName(), e.getMessage());
        }
    }

    // [FUN-002] 위기 징후 정의 및 시나리오 설정에 따른 등급 분류 
    private String determineLevel(Double score) {
        if (score >= 90) return "심각"; // 즉시 상담 필요
        if (score >= 70) return "경고"; // 모니터링 필요
        if (score >= 50) return "주의"; // 관심 필요
        return "정상";
    }

    // [FUN-002] 위기 학생 감지 시 담당 지도교수에게 알림 
    private void sendAlertToProfessor(Student student, String level, String reason) {
        // 교수님 정보가 있다면 알림 로직 실행
        // if (student.getDepartment() != null) { ... }
        log.warn("[알림 발송] 대상: {} 교수님 / 내용: 학생 {}이(가) 중도이탈 위험 '{}' 단계입니다. 사유: {}", 
                "지도교수", student.getName(), level, reason);
        
        // TODO: NotificationRepository를 만들었다면 여기서 save() 하세요.
        // Notification noti = Notification.builder()...build();
        // notificationRepository.save(noti);
    }
}