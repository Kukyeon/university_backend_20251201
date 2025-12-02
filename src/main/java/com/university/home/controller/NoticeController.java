package com.university.home.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.university.home.dto.NoticeFormDto;
import com.university.home.dto.NoticePageFormDto;
import com.university.home.entity.Notice;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록
    @GetMapping("")
    public String noticeList(Model model,
                             @RequestParam(defaultValue = "select") String crud,
                             @RequestParam(defaultValue = "0") Integer page,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String type) {

        model.addAttribute("crud", crud);

        NoticePageFormDto dto = new NoticePageFormDto();
        dto.setPage(page);
        dto.setKeyword(keyword);
        dto.setType(type);

        List<Notice> noticeList = noticeService.getNotices(dto);
        model.addAttribute("listCount", Math.ceil(noticeList.size() / 10.0));
        model.addAttribute("noticeList", noticeList.isEmpty() ? null : noticeList);

        return "/board/notice";
    }

    // 공지사항 작성
    @PostMapping("/write")
    public String createNotice(NoticeFormDto dto) {
        noticeService.createNotice(dto);
        return "redirect:/notice";
    }

    // 공지사항 읽기
    @GetMapping("/read")
    public String readNotice(Model model, @RequestParam Long id) {
        model.addAttribute("crud", "read");
        model.addAttribute("id", id);

        Notice notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);

        // 줄바꿈 처리
        notice.setContent(notice.getContent().replace("\r\n", "<br>"));

        return "/board/notice";
    }

    // 공지사항 삭제
    @GetMapping("/delete")
    public String deleteNotice(@RequestParam Long id) {
        noticeService.deleteNotice(id);
        return "redirect:/notice";
    }
}
