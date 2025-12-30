package com.university.home.service;

import com.university.home.component.SubjectSpecification;
import com.university.home.controller.SugangController;
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.PreStuSub;
import com.university.home.entity.StuSub;
import com.university.home.entity.StuSubDetail;
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

import java.util.Collections;
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

    // 개설된 최신 학년 학기 강의 목록 가져오기
    @Transactional(readOnly = true)
    public Page<Subject> getAvailableCourses(Long studentId, int page, String type, String name, Long deptId, Long targetGrade) {
    	Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));
    	Subject latest = subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
        Long subYear = (latest != null) ? latest.getSubYear() : 2025L;
        Long semester = (latest != null) ? latest.getSemester() : 1L;
        
        Long searchDeptId = (deptId != null) ? deptId : student.getDepartment().getId();
        Long searchGrade = (targetGrade != null) ? targetGrade : student.getGrade();
        
        Specification<Subject> spec = Specification.where(SubjectSpecification.equalYearAndSemester(subYear, semester));
        spec = spec.and(SubjectSpecification.equalDeptId(searchDeptId));
        spec = spec.and(SubjectSpecification.equalTargetGrade(searchGrade));
        
        if (type != null && !type.isEmpty()) spec = spec.and(SubjectSpecification.equalType(type));
        if (name != null && !name.isEmpty()) spec = spec.and(SubjectSpecification.likeName(name));

        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.ASC, "name"));

        return subjectRepository.findAll(spec, pageable);
    }
    
    // 모든 강좌 조회 (년도, 학기 상관없이 전체 조회)
    @Transactional(readOnly = true)
    public Page<Subject> getAllCourses(int page, String type, String name, Long deptId, Long year, Long semester) {
        
        Specification<Subject> spec = (root, query, criteriaBuilder) -> null;

        if (type != null && !type.isEmpty()) {
            spec = spec.and(SubjectSpecification.equalType(type));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and(SubjectSpecification.likeName(name));
        }
        if (deptId != null) {
            spec = spec.and(SubjectSpecification.equalDeptId(deptId));
        }
        
        if (year != null) {
            spec = spec.and(SubjectSpecification.equalSubYear(year));
        }
        if (semester != null) {
            spec = spec.and(SubjectSpecification.equalSemester(semester));
        }

        Pageable pageable = PageRequest.of(page, 20, 
                Sort.by(Sort.Direction.DESC, "subYear")
                    .and(Sort.by(Sort.Direction.DESC, "semester"))
                    .and(Sort.by(Sort.Direction.ASC, "name"))
        );

        return subjectRepository.findAll(spec, pageable);
    }

    // 나의 수강 내역 가져오기
    @Transactional(readOnly = true)
    public List<?> getMyCourseHistory(Long studentId) {
        int period = SugangController.SUGANG_PERIOD;

        if (period == 0) {
            return preStuSubRepository.findByStudentId(studentId);
        } 
        else {
            Subject latestSubject = getLatestSubjectInfo();

            if (latestSubject == null) {
                return Collections.emptyList(); // 데이터 없으면 빈 리스트
            }

            Long targetYear = latestSubject.getSubYear();
            Long targetSemester = latestSubject.getSemester();

            return stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, targetYear, targetSemester
            );
        }
    }

    // [AI] 강의 추천 기능
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

    // 수강신청 / 취소 기능
    private Subject getLatestSubjectInfo() {
        return subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
    }
    
    //  수강 신청 (기간별 분기)
    @Transactional
    public void enroll(Long studentId, Long subjectId) {
        int period = SugangController.SUGANG_PERIOD;

        if (period == 2) {
            throw new IllegalStateException("지금은 수강신청 기간이 아닙니다.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보가 없습니다."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("강의 정보가 없습니다."));

        validateTimeConflict(studentId, subject, period);
        if (period == 0) {
            if (preStuSubRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
                throw new IllegalStateException("이미 수강목록에 담은 강의입니다.");
            }
            List<StuSub> currentSemesterSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, subject.getSubYear(), subject.getSemester());

            int currentCredits = currentSemesterSubjects.stream()
                    .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                    .sum();

            if (currentCredits + subject.getGrades().intValue() > 18) {
                throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
            }
            
            PreStuSub pre = new PreStuSub();
            pre.setStudent(student);
            pre.setSubject(subject);
            preStuSubRepository.save(pre);
            
            if (subject.getBasketCount() == null) subject.setBasketCount(0);
            subject.setBasketCount(subject.getBasketCount() + 1);    
        
        }

        else if (period == 1) {
            if (stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId)) {
                throw new IllegalStateException("이미 수강신청한 강의입니다.");
            }

            if (subject.getNumOfStudent() >= subject.getCapacity()) {
                throw new IllegalStateException("정원이 초과되었습니다.");
            }

            List<StuSub> currentSemesterSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, subject.getSubYear(), subject.getSemester());

            int currentCredits = currentSemesterSubjects.stream()
                    .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                    .sum();

            if (currentCredits + subject.getGrades().intValue() > 18) {
                throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
            }

            StuSub newEnrollment = new StuSub();
            newEnrollment.setStudent(student);
            newEnrollment.setSubject(subject);
            

            StuSubDetail detail = new StuSubDetail();
            detail.setStudent(student);
            detail.setSubject(subject);
           
            newEnrollment.setDetail(detail);
            
            stuSubRepository.save(newEnrollment);
            subject.setNumOfStudent(subject.getNumOfStudent() + 1);
        }
    }
 
    // 시간표 중복 검증 로직
    private void validateTimeConflict(Long studentId, Subject targetSubject, int period) {
        List<Subject> existingSubjects;

        if (period == 0) {
            existingSubjects = preStuSubRepository.findByStudentId(studentId).stream()
                    .map(PreStuSub::getSubject)
                    .collect(Collectors.toList());
        } else {
            existingSubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, targetSubject.getSubYear(), targetSubject.getSemester()
            ).stream()
             .map(StuSub::getSubject)
             .collect(Collectors.toList());
        }

        for (Subject existing : existingSubjects) {
            if (existing.getSubDay().equals(targetSubject.getSubDay())) {
                boolean isOverlap = 
                    targetSubject.getStartTime() <= existing.getEndTime() && 
                    targetSubject.getEndTime() >= existing.getStartTime();

                if (isOverlap) {
                    throw new IllegalStateException(
                        String.format("시간표가 중복됩니다. \n기존: %s (%s %d~%d교시)", 
                        existing.getName(), existing.getSubDay(), existing.getStartTime(), existing.getEndTime())
                    );
                }
            }
        }
    }

    // 수강 취소 (기간별 분기)
    @Transactional
    public void cancel(Long studentId, Long subjectId) {
        int period = SugangController.SUGANG_PERIOD;

        if (period == 2) {
            throw new IllegalStateException("수강 취소 기간이 지났습니다.");
        }

        if (period == 0) {
            PreStuSub pre = preStuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId);
            if (pre == null) {
                throw new IllegalArgumentException("신청목록에 해당 과목이 없습니다.");
            }
            Subject subject = pre.getSubject();
            
            if (subject.getBasketCount() > 0) {
                subject.setBasketCount(subject.getBasketCount() - 1);
            }
            preStuSubRepository.delete(pre);
        }

        else if (period == 1 || period == 2) {
            StuSub enrollment = stuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("수강신청 내역이 없습니다."));
            
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
    
    // 기간 변경 및 장바구니 초기화 로직
    @Transactional
    public void updateSugangPeriod(int newPeriod) {
        
        SugangController.SUGANG_PERIOD = newPeriod;

        if (newPeriod == 2) {
            resetBasketData();
        }
    }

    private void resetBasketData() {
        preStuSubRepository.deleteAll();
        List<Subject> allSubjects = subjectRepository.findAll();
        for (Subject s : allSubjects) {
            s.setBasketCount(0);
        }
    }
}
    
    
