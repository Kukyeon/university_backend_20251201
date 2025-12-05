package com.university.home.controller;

import com.university.home.entity.DataCollectionLog;
import com.university.home.repository.DataCollectionLogRepository;
import com.university.home.service.DataIntegrationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final DataIntegrationService integrationService;
    private final DataCollectionLogRepository logRepository;

    // [API] 외부 데이터 수집/동기화 실행
    // POST http://localhost:8888/api/system/sync
    @PostMapping("/sync")
    public ResponseEntity<String> syncData() {
        integrationService.syncAllData();
        return ResponseEntity.ok("데이터 수집 및 통합이 완료되었습니다.");
    }
    
    @GetMapping("/logs")
    public ResponseEntity<List<DataCollectionLog>> getLogs() {
        // 모든 로그를 최신순으로 가져오기 (Repository에 findAllByOrderByCollectedAtDesc 필요)
        // 없으면 그냥 findAll() 써도 됩니다.
    	return ResponseEntity.ok(logRepository.findAll());
    }
    
    @DeleteMapping("/logs")
    public ResponseEntity<String> clearLogs() {
    	integrationService.clearAllLogs();
    	return ResponseEntity.ok("로그데이터가 삭제되었습니다");
    }
}