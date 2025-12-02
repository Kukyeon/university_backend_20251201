package com.university.home.controller;

import java.util.List;



import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.university.home.dto.ScheduleFormDto;
import com.university.home.entity.Schedule;
import com.university.home.dto.PrincipalDto;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.ScheduleService;
import com.university.home.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final HttpSession session;
    private final ScheduleService scheduleService;

    @GetMapping("")
    public String scheduleList(Model model) {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        return "/schedule/schedule";
    }

    @PostMapping("/write")
    public String createSchedule(ScheduleFormDto dto) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        if (dto.getStartDay() == null) throw new CustomRestfullException("시작일을 입력해주세요", HttpStatus.BAD_REQUEST);
        if (dto.getEndDay() == null) throw new CustomRestfullException("종료일을 입력해주세요", HttpStatus.BAD_REQUEST);
        if (dto.getInformation() == null || dto.getInformation().isEmpty())
            throw new CustomRestfullException("내용을 입력해주세요", HttpStatus.BAD_REQUEST);

        scheduleService.createSchedule(principal.getId(), dto);
        return "redirect:/schedule";
    }

    @PostMapping("/update")
    public String updateSchedule(@RequestParam Long id, ScheduleFormDto dto) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        scheduleService.updateSchedule(id, dto);
        return "redirect:/schedule";
    }

    @GetMapping("/delete")
    public String deleteSchedule(@RequestParam Long id) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        scheduleService.deleteSchedule(id, principal.getId());
        return "redirect:/schedule";
    }
}
