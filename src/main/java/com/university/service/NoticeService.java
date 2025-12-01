package com.university.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.dto.NoticeFormDto;
import com.university.dto.NoticePageFormDto;
import com.university.entity.Notice;
import com.university.entity.NoticeFile;
import com.university.repository.NoticeFileRepository;
import com.university.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;

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

    public List<Notice> getNotices(NoticePageFormDto dto) {
        if (dto.getKeyword() == null) {
            return noticeRepository.findAll();
        }
        if ("title".equals(dto.getType())) {
            return noticeRepository.findByTitleContaining(dto.getKeyword());
        } else {
            return noticeRepository.findByTitleContainingOrContentContaining(dto.getKeyword(), dto.getKeyword());
        }
    }

    @Transactional
    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));
        noticeRepository.incrementViews(id); // 조회수 증가
        return notice;
    }

    @Transactional
    public Notice updateNotice(Long id, NoticeFormDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));
        notice.setCategory(dto.getCategory());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    public List<Notice> getLatest5Notices() {
        return noticeRepository.findTop5ByOrderByCreateTimeDesc();
    }
}
