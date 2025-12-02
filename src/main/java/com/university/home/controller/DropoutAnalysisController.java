package com.university.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.service.DropoutAnalysisService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dropout")
@RequiredArgsConstructor
public class DropoutAnalysisController {

    private final DropoutAnalysisService analysisService;

    // 1. 분석 실행 (관리자/교수용 버튼) - 데이터가 많으면 시간이 좀 걸립니다.
    // POST /api/dropout/analyze
    @PostMapping("/analyze")
    public ResponseEntity<?> runAnalysis() {
        analysisService.analyzeAllStudents();
        return ResponseEntity.ok("전체 학생 중도이탈 위험 분석이 완료되었습니다.");
    }

    // 2. 결과 조회 등은 필요 시 Repository를 통해 Service에 메소드를 추가하여 호출
}