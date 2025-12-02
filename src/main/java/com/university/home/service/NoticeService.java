package com.university.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.NoticeFormDto;
import com.university.home.dto.NoticePageFormDto;
import com.university.home.entity.Notice;
import com.university.home.entity.NoticeFile;
import com.university.home.repository.NoticeFileRepository;
import com.university.home.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;

    // 공지사항 생성
    @Transactional
    public Notice createNotice(NoticeFormDto dto) {
        Notice notice = new Notice();
        notice.setCategory(dto.getCategory());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setViews(0L);
        notice.setCreatedTime(dto.getCreatedTime());

        notice = noticeRepository.save(notice);

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            NoticeFile file = new NoticeFile();
            file.setNotice(notice);
            file.setOriginFilename(dto.getOriginFilename());
            file.setUuidFilename(dto.getUuidFilename());
            noticeFileRepository.save(file);
        }

        return notice;
    }

    // 공지사항 검색 / 목록
    public List<Notice> getNotices(NoticePageFormDto dto) {
        if (dto.getKeyword() == null || dto.getKeyword().isEmpty()) {
            return noticeRepository.findAll();
        }

        if ("title".equals(dto.getType())) {
            return noticeRepository.findByTitleContaining(dto.getKeyword());
        } else {
            return noticeRepository.findByTitleContainingOrContentContaining(dto.getKeyword(), dto.getKeyword());
        }
    }

    // 공지사항 조회 (조회수 증가 포함)
    @Transactional
    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));

        if (notice.getViews() == null) notice.setViews(0L);
        noticeRepository.incrementViews(id);

        return notice;
    }

    // 공지사항 수정
    @Transactional
    public Notice updateNotice(Long id, NoticeFormDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));

        notice.setCategory(dto.getCategory());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        return noticeRepository.save(notice);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    // 최신 공지 5개
    public List<Notice> getLatest5Notices() {
        return noticeRepository.findTop5ByOrderByCreatedTimeDesc();
    }
}
