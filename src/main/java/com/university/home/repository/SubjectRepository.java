package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.home.entity.Subject;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // 과목 삭제
    void deleteById(Long id);

    // 특정 강의실, 요일, 연도, 학기 조건 조회
    List<Subject> findByRoom_IdAndSubDayAndSubYearAndSemester(Long roomId, String subDay, Long subYear, Long semester);

    // 최근 강의 ID 조회 (최신 순으로 정렬)
    List<Subject> findAllByOrderByIdDesc();

    // 교수 기준 연도-학기 조회 (distinct 불가능, Object[]대신 엔티티 사용 가능)
    List<Subject> findDistinctByProfessor_IdOrderBySubYearDescSemesterDesc(Long professorId);

    // 교수 기준 해당 학기의 수업 조회
    List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

    // 연도-학기-개설학과-강의명 검색
    List<Subject> findBySubYearAndSemesterAndDepartment_IdAndNameContaining(Long subYear, Long semester, Long deptId, String name);

    // 정원 이상/미만 조회
    List<Subject> findByCapacityGreaterThanEqual(Long num);
    List<Subject> findByCapacityLessThan(Long num);
}
