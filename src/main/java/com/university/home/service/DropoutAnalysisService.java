package com.university.home.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;


import com.university.home.entity.DropoutRisk;
import com.university.home.entity.Notification;
import com.university.home.entity.Professor;
import com.university.home.entity.StuStat;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.NotificationRepository;
import com.university.home.repository.ProfessorRepository;
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
    private final ProfessorRepository professorRepository;
    // [추가 1] 실제 성적 계산을 위해 GradeService 주입
    private final GradeService gradeService; 
    
    // [추가 2] 알림 저장을 위한 Repository (필요 시 주석 해제 후 사용)
    private final NotificationRepository notificationRepository;
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
            	sendAlert(student, riskLevel, reason);
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
    private void sendAlert(Student student, String level, String reason) {
        // 1. 교수님 ID 가져오기 (없으면 스킵)
    	if (student.getDepartment().getProfessors() == null) {
            log.warn("학생({})의 지도교수 정보가 없어 알림을 보낼 수 없습니다.", student.getName());
            return; 
        }
    	//학과 교수님에게 알림보내기
    	if (student.getDepartment() != null) {
            Long deptId = student.getDepartment().getId();
            List<Professor> professors = professorRepository.findByDepartmentId(deptId);

        // 2. 알림 저장
        for (Professor prof : professors) {
        Notification noti = Notification.builder() 
        		.receiverId(prof.getId()) // 교수님 ID
                .content(String.format("🚨[위험 알림] %s 학생이 '%s' 단계입니다. (사유: %s)", student.getName(), student.getDepartment(),level, reason))
                .url("/dashboard/risk-student/" + student.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(noti);
        }
        log.info("학과 교수님들께 알림 전송 완료", professors.size());
    }
    	// ---------------------------------------------------
        // 2. 학생 본인에게 상담 권유 알림 보내기 ->예방대책
        // ---------------------------------------------------
        Notification studentNoti = Notification.builder()
                .receiverId(student.getId()) // 학생 본인 ID
                .content(String.format("💬 [상담 권장] %s님, 학업에 어려움은 없으신가요? 교수님과 상담을 받아보세요.", student.getName()))
                .url("/student/counseling") // 학생은 상담 예약 페이지로 이동
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(studentNoti);
        log.info("학생({}) 본인에게 상담 권유 알림 전송 완료", student.getName());
    }
    		
  }
