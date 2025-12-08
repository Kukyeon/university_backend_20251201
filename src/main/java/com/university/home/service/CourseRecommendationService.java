package com.university.home.service;

import com.university.home.entity.StuSub;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository; // (전체 강의 목록 조회용)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseRecommendationService {

    private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public String recommendCourses(Long studentId) {
        
        // 1. 학생 및 수강 내역 조회 (재료 수집)
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<StuSub> history = stuSubRepository.findByStudentId(studentId);
        
        // 2. 수강했던 과목명들을 문자열로 변환 (예: "자바프로그래밍, 데이터베이스, ...")
        String takenCourses = history.stream()
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));

        // 3. 이번 학기 개설된 강의 목록 조회 (여기서 추천해달라고 할 예정)
        // (SubjectRepository에 findBySubYearAndSemester 메서드가 있다고 가정)
        List<Subject> openSubjects = subjectRepository.findBySubYearAndSemester(2025L, 1L);
        String availableCourses = openSubjects.stream()
                .map(Subject::getName)
                .limit(5) // 토큰 제한 때문에 30개 정도만 예시로 줌
                .collect(Collectors.joining(", "));

        // 4. 추천 프롬프트 작성 [FUN-001: 교과 추천]
        String prompt = """
                당신은 대학 학사 AI입니다. 학생의 수강 이력을 분석하여, 이번 학기 개설 과목 중 3가지를 추천해주세요.
                
                [학생 정보]
                - 학과: %s
                - 과거 수강 과목: [%s]
                
                [이번 학기 개설 과목 목록]
                [%s]
                
                [요청사항]
                1. 학생의 전공 연관성과 흥미를 고려하여 3과목을 추천하세요.
                2. 추천 이유를 각 과목당 1줄로 짧게 설명하세요.
                3. 형식:
                - 과목명: 추천이유
                """.formatted(student.getDepartment().getName(), takenCourses, availableCourses);

        // 5. Gemini 호출
        return geminiService.talk(prompt);
    }
 // 1. [조회] 개설된 전체 강의 목록 가져오기 (수강신청 화면용)
    @Transactional(readOnly = true)
    public List<Subject> getAvailableCourses(Long subYear, Long semester) {
    	// 1. 만약 연도나 학기가 입력되지 않았다면? (null 체크)
        if (subYear == null || semester == null) {
            // DB에서 가장 최신 과목 하나를 꺼내봅니다.
            Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                    .orElse(null);

            if (latestSubject != null) {
                // 최신 과목의 연도와 학기를 사용!
                subYear = latestSubject.getSubYear();
                semester = latestSubject.getSemester();
                System.out.println("🤖 자동 감지된 최신 학기: " + subYear + "년 " + semester + "학기");
            } else {
                // DB가 텅 비어있으면 기본값 (예: 2025-1)
                subYear = 2023L;
                semester = 1L;
               
            }
        } else {
            System.out.println("📡 프론트 요청 학기: " + subYear + "년 " + semester + "학기");
        }

        // 2. 결정된 연도/학기로 조회
        List<Subject> result = subjectRepository.findBySubYearAndSemester(subYear, semester);
       
        
        return result;
    }
    // 2. [조회] 나의 수강 내역 가져오기 (마이페이지용)
    @Transactional(readOnly = true)
    public List<StuSub> getMyCourseHistory(Long studentId) {
        return stuSubRepository.findByStudentId(studentId);
    }

    // 3. [동작] 수강 신청 하기 (핵심 로직!)
    @Transactional
    public String registerCourse(Long studentId, Long subjectId) {
        // (1) 학생과 과목 정보 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 없음"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("과목 없음"));

        // (2) 중복 신청 체크 (이미 신청했는지?)
        // StuSubRepository에 existsByStudentIdAndSubjectId 메서드가 필요합니다! (없으면 추가하세요)
        boolean alreadyRegistered = stuSubRepository.existsByStudentIdAndSubjectId(studentId, subjectId);
        if (alreadyRegistered) {
            throw new IllegalStateException("이미 신청한 과목입니다.");
        }

        // (3) 정원 초과 체크
        if (subject.getNumOfStudent() >= subject.getCapacity()) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }

        // (4) 수강신청 완료 (DB 저장)
        StuSub newEnrollment = new StuSub();
        newEnrollment.setStudent(student);
        newEnrollment.setSubject(subject);
        newEnrollment.setGrade(null); // 성적은 아직 없음
        
        stuSubRepository.save(newEnrollment);

        // (5) 과목의 수강인원 +1 증가 (Dirty Checking)
        subject.setNumOfStudent(subject.getNumOfStudent() + 1);
        
        return subject.getName() + " 수강신청 성공!";
    }
}