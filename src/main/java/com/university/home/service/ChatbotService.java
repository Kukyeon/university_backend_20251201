package com.university.home.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.entity.ChatLog;
import com.university.home.entity.DropoutRisk;
import com.university.home.entity.Professor;
import com.university.home.entity.StuSub;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.ChatLogRepository;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiService geminiService;
    private final ChatLogRepository chatLogRepository;
    private final StudentRepository studentRepository; 
    private final GradeService gradeService;
    private final DropoutRiskRepository dropoutRiskRepository;
    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository;
    private final ProfessorRepository professorRepository;

    @Transactional
    public String ask(Long studentId, String question) {
        
        // 1. 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 찾기 실패"));

        // 2. 학생의 위험도 조회 및 안내 문구 생성
        String riskGuidance = "";
        DropoutRisk risk = dropoutRiskRepository.findTopByStudentIdOrderByAnalyzedDateDesc(studentId)
                .orElse(null);

        if (risk != null && "심각".equals(risk.getRiskLevel())) {
            riskGuidance = """
                [💡 대화 가이드라인]
                이 학생은 현재 학업 지속에 어려움을 겪고 있을 가능성이 높습니다.
                답변의 마지막에 기계적인 경고(위험 단계 등)를 하는 대신, 친구처럼 따뜻하고 자연스럽게 아래 내용을 덧붙여주세요.
                
                1. "요즘 학업이나 학교생활에 힘든 점은 없으신가요?"라고 안부 묻기.
                2. "혹시 고민이 있다면 교수님이나 상담 센터에서 편하게 이야기를 나눌 수 있어요."라고 권유하기.
                3. 그리고 반드시 상담 예약 링크를 보여주기: 👉 [상담 예약 바로가기](/student-schedule)
                """;
        }
        Long currentYear = 2023L;
        Long currentSemester = 1L;

        //교수님 정보 조회
        String professorInfo = "정보 없음";
        if (student.getDepartment() != null) {
            List<Professor> professors = professorRepository.findByDepartmentId(student.getDepartment().getId());
            
            // 교수님 이름과 이메일(또는 연락처)을 문자열로 변환
            professorInfo = professors.stream()
                    .map(p -> String.format("%s (%s)", p.getName(), p.getEmail())) // 예: 김철수 (cs@univ.ac.kr)
                    .collect(Collectors.joining(", "));
        }
        
        
        // 3. 학생 및 수강 내역 조회 (재료 수집)
        List<StuSub> history = stuSubRepository.findByStudentId(studentId);
        
        
     // A. 과거 수강 과목 (현재 학기가 아닌 것들)
        String pastCourses = history.stream()
                .filter(sub -> !sub.getSubject().getSubYear().equals(currentYear) 
                            || !sub.getSubject().getSemester().equals(currentSemester))
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));

        // B. 이번 학기 신청 과목 (현재 학기와 일치하는 것들)
        String currentCourses = history.stream()
                .filter(sub -> sub.getSubject().getSubYear().equals(currentYear) 
                            && sub.getSubject().getSemester().equals(currentSemester))
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));

        // (만약 비어있으면 "없음" 처리)
        if (pastCourses.isEmpty()) pastCourses = "없음";
        if (currentCourses.isEmpty()) currentCourses = "없음 (아직 신청 안 함)";
        // 수강했던 과목명 문자열 변환
        String takenCourses = history.stream()
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        
     // ★ [추가] C. 상세 성적/출석 정보 생성
        String detailedGradeInfo = makeDetailedGradeInfo(history);

     // 3. 이번 학기 개설된 강의 목록 조회 (수강신청 안 한 과목들 중 추천용)
        List<Subject> openSubjects = subjectRepository.findBySubYearAndSemester(currentYear, currentSemester);
        String availableCourses = openSubjects.stream()
                .map(Subject::getName)
                .limit(10) 
                .collect(Collectors.joining(", "));
        
        // 4. 학생 프로필 생성
        String studentProfile = makeStudentInfoString(student);

        // ★ [핵심] 6. 사이트맵(URL 정보) 정의
        // 실제 리액트 라우터(App.js)에 정의된 경로와 일치시켜야 합니다.
        String siteMap = """
                [주요 서비스 링크]
                - 수강 신청: /enrollment
                - 성적 조회: /grade
                - 휴학 신청/조회: /student/leave
                - 마이 페이지: /student/my
                - 강의 목록: /course/list
                - 장학금 조회: /student/scholarship
                """;

        // 7. 프롬프트 작성
        String systemPrompt = """
               당신은 '우리대학교'의 학사 행정 챗봇입니다.
                아래 정보를 바탕으로 학생의 질문에 친절하게 답변해주세요.

                [학생 상세 프로필]
                %s
                
                [소속 학과 교수진]
                %s

                %s

                [✅ 기수강 과목 (이미 학점 이수함)]
                %s
                
                [📅 이번 학기 수강신청 내역 (현재 듣고 있는 중)]
                %s

                [📊 상세 성적 및 출석/태도 현황 (중요)]
                %s
                
                [이번 학기 전체 개설 강의 목록 (참고용)]
                %s

                [답변 작성 지침]
                1. 학생이 [주요 서비스 링크]에 있는 기능을 물어보면, 마크다운 링크를 제공하세요.
                2. "추천해줘" 질문 시 기수강 과목을 제외하고 추천하세요.
                3. 학생이 "중간고사 점수 어때?"나 "결석 얼마나 했어?" 같이 구체적인 성적/태도를 물어보면 [상세 성적 및 출석/태도 현황] 데이터를 확인하여 정확한 수치로 답해주세요.
                4. 개인정보(학점, 학과 등)는 [학생 상세 프로필]을 참고하세요.
                5. 답변은 간결하고 명확하게 작성하세요.
                %s
                
                [질문]: %s
                """.formatted(
                        studentProfile,    // 1
                        professorInfo,     // 2
                        siteMap,           // 3
                        pastCourses,       // 4
                        currentCourses,    // 5
                        detailedGradeInfo, // 6
                        availableCourses,  // 7
                        riskGuidance,      // 8
                        question           // 9
                );
