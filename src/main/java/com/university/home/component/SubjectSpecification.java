package com.university.home.component;

import com.university.home.entity.Subject;
import org.springframework.data.jpa.domain.Specification;

public class SubjectSpecification {

    // 1. [필수] 연도와 학기 일치 (WHERE sub_year = ? AND semester = ?)
    public static Specification<Subject> equalYearAndSemester(Long year, Long semester) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("subYear"), year),
                cb.equal(root.get("semester"), semester)
        );
    }
   
    // 2. [선택] 강의명 검색 (WHERE name LIKE %?%)
    public static Specification<Subject> likeName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) return null; // 검색어 없으면 조건 무시
            return cb.like(root.get("name"), "%" + name + "%");
        };
    }

    // 3. [선택] 강의구분 검색 (WHERE type = ?)
    public static Specification<Subject> equalType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.trim().isEmpty() || "전체".equals(type)) return null;
            return cb.equal(root.get("type"), type);
        };
    }

    // 4. [선택] 학과 검색 (WHERE department_id = ?)
    public static Specification<Subject> equalDeptId(Long deptId) {
        return (root, query, cb) -> {
            if (deptId == null || deptId <= 0) return null; // 전체(-1 또는 null)면 무시
            // Subject 엔티티 안의 department 객체의 id를 비교
            return cb.equal(root.get("department").get("id"), deptId);
        };
    }
    
 // 2. [신규/선택] 연도만 검색 (예: 2024년 전체)
    public static Specification<Subject> equalSubYear(Long year) {
        return (root, query, cb) -> {
            if (year == null || year == 0) return null;
            return cb.equal(root.get("subYear"), year);
        };
    }

    // 3. [신규/선택] 학기만 검색 (예: 모든 연도의 1학기)
    public static Specification<Subject> equalSemester(Long semester) {
        return (root, query, cb) -> {
            if (semester == null || semester == 0) return null;
            return cb.equal(root.get("semester"), semester);
        };
    }
}