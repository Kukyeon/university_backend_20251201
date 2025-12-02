package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.university.home.entity.Subject;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // 과목 삭제
    void deleteById(Integer id);

    // 특정 강의실, 요일, 연도, 학기 조건 조회
    @Query("SELECT s FROM Subject s WHERE s.roomId = :roomId AND s.subDay = :subDay AND s.subYear = :subYear AND s.semester = :semester")
    List<Subject> findByRoomIdAndSubDayAndSubYearAndSemester(@Param("roomId") String roomId,
                                                             @Param("subDay") String subDay,
                                                             @Param("subYear") Integer subYear,
                                                             @Param("semester") Integer semester);

    // 최근 강의 ID 조회
    @Query("SELECT s.id FROM Subject s ORDER BY s.id DESC")
    List<Integer> findLatestId();

    // 교수 기준 연도-학기 조회
    @Query("SELECT DISTINCT s.subYear, s.semester FROM Subject s WHERE s.professorId = :professorId")
    List<Object[]> findSemesterByProfessorId(@Param("professorId") Integer professorId);

    // 교수 기준 해당 학기의 수업 조회
    List<Subject> findByProfessorIdAndSubYearAndSemester(Integer professorId, Integer subYear, Integer semester);

    // 연도-학기-개설학과-강의명 검색
    @Query("SELECT s FROM Subject s WHERE s.subYear = :subYear AND s.semester = :semester "
         + "AND (:deptId IS NULL OR s.deptId = :deptId) "
         + "AND (:name IS NULL OR s.name LIKE %:name%)")
    List<Subject> findBySemesterAndDeptAndName(@Param("subYear") Integer subYear,
                                               @Param("semester") Integer semester,
                                               @Param("deptId") Integer deptId,
                                               @Param("name") String name);

    // 정원 이상/미만 조회
    @Query("SELECT s.id FROM Subject s WHERE s.capacity >= s.numOfStudent")
    List<Integer> findIdByLessNumOfStudent();

    @Query("SELECT s.id FROM Subject s WHERE s.capacity < s.numOfStudent")
    List<Integer> findIdByMoreNumOfStudent();
}
