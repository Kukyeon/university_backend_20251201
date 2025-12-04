package com.university.home.controller;

import com.university.home.service.DataIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final DataIntegrationService integrationService;

    // [API] 외부 데이터 수집/동기화 실행
    // POST http://localhost:8888/api/system/sync
    @PostMapping("/sync")
    public ResponseEntity<String> syncData() {
        integrationService.syncAllData();
        return ResponseEntity.ok("데이터 수집 및 통합이 완료되었습니다.");
    }
}