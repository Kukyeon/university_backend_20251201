package com.university.home.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("")
    public String noticeList(Model model, @RequestParam(defaultValue = "select") String crud) {
        model.addAttribute("crud", crud);
        NoticePageFormDto dto = new NoticePageFormDto();
        dto.setPage(0);

        List<Notice> noticeList = noticeService.getNotices(dto);
        model.addAttribute("listCount", Math.ceil(noticeList.size() / 10.0));
        model.addAttribute("noticeList", noticeList.isEmpty() ? null : noticeList);
        return "/board/notice";
    }

    @PostMapping("/write")
    public String createNotice(NoticeFormDto dto) {
        noticeService.createNotice(dto);
        return "redirect:/notice";
    }

    @GetMapping("/read")
    public String readNotice(Model model, @RequestParam Long id) {
        model.addAttribute("crud", "read");
        model.addAttribute("id", id);

        Notice notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);
        notice.setContent(notice.getContent().replace("\r\n", "<br>"));

        return "/board/notice";
    }

    @GetMapping("/delete")
    public String deleteNotice(@RequestParam Long id) {
        noticeService.deleteNotice(id);
        return "redirect:/notice";
    }
}
