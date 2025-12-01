package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.NoticeFile;

import java.util.List;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {

    List<NoticeFile> findByNotice_Id(Long noticeId);
}
