package com.university.home.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // jakarta 대신 springframework 권장

import com.university.home.entity.ChatLog;
import com.university.home.entity.Student;
import com.university.home.repository.ChatLogRepository;
import com.university.home.repository.StudentRepository; // [필수] 학생을 찾기 위해 추가

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiService geminiService;
    private final ChatLogRepository chatLogRepository;
    private final StudentRepository studentRepository; // [추가] 리포지토리 주입
    private final GradeService gradeService;
   
    
    @Transactional
    public String ask(Long studentId, String question) {
        
    	// 1. 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 찾기 실패"));
        
        // 2. [핵심] 학생의 모든 정보를 문자열로 예쁘게 포장하기
        String studentProfile = makeStudentInfoString(student);

        // 3. 프롬프트에 주입
        String systemPrompt = """
                당신은 '그린대학교'의 학사 행정 챗봇입니다.
                아래 [학생 상세 프로필]을 참고하여, 질문에 대해 친절하고 정확하게 답변해주세요.
                (학생의 개인정보를 물어보면 프로필 내용을 바탕으로 대답하세요.)
                
                %s
                
                [질문]: %s
                """.formatted(studentProfile, question);

        // 4. Gemini 호출
        String answer = geminiService.talk(systemPrompt);

        // 5. 저장
        ChatLog log = ChatLog.builder()
                .student(student)
                .question(question)
                .answer(answer)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatLogRepository.save(log);

        return answer;
    }
    //학생 엔티티 정보
    private String makeStudentInfoString(Student student) {
    	//학점계산
    	Integer totalCredits = gradeService.calculateTotalCredits(student.getId());
    	Double avgGrade = gradeService.calculateAverageGrade(student.getId());
    	
    	//학생 정보 Null방지
    	String dept = (student.getDepartment() != null) ? student.getDepartment().getName() : "학부 미배정";
    	String entrance = (student.getEntranceDate() != null) ? student.getEntranceDate().toString() : "정보없음";
    	String birth = (student.getBirthDate() != null) ? student.getBirthDate().toString() : "정보 없음";
        String tel = (student.getTel() != null) ? student.getTel() : "정보 없음";
        String addr = (student.getAddress() != null) ? student.getAddress() : "정보 없음";
        String email = (student.getEmail() != null) ? student.getEmail() : "정보 없음";
        String gender = (student.getGender() != null) ? student.getGender() : "정보 없음";		
    			
        return """
                [학생 상세 프로필]
                - 학번: %d
                - 이름: %s
                - 성별: %s
                - 생년월일: %s
                - 소속 학과: %s
                - 학년/학기: %d학년 %d학기
                - 입학일: %s
                - 연락처: %s
                - 이메일: %s
                - 주소: %s
                - 총 이수 학점: %d학점 (졸업요건 130)
                - 전체 평균 평점: %.2f점
                """.formatted(
                    student.getId(), student.getName(), gender, birth, dept, 
                    student.getGrade(), student.getSemester(), entrance, 
                    tel, email, addr, 
                    totalCredits, avgGrade
                );
    }		
    			
    
    public List<ChatLog> getChatHistory(Long studentId) {
        // 과거 대화부터 순서대로 보여주기 위해 Asc(오름차순) 사용
        return chatLogRepository.findByStudentIdOrderByCreatedAtAsc(studentId);
    }
}
