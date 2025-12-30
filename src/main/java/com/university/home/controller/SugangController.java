package com.university.home.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.service.CourseService; 

@RestController
@RequestMapping("/api/sugang") 
public class SugangController {

    @Autowired
    private CourseService courseService; 

    public static int SUGANG_PERIOD = 0;

    // 현재 기간 상태 조회
    @GetMapping("/period")
    public ResponseEntity<?> getSugangPeriod() {
        Map<String, Object> response = new HashMap<>();
        response.put("period", SUGANG_PERIOD);
        
        String message = "";
        switch(SUGANG_PERIOD) {
            case 0: message = "현재 예비 수강 신청 기간입니다."; break;
            case 1: message = "현재 수강 신청 기간입니다."; break;
            case 2: message = "이번 학기 수강 신청 기간이 종료되었습니다."; break;
        }
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    // 기간 상태 변경 API
    @PutMapping("/period")
    public ResponseEntity<?> updateSugangPeriod(@RequestParam("type") int type) {
        if (type < 0 || type > 2) return ResponseEntity.badRequest().body("잘못된 타입");

        courseService.updateSugangPeriod(type);

        String msg = "";
        if (type == 0) msg = "예비 수강신청 기간 시작";
        else if (type == 1) msg = "본 수강신청 기간 시작";
        else if (type == 2) msg = "수강신청 종료";

        return ResponseEntity.ok(msg);
    }

}