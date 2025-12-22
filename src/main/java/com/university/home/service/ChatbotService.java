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
        //String riskGuidance = "";
        DropoutRisk risk = dropoutRiskRepository.findTopByStudentIdOrderByAnalyzedDateDesc(studentId)
                .orElse(null);
        // ★ [수정 1] AI에게 줄 '태도 지침'과 자바가 붙일 '필수 링크'를 분리
        String attitudeGuide = ""; // AI에게 줄 지침 (프롬프트용)
        String mandatorySuffix = ""; // 답변 뒤에 무조건 붙일 꼬리말 (링크용)
        if (risk != null && (risk.getRiskLevel().contains("심각") || risk.getRiskLevel().contains("경고"))) {
        	// (1) AI에게는 "따뜻하게 말해라"라고만 시킵니다. (링크 강요 X)
            attitudeGuide = """
                [⚠️ 중요: 대화 태도 가이드]
                이 학생은 현재 학업 중단 위험(심각/경고) 단계입니다.
                질문에 대한 답변을 마친 후, 문단은 나누지 말고 **"혹시 요즘 학교생활에 힘든 점은 없으신가요?"** 와 같이 학생을 걱정하는 따뜻한 멘트를 한 문장 덧붙여주세요.
                """;
            mandatorySuffix = """
                    \n
                    --------------------------------------------------
                    💬 **상담이 필요하신가요?**
                    교수님과 편하게 이야기를 나눌 수 있어요.
                    
                    👉 [상담 예약 바로가기](/counseling)
                    """;
            
        }
        Long currentYear = 2023L;
        Long currentSemester = 1L;

        Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                .orElse(null);

        if (latestSubject != null) {
            currentYear = latestSubject.getSubYear();
            currentSemester = latestSubject.getSemester();
        }
        
        // AI에게 알려줄 기준 시점 문자열 생성
        String semesterInfo = String.format("현재 학사 기준: %d년 %d학기 (최신 개설 강의 기준)", currentYear, currentSemester);
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
        
     // A. 과거 수강 (현재 학기가 아닌 것)
        // effectively final 문제 해결을 위해 로컬 변수 재할당
        Long finalCurrentYear = currentYear;
        Long finalCurrentSemester = currentSemester;
        
        
     // A. 과거 수강 과목 (현재 학기가 아닌 것들)
        String pastCourses = history.stream()
                .filter(sub -> !sub.getSubject().getSubYear().equals(finalCurrentYear) 
                            || !sub.getSubject().getSemester().equals(finalCurrentSemester))
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        if (pastCourses.isEmpty()) pastCourses = "없음";

     // B. 이번 학기 수강 (현재 학기와 일치하는 것)
        String currentCourses = history.stream()
                .filter(sub -> sub.getSubject().getSubYear().equals(finalCurrentYear) 
                            && sub.getSubject().getSemester().equals(finalCurrentSemester))
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        if (currentCourses.isEmpty()) currentCourses = "없음 (아직 신청 안 함)";

        // (만약 비어있으면 "없음" 처리)
//        if (pastCourses.isEmpty()) pastCourses = "없음";
//        if (currentCourses.isEmpty()) currentCourses = "없음 (아직 신청 안 함)";
        // 수강했던 과목명 문자열 변환
        String takenCourses = history.stream()
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        
     // ★ [추가] C. 상세 성적/출석 정보 생성
        String detailedGradeInfo = makeDetailedGradeInfo(history);

     // 3. 이번 학기 개설된 강의 목록 조회 (수강신청 안 한 과목들 중 추천용)
        List<Subject> openSubjects = subjectRepository.findBySubYearAndSemester(finalCurrentYear, finalCurrentSemester);
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
                - 수강 신청: /sugang
                - 성적 조회: /grade
                - 휴학 신청/조회: /student/leave
                - 마이 페이지: /student/my
                - 강의 목록: /course/list
                - 장학금 조회: /student/scholarship
                """;

        // 7. 프롬프트 작성
        String systemPrompt = """
     	       당신은 '우리대학교'의 학사 행정 챗봇입니다.
     	       제공된 데이터를 바탕으로 학생의 질문에 답변하세요.

     	       [⚠️ 출력 형식 지침]
     	       1. **여백 최소화**: 불필요한 빈 줄(공백 라인)을 넣지 마세요. 문장은 붙여서 쓰세요.
        	   2. **목록 형식**: 목록을 나열할 때는 줄바꿈을 한 번만 하세요.
        	   3. **간결함**: "~입니다", "~합니다" 같은 서술어보다 명사형 종결이나 간결한 문장을 사용하세요.
     	       4. **마크다운 사용**: 목록은 Bullet point(-)를 사용하여 정리하세요.
     	       5. **명확성**: 핵심 키워드는 굵게(**) 표시하세요.
     	       4. **TMI 금지**: 사용자가 묻지 않은 전체 리스트를 나열하지 마세요. 질문에 대한 답만 하세요.

     	       [⚠️ 필수 포함 사항 (중요)]
     	       아래 '[🚨 특별 안내 메시지]' 항목에 내용이 있다면, 답변의 **맨 마지막**에 해당 내용을 **반드시 그대로 출력**해야 합니다. (상담 링크 포함)

     	       [🕒 현재 학사 일정 기준]
     	       %s

     	       [학생 상세 프로필]
     	       %s
     	       
     	       [소속 학과 교수진]
     	       %s

     	       [사이트맵 (링크 정보)]
     	       %s

     	       [✅ 기수강 과목]
     	       %s
     	       
     	       [📅 이번 학기 수강신청 내역]
     	       %s

     	       [📊 상세 성적 및 출석/태도 현황]
     	       %s
     	       
     	       [이번 학기 개설 강의 (참고용)]
     	       %s
     	       
     	       [🚨 특별 안내 메시지 (내용이 있으면 무조건 답변 끝에 붙여넣기)]
     	       %s

     	       [질문]: %s
     	       """.formatted(
     	                semesterInfo,      // 1
     	                studentProfile,    // 2
     	                professorInfo,     // 3
     	                siteMap,           // 4
     	                pastCourses,       // 5
     	                currentCourses,    // 6
     	                detailedGradeInfo, // 7
     	                availableCourses,  // 8
     	                attitudeGuide,     // 9 (이 내용이 있으면 챗봇이 그대로 뱉어냄)
     	                question           // 10
     	       );
//질문쪽 인자 [질문]: %s가 없었기에 마지막인 교수님 질문으로 들어가짐 -> 프롬포트 유의사항, 순서 맞춰야함
     // 8. Gemini 호출
        String rawAnswer = geminiService.talk(systemPrompt);

        // ★ [핵심] 줄바꿈 압축 로직
        // 1. 연속된 3개 이상의 줄바꿈(\n\n\n...) -> 2개(\n\n)로 (문단 구분용)
        // 2. 불필요한 공백 라인 제거
        String answer = rawAnswer.replaceAll("(\\r?\\n){3,}", "\n\n").trim();
        if (!mandatorySuffix.isEmpty()) {
            answer += mandatorySuffix;
        }
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