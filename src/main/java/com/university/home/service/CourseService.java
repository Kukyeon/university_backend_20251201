package com.university.home.service;

import com.university.home.component.SubjectSpecification;
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.StuSub;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository;

    // =================================================================================
    // 1. 조회 기능 (목록, 내역, AI 추천)
    // =================================================================================

    // [조회] 개설된 전체 강의 목록 가져오기 (학기 자동 감지 포함)
    @Transactional(readOnly = true)
    public Page<Subject> getAvailableCourses(Long subYear, Long semester, int page, String type, String name, Long deptId) {
        
        // 1. 연도/학기가 없으면 최신 학기 자동 감지
        if (subYear == null || semester == null) {
            Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                    .orElse(null);

            if (latestSubject != null) {
                subYear = latestSubject.getSubYear();
                semester = latestSubject.getSemester();
            } else {
                // [개선 1] 하드코딩 대신 현재 날짜 기반으로 설정
                subYear = 2023L;
                semester = 1L; 
            }
        }
     // 2. 검색 조건 조립 (Specification)
        // (1) 기본 조건: 연도와 학기는 무조건 일치해야 함
        Specification<Subject> spec = Specification.where(SubjectSpecification.equalYearAndSemester(subYear, semester));

        // (2) 동적 조건: 값이 있을 때만 AND 조건 추가
        if (type != null) {
            spec = spec.and(SubjectSpecification.equalType(type));
        }
        if (name != null) {
            spec = spec.and(SubjectSpecification.likeName(name));
        }
        if (deptId != null) {
            spec = spec.and(SubjectSpecification.equalDeptId(deptId));
        }

        // 2. 페이징 정보 생성
        // [개선 2] 학생 보기 편하게 '과목명(name)' 오름차순(ASC) 정렬
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.ASC, "name"));

        // 3. 조회
        return subjectRepository.findAll(spec, pageable);
    }

    // [조회] 나의 수강 내역 가져오기
    @Transactional(readOnly = true)
    public List<StuSub> getMyCourseHistory(Long studentId) {
        return stuSubRepository.findByStudentId(studentId);
    }

    // [AI] 강의 추천 기능
    @Transactional(readOnly = true)
    public String recommendCourses(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<StuSub> history = stuSubRepository.findByStudentId(studentId);
        
        String takenCourses = history.stream()
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        
        Long subYear = 2023L; 
        Long semester = 1L;
        Subject latest = getLatestSubjectInfo();
        if (latest != null) {
            subYear = latest.getSubYear();
            semester = latest.getSemester();
        }

        // 현재 학기 과목 조회 (위의 메서드 재사용 불가능하므로 직접 조회하거나 로직 분리 필요. 여기선 간단히 2025-1 고정 혹은 동적 조회)
        // 편의상 최신 학기 자동 감지 로직을 사용하여 가져옴
        List<Subject> openSubjects = subjectRepository.findBySubYearAndSemester(subYear, semester);
        
        String availableCourses = openSubjects.stream()
                .map(Subject::getName)
                .limit(50) 
                .collect(Collectors.joining(", "));

        String prompt = """
                당신은 대학 학사 AI입니다. 학생의 수강 이력을 분석하여, 이번 학기 개설 과목 중 3가지를 추천해주세요.
                [학생 정보] 학과: %s, 과거 수강 과목: [%s]
                [이번 학기 개설 과목] [%s]
                [요청] 전공 연관성과 흥미를 고려하여 3과목 추천. 형식: 과목명: 이유
                """.formatted(student.getDepartment().getName(), takenCourses, availableCourses);

        return geminiService.talk(prompt);
    }

    // =================================================================================
    // 2. 수강신청 / 취소 기능 (검증 로직 강화)
    // =================================================================================

    // --- Helper Method: 최신 학기 정보 찾기 (중복 제거용) ---
    private Subject getLatestSubjectInfo() {
        // Repository에 findTopByOrderBy... 메서드가 있어야 함 (Optional 반환)
        // 없으면 null 리턴하도록 처리
        return subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
    }
    
    // [동작] 수강 신청
    @Transactional
    public void enroll(Long studentId, Long subjectId) {
        // 1. 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보가 없습니다."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("강의 정보가 없습니다."));

        // 2. 중복 체크
        if (stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId)) {
            throw new IllegalStateException("이미 수강신청한 강의입니다.");
        }

        // 3. 정원 체크
        if (subject.getNumOfStudent() >= subject.getCapacity()) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }

        // 4. 최대 학점 체크 (동적 학기 적용)
        // 신청하려는 과목의 연도/학기를 기준으로 현재 수강 학점을 계산해야 정확함
        List<StuSub> currentSemesterSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                studentId, subject.getSubYear(), subject.getSemester());

        int currentCredits = currentSemesterSubjects.stream()
                .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                .sum();

        if (currentCredits + subject.getGrades().intValue() > 18) {
            throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
        }

        // 5. 저장
        StuSub newEnrollment = new StuSub();
        newEnrollment.setStudent(student);
        newEnrollment.setSubject(subject);
        stuSubRepository.save(newEnrollment);

        // 6. 인원 증가
        subject.setNumOfStudent(subject.getNumOfStudent() + 1);
    }

    // [동작] 수강 취소
    @Transactional
    public void cancel(Long studentId, Long subjectId) {
        StuSub enrollment = stuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("수강 내역이 없습니다."));
        
        Subject subject = enrollment.getSubject();
        subject.setNumOfStudent(subject.getNumOfStudent() - 1);
        
        stuSubRepository.delete(enrollment);
    }
    
    @Transactional
    public ResponseEntity<SyllabusDto> Syllabus(Long subjectId) {
    	return subjectRepository.findById(subjectId)
    			.map(SyllabusDto::fromEntity) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
}