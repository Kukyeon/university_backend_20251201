package com.university.home.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import com.university.home.entity.Subject;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    // 과목 삭제
    void deleteById(Long id);

    // 특정 강의실, 요일, 연도, 학기 조건 조회
    List<Subject> findByRoom_IdAndSubDayAndSubYearAndSemester(String roomId, String subDay, Long subYear, Long semester);

    // 최근 강의 ID 조회 (최신 순으로 정렬)
    List<Subject> findAllByOrderByIdDesc();

    // 교수 기준 연도-학기 조회 (distinct 불가능, Object[]대신 엔티티 사용 가능)
    List<Subject> findDistinctByProfessor_IdOrderBySubYearDescSemesterDesc(Long professorId);

    // 교수 기준 해당 학기의 수업 조회
    List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

    List<Subject> findByProfessor_Id(Long professorId);
    
    // 연도-학기-개설학과-강의명 검색
    List<Subject> findBySubYearAndSemesterAndDepartment_IdAndNameContaining(Long subYear, Long semester, Long deptId, String name);

    // 정원 이상/미만 조회
    List<Subject> findByCapacityGreaterThanEqual(Long num);
    List<Subject> findByCapacityLessThan(Long num);
    
    // 해당년도, 학기의 모든 강좌 조회
    List<Subject> findBySubYearAndSemester(Long subYear, Long semester);
    
    Optional<Subject> findTopByOrderBySubYearDescSemesterDesc();
    
    // 페이징 적용된 버전
    // 메서드 이름 뒤에 Pageable 파라미터만 추가하면 JPA가 알아서 페이징 쿼리를 날립니다.
    @EntityGraph(attributePaths = {"professor", "department", "department.college"})
    Page<Subject> findBySubYearAndSemester(Long subYear, Long semester, Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"professor", "department", "department.college"})
    Page<Subject> findAll(@Nullable Specification<Subject> spec, Pageable pageable);
    
    // (학기 자동감지 메서드 유지)
    // Optional<Subject> findTopByOrderBySubYearDescSemesterDesc();
 
}
