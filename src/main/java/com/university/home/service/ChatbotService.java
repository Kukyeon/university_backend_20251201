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
                \n\n[🚨 긴급 안내]
                학생분은 현재 학업 중도이탈 위험 '심각' 단계로 분석되었습니다.
                학업에 어려움이 있으시다면, 반드시 교수님이나 상담 센터의 도움을 받으세요.
                👉 [상담 예약 바로가기](/student/counseling)
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
                - 성적 조회: /student/grade
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
                
                %s

                [✅ 기수강 과목 (이미 학점 이수함)]
                %s
                
                [📅 이번 학기 수강신청 내역 (현재 듣고 있는 중)]
                %s
                
                [이번 학기 전체 개설 강의 목록 (참고용)]
                %s

                [답변 작성 지침]
                1. 학생이 [주요 서비스 링크]에 있는 기능을 물어보면, 답변 중에 **마크다운 링크 형식**으로 바로가기를 제공하세요.
                2. "추천해줘" 질문 시 [기수강 과목]과 [이번 학기 수강신청 내역]을 모두 고려하여, 중복되지 않게 [전체 개설 강의] 중에서 추천하세요.
                3. 학생이 "나 뭐 신청했어?"라고 물으면 [이번 학기 수강신청 내역]을 알려주세요.
                4. 학생이 "과거에 뭐 들었어?"라고 물으면 [기수강 과목]을 알려주세요.
                2. 학생의 개인정보(학점, 학과 등)를 물어보면 [학생 상세 프로필]을 참고하여 정확히 대답하세요.
                3. 강의 추천 요청 시, 과거 수강 과목과 개설 강의를 비교하여 3가지를 추천하고 이유를 설명하세요.
                4. 답변은 간결하고 명확하게 요약해서 작성하세요.
                %s
                
                [질문]: %s
                """.formatted(
                        studentProfile, 
                        siteMap,         
                        pastCourses,      // 과거
                        currentCourses,   // 현재 (방금 신청한 것)
                        availableCourses, 
                        riskGuidance,  
                        professorInfo,
                        question
                );

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

    // (makeStudentInfoString 등 나머지 메서드는 기존 유지)
    private String makeStudentInfoString(Student student) {
        // ... (기존 코드와 동일) ...
        Integer totalCredits = gradeService.calculateTotalCredits(student.getId());
        Double avgGrade = gradeService.calculateAverageGrade(student.getId());
        
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
                - 전체 평균 평점: %.2f점
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