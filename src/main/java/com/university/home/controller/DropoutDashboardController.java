package com.university.home.controller;

import com.university.home.dto.DropoutRiskResponseDto;
import com.university.home.repository.DropoutRiskRepository;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.DropoutDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DropoutDashboardController {

    private final DropoutDashboardService dashboardService;
    private final DropoutRiskRepository dropoutRiskRepository;
    //교수님 대시보드 데이터 조회
    @GetMapping("/risk-list")
    public ResponseEntity<List<DropoutRiskResponseDto>> getRiskList(@AuthenticationPrincipal CustomUserDetails loginUser
    		) {
    	Long professorId = loginUser.getUser().getId();
        List<DropoutRiskResponseDto> list = dashboardService.getProfessorDashboard(professorId);
        return ResponseEntity.ok(list);
    }
    // 학생 삭제
    @DeleteMapping("/risk/{id}")
    public ResponseEntity<String> deleteRisk(@PathVariable("id") Long id) {
        dropoutRiskRepository.deleteById(id);
        return ResponseEntity.ok("삭제되었습니다.");
    }
    
    // 관리자용 전체 리스트
    @GetMapping("/admin/risk-list")
    public ResponseEntity<List<DropoutRiskResponseDto>> getAllRiskList() {
        return ResponseEntity.ok(dashboardService.getAllRiskStudents());
    }
}