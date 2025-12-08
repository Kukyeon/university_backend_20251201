package com.university.home;

import com.university.home.entity.CounselingRecord;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RecordSpecification {

    // 학생 이름 부분 일치 검색 (LIKE %name%)
    public static Specification<CounselingRecord> hasStudentName(String studentName) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.like(root.get("studentName"), "%" + studentName + "%");
    }

    // 상담 날짜 검색 (특정 날짜의 00:00:00 ~ 23:59:59 범위)
    public static Specification<CounselingRecord> hasConsultationDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.between(root.get("consultationDate"), startOfDay, endOfDay);
    }

    // 키워드/내용 검색 (notes 또는 keywords 필드 중 하나라도 포함)
    public static Specification<CounselingRecord> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            
            // 1. notes 필드 검색 조건
            jakarta.persistence.criteria.Predicate notesMatch = 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), likeKeyword);
            
            // 2. keywords 필드 검색 조건
            jakarta.persistence.criteria.Predicate keywordsMatch = 
                criteriaBuilder.like(criteriaBuilder.lower(root.get("keywords")), likeKeyword);
            
            // 두 조건 중 하나라도 만족하면 검색
            return criteriaBuilder.or(notesMatch, keywordsMatch);
        };
    }
}