package com.university.home.service;

import com.university.home.component.SubjectSpecification;
import com.university.home.controller.SugangController; // ★ 기간 확인용 컨트롤러 import
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.PreStuSub; // ★ 예비수강 엔티티
import com.university.home.entity.StuSub;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.PreStuSubRepository;
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
    private final PreStuSubRepository preStuSubRepository;

    // =================================================================================
    // 1. 조회 기능 (목록, 내역, AI 추천)
    // =================================================================================

    // [조회] 개설된 전체 강의 목록 가져오기 (기존 유지)
    @Transactional(readOnly = true)
    public Page<Subject> getAvailableCourses(Long subYear, Long semester, int page, String type, String name, Long deptId) {
        
        if (subYear == null || semester == null) {
            Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                    .orElse(null);
            
            if (latestSubject != null) {
                subYear = latestSubject.getSubYear();
                semester = latestSubject.getSemester();
            } else {
                subYear = 2023L; // 최신 연도로 수정
                semester = 1L; 
            }
        }
        System.out.println(">>> [DEBUG] 현재 조회 조건: 연도=" + subYear + ", 학기=" + semester);
        Specification<Subject> spec = Specification.where(SubjectSpecification.equalYearAndSemester(subYear, semester));

        if (type != null && !type.isEmpty()) spec = spec.and(SubjectSpecification.equalType(type));
        if (name != null && !name.isEmpty()) spec = spec.and(SubjectSpecification.likeName(name));
        if (deptId != null) spec = spec.and(SubjectSpecification.equalDeptId(deptId));

        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.ASC, "name"));

        return subjectRepository.findAll(spec, pageable);
    }
    
 // =================================================================================
    // [추가] 모든 강좌 조회 (년도, 학기 상관없이 전체 조회)
    // =================================================================================
    @Transactional(readOnly = true)
    public Page<Subject> getAllCourses(int page, String type, String name, Long deptId, Long year, Long semester) {
        
        // ★ 1. Specification 초기화 에러 해결
        // where(null) 대신 "조건 없음"을 뜻하는 람다식 사용
        Specification<Subject> spec = (root, query, criteriaBuilder) -> null;

        // 2. 동적 조건 추가
        if (type != null && !type.isEmpty()) {
            spec = spec.and(SubjectSpecification.equalType(type));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and(SubjectSpecification.likeName(name));
        }
        if (deptId != null) {
            spec = spec.and(SubjectSpecification.equalDeptId(deptId));
        }
        
        // ★ [추가] 연도와 학기도 선택적으로 검색 가능하게 변경
        if (year != null) {
            spec = spec.and(SubjectSpecification.equalSubYear(year));
        }
        if (semester != null) {
            spec = spec.and(SubjectSpecification.equalSemester(semester));
        }

        // 3. 정렬 및 페이징 (최신순)
        Pageable pageable = PageRequest.of(page, 20, 
                Sort.by(Sort.Direction.DESC, "subYear")
                    .and(Sort.by(Sort.Direction.DESC, "semester"))
                    .and(Sort.by(Sort.Direction.ASC, "name"))
        );

        return subjectRepository.findAll(spec, pageable);
    }

    // [조회] 나의 수강 내역 가져오기 (★ 수정됨: 기간에 따라 다른 리스트 반환)
    @Transactional(readOnly = true)
    public List<?> getMyCourseHistory(Long studentId) {
        int period = SugangController.SUGANG_PERIOD;

        // 예비 수강신청 기간(0)이면 장바구니 목록 반환
        if (period == 0) {
            return preStuSubRepository.findByStudentId(studentId);
        } 
        // 본 수강신청 기간(1) 혹은 종료(2)면 실제 수강 목록 반환
        else {
            return stuSubRepository.findByStudentId(studentId);
        }
    }

    // [AI] 강의 추천 기능 (기존 유지)
    @Transactional(readOnly = true)
    public String recommendCourses(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<StuSub> history = stuSubRepository.findByStudentId(studentId);
        
        String takenCourses = history.stream()
                .map(sub -> sub.getSubject().getName())
                .collect(Collectors.joining(", "));
        
        Long subYear = 2025L; 
        Long semester = 1L;
        Subject latest = getLatestSubjectInfo();
        if (latest != null) {
            subYear = latest.getSubYear();
            semester = latest.getSemester();
        }

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
    // 2. 수강신청 / 취소 기능 (★ 핵심 수정: 기간별 로직 분기)
    // =================================================================================

    private Subject getLatestSubjectInfo() {
        return subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
    }
    
    // [동작] 수강 신청 (기간별 분기)
    @Transactional
    public void enroll(Long studentId, Long subjectId) {
        // 현재 기간 확인
        int period = SugangController.SUGANG_PERIOD;

        // 기간 2: 종료됨 -> 신청 불가
        if (period == 2) {
            throw new IllegalStateException("지금은 수강신청 기간이 아닙니다.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보가 없습니다."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("강의 정보가 없습니다."));

        validateTimeConflict(studentId, subject, period);
        // === [기간 0] 예비 수강 신청 (장바구니) ===
        if (period == 0) {
            // 중복 체크만 수행 (이미 담았는지)
            if (preStuSubRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
                throw new IllegalStateException("이미 장바구니에 담은 강의입니다.");
            }
         // 2. 정원 체크
            if (subject.getNumOfStudent() >= subject.getCapacity()) {
                throw new IllegalStateException("장바구니 정원이 마감되었습니다.");
            }
            
            
         // 3. 최대 학점 체크
            List<StuSub> currentSemesterSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, subject.getSubYear(), subject.getSemester());

            int currentCredits = currentSemesterSubjects.stream()
                    .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                    .sum();

            if (currentCredits + subject.getGrades().intValue() > 18) {
                throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
            }
            
            // 장바구니 저장 (정원 체크, 학점 체크 안 함, 인원수 증가 안 함)
            PreStuSub pre = new PreStuSub();
            pre.setStudent(student);
            pre.setSubject(subject);
            preStuSubRepository.save(pre);
            
         // 5. 인원 증가
            subject.setNumOfStudent(subject.getNumOfStudent() + 1);
        
        }

        // === [기간 1] 본 수강 신청 (실제 신청) ===
        else if (period == 1) {
            // 1. 중복 체크
            if (stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId)) {
                throw new IllegalStateException("이미 수강신청한 강의입니다.");
            }

            // 2. 정원 체크
            if (subject.getNumOfStudent() >= subject.getCapacity()) {
                throw new IllegalStateException("정원이 초과되었습니다.");
            }

            // 3. 최대 학점 체크
            List<StuSub> currentSemesterSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, subject.getSubYear(), subject.getSemester());

            int currentCredits = currentSemesterSubjects.stream()
                    .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                    .sum();

            if (currentCredits + subject.getGrades().intValue() > 18) {
                throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
            }

            // 4. 저장
            StuSub newEnrollment = new StuSub();
            newEnrollment.setStudent(student);
            newEnrollment.setSubject(subject);
            stuSubRepository.save(newEnrollment);

            // 5. 인원 증가
            subject.setNumOfStudent(subject.getNumOfStudent() + 1);
        }
    }
 
    // 시간표 중복 검증 로직
    private void validateTimeConflict(Long studentId, Subject targetSubject, int period) {
        // 비교할 기존 강의 목록 가져오기
        List<Subject> existingSubjects;

        if (period == 0) {
            // 기간 0: '장바구니'에 있는 과목들과 비교
            existingSubjects = preStuSubRepository.findByStudentId(studentId).stream()
                    .map(PreStuSub::getSubject)
                    .collect(Collectors.toList());
        } else {
            // 기간 1: 실제 '수강신청 완료'된 과목들과 비교
            existingSubjects = stuSubRepository.findByStudentId(studentId).stream()
                    .map(StuSub::getSubject)
                    .collect(Collectors.toList());
        }

        // 반복문으로 하나씩 시간 비교
        for (Subject existing : existingSubjects) {
            // 1. 요일이 같은지 확인
            if (existing.getSubDay().equals(targetSubject.getSubDay())) {
                
                // 2. 교시(시간)가 겹치는지 확인 (Overlap Logic)
                // (신청강의 시작 <= 기존강의 끝) AND (신청강의 끝 >= 기존강의 시작)
                boolean isOverlap = 
                    targetSubject.getStartTime() <= existing.getEndTime() && 
                    targetSubject.getEndTime() >= existing.getStartTime();

                if (isOverlap) {
                    throw new IllegalStateException(
                        String.format("시간표가 중복됩니다! \n기존: %s (%s %d~%d교시)", 
                        existing.getName(), existing.getSubDay(), existing.getStartTime(), existing.getEndTime())
                    );
                }
            }
        }
    }

    // [동작] 수강 취소 (기간별 분기)
    @Transactional
    public void cancel(Long studentId, Long subjectId) {
        int period = SugangController.SUGANG_PERIOD;

        if (period == 2) {
            throw new IllegalStateException("수강 취소 기간이 지났습니다.");
        }

        // === [기간 0] 예비 수강 취소 (장바구니 삭제) ===
        if (period == 0) {
            PreStuSub pre = preStuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId);
            if (pre == null) {
                throw new IllegalArgumentException("장바구니에 해당 과목이 없습니다.");
            }
            Subject subject = pre.getSubject();
            if (subject.getNumOfStudent() > 0) {
                subject.setNumOfStudent(subject.getNumOfStudent() - 1);
            }
            preStuSubRepository.delete(pre);
        }

        // === [기간 1] 본 수강 취소 (실제 삭제) ===
        else if (period == 1 || period == 2) {
            StuSub enrollment = stuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("수강 내역이 없습니다."));
            
            Subject subject = enrollment.getSubject();
            if (subject.getNumOfStudent() > 0) {
                subject.setNumOfStudent(subject.getNumOfStudent() - 1);
            }
            
            stuSubRepository.delete(enrollment);
        }
    }
    
    @Transactional
    public ResponseEntity<SyllabusDto> Syllabus(Long subjectId) {
    	return subjectRepository.findById(subjectId)
    			.map(SyllabusDto::fromEntity) 
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}