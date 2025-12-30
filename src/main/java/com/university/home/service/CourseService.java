package com.university.home.service;

import com.university.home.component.SubjectSpecification;
import com.university.home.controller.SugangController; // ★ 기간 확인용 컨트롤러 import
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.PreStuSub; // ★ 예비수강 엔티티
import com.university.home.entity.StuSub;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.PreStuSubRepository;
import com.university.home.repository.StuSubDetailRepository;
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
    private final StuSubDetailRepository stuSubDetailRepository;

    // =================================================================================
    // 1. 조회 기능 (목록, 내역, AI 추천)
    // =================================================================================

    // [조회] 개설된 최신 학년 학기 강의 목록 가져오기 (기존 유지)
    @Transactional(readOnly = true)
    public Page<Subject> getAvailableCourses(Long studentId, int page, String type, String name, Long deptId, Long targetGrade) {
    	Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생 정보를 찾을 수 없습니다."));
    	Subject latest = subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
        Long subYear = (latest != null) ? latest.getSubYear() : 2025L;
        Long semester = (latest != null) ? latest.getSemester() : 1L;
        
        Long searchDeptId = (deptId != null) ? deptId : student.getDepartment().getId();
        Long searchGrade = (targetGrade != null) ? targetGrade : student.getGrade();
        
        System.out.println(">>> [DEBUG] 현재 조회 조건: 연도=" + subYear + ", 학기=" + semester);
        Specification<Subject> spec = Specification.where(SubjectSpecification.equalYearAndSemester(subYear, semester));
        spec = spec.and(SubjectSpecification.equalDeptId(searchDeptId));
        spec = spec.and(SubjectSpecification.equalTargetGrade(searchGrade));
        
        if (type != null && !type.isEmpty()) spec = spec.and(SubjectSpecification.equalType(type));
        if (name != null && !name.isEmpty()) spec = spec.and(SubjectSpecification.likeName(name));

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

        // 기간 0: 장바구니 목록 반환
        if (period == 0) {
            return preStuSubRepository.findByStudentId(studentId);
        } 
        // 기간 1(본수강) 혹은 2(종료): 실제 수강 목록 반환
        else {
            // (1) 현재 기준이 되는 최신 연도와 학기를 찾습니다.
            Subject latestSubject = getLatestSubjectInfo();

            if (latestSubject == null) {
                return Collections.emptyList(); // 데이터 없으면 빈 리스트
            }

            Long targetYear = latestSubject.getSubYear();
            Long targetSemester = latestSubject.getSemester();

            // (2) ★ 학생의 전체 기록이 아닌, "이번 학기" 내역만 조회합니다.
            // 그래야 과거 내역이 딸려오지 않고, 현재 신청한 것만 깔끔하게 나옵니다.
            return stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                    studentId, targetYear, targetSemester
            );
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
        
        Long subYear = 2023L; 
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
                throw new IllegalStateException("이미 수강목록에 담은 강의입니다.");
            }
//         // 2. 정원 체크
//            if (subject.getNumOfStudent() >= subject.getCapacity()) {
//                throw new IllegalStateException("장바구니 정원이 마감되었습니다.");
//            }
            
            
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
            //subject.setNumOfStudent(subject.getNumOfStudent() + 1);
            
         // ★ [핵심] 실제 인원(numOfStudent)은 건드리지 않고, 장바구니 카운트만 증가
            if (subject.getBasketCount() == null) subject.setBasketCount(0);
            subject.setBasketCount(subject.getBasketCount() + 1);    
        
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
            

            StuSubDetail detail = new StuSubDetail();
            detail.setStudent(student);
            detail.setSubject(subject);
           
            newEnrollment.setDetail(detail);
            
            stuSubRepository.save(newEnrollment);
            // 5. 인원 증가
            subject.setNumOfStudent(subject.getNumOfStudent() + 1);
        }
    }
 
    // 시간표 중복 검증 로직
    private void validateTimeConflict(Long studentId, Subject targetSubject, int period) {
        List<Subject> existingSubjects;

        if (period == 0) {
            // 기간 0: 장바구니 목록과 비교
            existingSubjects = preStuSubRepository.findByStudentId(studentId).stream()
                    .map(PreStuSub::getSubject)
                    .collect(Collectors.toList());
        } else {
            // 기간 1: ★ 과거 내역이 아닌 "이번 학기" 수강신청 내역과 비교해야 함!
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
                throw new IllegalArgumentException("신청목록에 해당 과목이 없습니다.");
            }
            Subject subject = pre.getSubject();
            
//            if (subject.getNumOfStudent() > 0) {
//                subject.setNumOfStudent(subject.getNumOfStudent() - 1);
//            }           
            // ★ 장바구니 카운트 감소
            if (subject.getBasketCount() > 0) {
                subject.setBasketCount(subject.getBasketCount() - 1);
            }
            preStuSubRepository.delete(pre);
        }

        // === [기간 1] 본 수강 취소 (실제 삭제) ===
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
        
        // 1. 기간 상태 변경
        SugangController.SUGANG_PERIOD = newPeriod;
        System.out.println(">>> 수강신청 기간이 변경되었습니다: " + newPeriod);

        // 2. 기간 1(본수강)이 될 때:
        // ★ 중요: 아무 작업도 하지 않습니다.
        // 기존 코드에 있던 'createStuSubByPreStuSub()' 같은 자동 복사 로직을 제거했으므로
        // 예비신청 목록이 자동으로 본수강으로 넘어가지 않습니다.
        // 학생이 직접 장바구니에서 "신청" 버튼을 눌러야 합니다.

        // 3. 기간 2(종료)가 될 때:
        if (newPeriod == 2) {
            // 장바구니 비우기 + 카운트 리셋
            resetBasketData();
            System.out.println(">>> [기간 종료] 장바구니 데이터가 초기화되었습니다.");
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

// // [핵심 로직] 장바구니 비우기 + 인원수 차감
//    private void resetBasketData() {
//        // 1. 현재 장바구니에 남아있는 모든 내역 조회
//        List<PreStuSub> remainingBaskets = preStuSubRepository.findAll();
//
//        // 2. 각 내역을 순회하며 과목 인원수 차감
////        for (PreStuSub basket : remainingBaskets) {
////            Subject subject = basket.getSubject();
////            
////            // 인원이 0보다 클 때만 감소 (음수 방지)
////            if (subject.getNumOfStudent() > 0) {
////                subject.setNumOfStudent(subject.getNumOfStudent() - 1);
////                // JPA의 Dirty Checking 기능으로 인해, 값만 변경하면 트랜잭션 종료 시 자동 update 쿼리가 나갑니다.
////            }
////        }
//
//        // 3. 장바구니 테이블 데이터 전체 삭제
//        preStuSubRepository.deleteAll();
//    }
    
    
