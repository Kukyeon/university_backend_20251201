package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import com.university.entity.Notice;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 검색: 제목+내용
    List<Notice> findByTitleContainingOrContentContaining(String title, String content);

    // 검색: 제목만
    List<Notice> findByTitleContaining(String title);

    // 최신글 5개
    List<Notice> findTop5ByOrderByCreateTimeDesc();

    // 조회수 증가
    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.views = n.views + 1 WHERE n.id = :id")
    void incrementViews(Long id);
}
