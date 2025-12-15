package com.university.home.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.university.home.dto.ScheduleFormDto;
import com.university.home.entity.Schedule;
import com.university.home.dto.PrincipalDto;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.ScheduleRepository;
import com.university.home.service.ScheduleService;
import com.university.home.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;

    private final HttpSession session;
    private final ScheduleService scheduleService;


    //  일정 등록
    @PostMapping("/write")
    
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleFormDto dto) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        if (dto.getStartDay() == null) throw new CustomRestfullException("시작일을 입력해주세요", HttpStatus.BAD_REQUEST);
        if (dto.getEndDay() == null) throw new CustomRestfullException("종료일을 입력해주세요", HttpStatus.BAD_REQUEST);
        if (dto.getInformation() == null || dto.getInformation().isEmpty())
            throw new CustomRestfullException("내용을 입력해주세요", HttpStatus.BAD_REQUEST);

        Schedule createdSchedule = scheduleService.createSchedule(principal.getId(), dto);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    // 일정 목록 조회
    @GetMapping("")
    // ⭐️ String 대신 List<Schedule>를 JSON으로 반환합니다.
    public ResponseEntity<List<Schedule>> scheduleList() { 
        List<Schedule> schedules = scheduleService.getAllSchedules();
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }
    
    // 일정 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleDetail(@PathVariable("id") Long id) {
        Schedule schedule = scheduleService.getSchedule(id);
        return new ResponseEntity<>(schedule, HttpStatus.OK);
    }


    //일정 수정
    
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable("id") Long id, @RequestBody ScheduleFormDto dto) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        Schedule updatedSchedule = scheduleService.updateSchedule(id, dto);
        return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
    }

    // 일정 삭제
   
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable("id") Long id) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        scheduleService.deleteSchedule(id, principal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestSchedules() {
        Pageable top5 = PageRequest.of(0, 5, Sort.by("startDay"));
        
        List<ScheduleFormDto> latest = scheduleRepository.findAll(top5)
                                                         .stream()
                                                         .map(schedule -> {
                                                             ScheduleFormDto dto = new ScheduleFormDto();
                                                             dto.setId(schedule.getId());
                                                             dto.setInformation(schedule.getInformation());
                                                             dto.setStartDay(schedule.getStartDay());
                                                             dto.setEndDay(schedule.getEndDay());
                                                             dto.setInformation(schedule.getInformation());
                                                             return dto;
                                                         })
                                                         .collect(Collectors.toList());
        return ResponseEntity.ok(latest);
    }

}