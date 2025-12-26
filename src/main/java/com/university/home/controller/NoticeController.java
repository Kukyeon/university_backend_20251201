package com.university.home.controller;


import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.NoticeFormDto;
import com.university.home.entity.Notice;
import com.university.home.service.NoticeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지 리스트 조회
    @GetMapping("/list")
    public Page<Notice> getNoticeList(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "keyword" ,defaultValue = "") String keyword,
            @RequestParam(name = "searchType",defaultValue = "title") String searchType
    ) {
        return noticeService.getNoticeList(page, keyword, searchType);
    }

    // 공지 상세 조회
    @GetMapping("/read/{id}")
    public Notice getNoticeDetail(@PathVariable("id") Long id) {
        return noticeService.getNoticeById(id);
    }
    
    //공지사항 조회수 증가
    @PostMapping("/views/{id}")
    public ResponseEntity<Void> incrementNoticeViews(@PathVariable("id") Long id){
    	noticeService.incrementViews(id);
    	return ResponseEntity.ok().build();
    }

    // 공지 등록 (파일 포함)
    @PostMapping("/write")
    public Notice writeNotice(@ModelAttribute NoticeFormDto dto) throws IOException {
        return noticeService.createNotice(dto);
    }

    // 공지 수정
    @PostMapping("/update/{id}")
    public Notice updateNotice(@PathVariable("id") Long id,  @ModelAttribute NoticeFormDto dto) throws IOException {
        return noticeService.updateNotice(id, dto);
    }

    // 공지 삭제
    @DeleteMapping("/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return "deleted";
    }
    // 공지 마지막 5개(홈화면용)
    @GetMapping("/latest")
    public ResponseEntity<?> latestList(){
    	List<Notice> latestList = noticeService.getLatest5Notices();
    	
    	return ResponseEntity.ok(latestList);
    }
}
