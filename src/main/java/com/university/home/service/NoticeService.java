package com.university.home.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final String uploadPath = "D:\\university_backend_20251201\\upload\\";

    // 공지사항 생성
    @Transactional
    public Notice createNotice(NoticeFormDto dto) {
    	String imageUrl = saveFile(dto.getFile());
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategory(dto.getCategory());
        notice.setImageUrl(imageUrl); // URL만 저장
        notice.setViews(0L);
        notice.setCreatedTime(LocalDateTime.now());

        return noticeRepository.save(notice);
    }
    
    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // 1. 파일 이름 생성 (UUID를 사용하여 중복 방지)
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            // 확장자가 없는 경우 처리
            String extension = "";
            if (originalFilename.lastIndexOf(".") != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = uuid + extension;
            
            // 2. 파일 저장 (로컬 경로에 물리적으로 저장)
            Path filePath = Paths.get(uploadPath, savedFilename);
            Files.copy(file.getInputStream(), filePath);

            // 3. DB에 저장할 URL 생성 (클라이언트 접근용 URL)
            // Spring 설정에 따라 '/images/'로 정적 리소스를 제공한다고 가정
            return "/images/" + savedFilename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 중 오류가 발생했습니다.", e);
        }
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
        
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            String newImageUrl = saveFile(dto.getFile());
            notice.setImageUrl(newImageUrl); // 새 URL로 업데이트
        }
        
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategory(dto.getCategory());
       // notice.setImageUrl(dto.getImageUrl());

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
