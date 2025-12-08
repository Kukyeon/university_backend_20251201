package com.university.home.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.entity.Notice;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 검색: 제목+내용
    
    Page<Notice> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Notice> findByTitleContaining(String title, Pageable pageable);
    @Modifying
    
    // 최신글 5개
    List<Notice> findTop5ByOrderByCreatedTimeDesc();

    // 조회수 증가
    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.views = n.views + 1 WHERE n.id = :id")
    void incrementViews(@Param("id") Long id);
}
