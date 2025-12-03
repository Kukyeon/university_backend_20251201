package com.university.home.controller;

import com.university.home.dto.DropoutRiskResponseDto;
import com.university.home.service.DropoutDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DropoutDashboardController {

    private final DropoutDashboardService dashboardService;

    // [API] 교수님 대시보드 데이터 조회
    // 요청: GET http://localhost:8888/api/dashboard/risk-list?professorId=101
    @GetMapping("/risk-list")
    public ResponseEntity<List<DropoutRiskResponseDto>> getRiskList(@RequestParam Long professorId) {
        List<DropoutRiskResponseDto> list = dashboardService.getProfessorDashboard(professorId);
        return ResponseEntity.ok(list);
    }
}