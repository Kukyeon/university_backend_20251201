package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.university.home.entity.Subject;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // 과목 삭제
    void deleteById(Long id);

    // 특정 강의실, 요일, 연도, 학기 조건 조회
    @Query("SELECT s FROM Subject s WHERE s.room.id = :roomId AND s.subDay = :subDay AND s.subYear = :subYear AND s.semester = :semester")
    List<Subject> findByRoom_IdAndSubDayAndSubYearAndSemester(@Param("roomId") Long roomId,
                                                              @Param("subDay") String subDay,
                                                              @Param("subYear") Long subYear,
                                                              @Param("semester") Long semester);

    // 최근 강의 ID 조회
    @Query("SELECT s.id FROM Subject s ORDER BY s.id DESC")
    List<Long> findLatestId();

    // 교수 기준 연도-학기 조회
    @Query("SELECT DISTINCT s.subYear, s.semester FROM Subject s WHERE s.professor.id = :professorId")
    List<Object[]> findSemesterByProfessorId(@Param("professorId") Long professorId);

    // 교수 기준 해당 학기의 수업 조회
    List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

    // 연도-학기-개설학과-강의명 검색
    @Query("SELECT s FROM Subject s WHERE s.subYear = :subYear AND s.semester = :semester "
         + "AND (:deptId IS NULL OR s.department.id = :deptId) "
         + "AND (:name IS NULL OR s.name LIKE %:name%)")
    List<Subject> findBySemesterAndDeptAndName(@Param("subYear") Long subYear,
                                               @Param("semester") Long semester,
                                               @Param("deptId") Long deptId,
                                               @Param("name") String name);

    // 정원 이상/미만 조회
    @Query("SELECT s.id FROM Subject s WHERE s.capacity >= s.numOfStudent")
    List<Long> findIdByLessNumOfStudent();

    @Query("SELECT s.id FROM Subject s WHERE s.capacity < s.numOfStudent")
    List<Long> findIdByMoreNumOfStudent();
}
