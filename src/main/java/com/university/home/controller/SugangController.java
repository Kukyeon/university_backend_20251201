package com.university.home.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.service.StuSubService;



// React와 통신하기 위해 RestController 사용
@RestController
@RequestMapping("/api/sugang") 
public class SugangController {

    @Autowired
    private StuSubService stuSubService;

    // 서버 메모리에 상태 저장 (0: 예비, 1: 본수강, 2: 종료)
    // 실제 서비스에선 DB의 'SystemSettings' 같은 테이블에서 관리하는 것을 권장합니다.
    public static int SUGANG_PERIOD = 0;

    // 1. 현재 기간 상태 조회 API
    @GetMapping("/period")
    public ResponseEntity<?> getSugangPeriod() {
        Map<String, Object> response = new HashMap<>();
        response.put("period", SUGANG_PERIOD);
        
        // 상태에 따른 메시지도 같이 보내주면 프론트에서 편함
        String message = "";
        switch(SUGANG_PERIOD) {
            case 0: message = "현재 예비 수강 신청 기간입니다."; break;
            case 1: message = "현재 수강 신청 기간입니다."; break;
            case 2: message = "이번 학기 수강 신청 기간이 종료되었습니다."; break;
        }
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    // 2. 기간 상태 변경 API (PUT 메서드 사용)
    @PutMapping("/period")
    public ResponseEntity<?> updateSugangPeriod(@RequestParam("type") int type) {
        
        if (type == 1) {
            // 예비 -> 본 수강 기간으로 변경
            SUGANG_PERIOD = 1;
            // 예비 신청 내역을 본 신청 내역으로 이관하는 핵심 로직 실행
            stuSubService.createStuSubByPreStuSub();
            return ResponseEntity.ok("수강 신청 기간이 시작되었습니다. (예비 신청 이관 완료)");
            
        } else if (type == 2) {
            // 수강 -> 종료로 변경
            SUGANG_PERIOD = 2;
            return ResponseEntity.ok("수강 신청 기간이 종료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청입니다.");
        }
    }
}