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
}