//질문쪽 인자 [질문]: %s가 없었기에 마지막인 교수님 질문으로 들어가짐 -> 프롬포트 유의사항, 순서 맞춰야함
        // 8. Gemini 호출
        String answer = geminiService.talk(systemPrompt);

        // 9. 저장
        ChatLog log = ChatLog.builder()
                .student(student)
                .question(question)
                .answer(answer)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatLogRepository.save(log);

        return answer;
    }
    
 // ★ [신규 메서드] 상세 성적 정보를 문자열로 변환
    private String makeDetailedGradeInfo(List<StuSub> history) {
        if (history == null || history.isEmpty()) {
            return "수강 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        for (StuSub sub : history) {
            Subject subject = sub.getSubject();
            StuSubDetail detail = sub.getDetail(); // 상세 정보 가져오기

            // 과목명 헤더
            sb.append(String.format("- [%d-%d학기] %s: ", 
                    subject.getSubYear(), subject.getSemester(), subject.getName()));

            // 최종 등급 (있는 경우)
            if (sub.getGrade() != null) {
                sb.append(String.format("최종성적 %s, ", sub.getGrade()));
            }

            // 상세 점수 (StuSubDetail이 존재하는 경우)
            if (detail != null) {
//                sb.append(String.format("중간고사 %d점, 기말고사 %d점, 과제 %d점, 출석(결석 %d회 / 지각 %d회), 환산총점 %d점",
                sb.append(String.format("중간고사 %d점, 기말고사 %d점, 과제 %d점, 출석(결석 %d회 / 지각 %d회)",
                        detail.getMidExam() != null ? detail.getMidExam() : 0,
                        detail.getFinalExam() != null ? detail.getFinalExam() : 0,
                        detail.getHomework() != null ? detail.getHomework() : 0,
                        detail.getAbsent() != null ? detail.getAbsent() : 0,
                        detail.getLateness() != null ? detail.getLateness() : 0
//                      detail.getConvertedMark() != null ? detail.getConvertedMark() : 0
                ));
            } else {
                sb.append("상세 점수 데이터 없음");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // (makeStudentInfoString 등 나머지 메서드는 기존 유지)
    private String makeStudentInfoString(Student student) {
        // ... (기존 코드와 동일) ...
        Integer totalCredits = gradeService.calculateTotalCredits(student.getId());
        Double avgGrade = gradeService.calculateCurrentSemesterAverageGrade(student.getId());      
        String dept = (student.getDepartment() != null) ? student.getDepartment().getName() : "학부 미배정";
        String entrance = (student.getEntranceDate() != null) ? student.getEntranceDate().toString() : "정보없음";
        String birth = (student.getBirthDate() != null) ? student.getBirthDate().toString() : "정보 없음";
        String tel = (student.getTel() != null) ? student.getTel() : "정보 없음";
        String addr = (student.getAddress() != null) ? student.getAddress() : "정보 없음";
        String email = (student.getEmail() != null) ? student.getEmail() : "정보 없음";
        String gender = (student.getGender() != null) ? student.getGender() : "정보 없음";       
        return """
                - 학번: %d
                - 이름: %s
                - 성별: %s
                - 소속 학과: %s
                - 학년/학기: %d학년 %d학기
                - 연락처: %s
                - 총 이수 학점: %d학점
                - 이번 학기 평점: %.2f점
                """.formatted(
                    student.getId(), student.getName(), gender, dept, 
                    student.getGrade(), student.getSemester(), 
                    tel, totalCredits, avgGrade
                );
    }       

    // ... (나머지 메서드 유지) ...
    public List<ChatLog> getChatHistory(Long studentId) {
        return chatLogRepository.findByStudentIdOrderByCreatedAtAsc(studentId);
    }
    
    @Transactional
    public void clearChatHistory(Long studnetId) {
        chatLogRepository.deleteByStudent_Id(studnetId);
    }
}