package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.university.home.entity.DropoutRisk;
import com.university.home.entity.Student;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DropoutAnalysisService {

    private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final DropoutRiskRepository dropoutRiskRepository;
    // private final GradeRepository gradeRepository; (성적 조회용, B팀원이 Native Query로 구현한 것)

    /**
     * 전체 학생에 대해 중도 이탈 위험 분석 실행 (배치 혹은 관리자 버튼용)
     */
    @Transactional
    public void analyzeAllStudents() {
        List<Student> students = studentRepository.findAll();

        for (Student student : students) {
            analyzeStudentRisk(student);
        }
    }

    private void analyzeStudentRisk(Student student) {
        // 1. 학생의 데이터 수집 (성적, 휴학 이력 등)
        // 예시: 실제로는 DB에서 가져온 값을 넣어야 합니다.
        double avgGrade = 2.5; // gradeRepository.findAvgScore(student.getId()); 
        int absenceCount = 5;  // 출결 DB에서 조회
        
        // 2. 분석용 프롬프트 작성
        //[cite_start]// [FUN-003] 데이터를 기반으로 중도탈락 예측 [cite: 91]
        String analysisPrompt = """
                다음 학생의 데이터를 분석하여 '중도 이탈(자퇴) 위험도'를 예측해주세요.
                
                [학생 데이터]
                - 평균 학점: %.2f / 4.5
                - 최근 결석 횟수: %d회
                - 학적 상태: %s
                
                [요청사항]
                1. 위험도를 0~100 사이의 숫자로만 답하세요. (예: 85)
                2. 위험도(숫자) 뒤에 줄바꿈을 하고 간단한 원인을 한 줄로 적어주세요.
                (형식 예시: 
                80
                성적 저조 및 잦은 결석으로 인한 학업 흥미도 저하 예상)
                """.formatted(avgGrade, absenceCount, "재학");

        // 3. Gemini 호출
        String result = geminiService.talk(analysisPrompt);

        // 4. 결과 파싱 (Gemini가 준 문자열을 분리)
        try {
            String[] parts = result.split("\n");
            Double riskScore = Double.parseDouble(parts[0].trim());
            String reason = parts.length > 1 ? parts[1].trim() : "분석된 원인 없음";
            String riskLevel = determineLevel(riskScore);

            // 5. DB 저장 (DropoutRisk 엔티티)
            DropoutRisk risk = DropoutRisk.builder()
                    .student(student)
                    .riskScore(riskScore)
                    .riskLevel(riskLevel)
                    .reason(reason)
                    .analyzedDate(LocalDate.now())
                    .build();
            
            dropoutRiskRepository.save(risk);

        } catch (Exception e) {
            System.err.println("분석 결과 파싱 실패: " + student.getName());
        }
    }

    private String determineLevel(Double score) {
        if (score >= 80) return "위험";
        if (score >= 50) return "주의";
        return "정상";
    }
}