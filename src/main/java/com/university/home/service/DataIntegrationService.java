package com.university.home.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.university.home.entity.DataCollectionLog;
import com.university.home.repository.DataCollectionLogRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataIntegrationService {

    private final DataCollectionLogRepository logRepository;

    // [FUN-001] 데이터 수집 및 통합 자동화 파이프라인
    @Transactional
    public void syncAllData() {
        // 1. 학사 정보 수집 (가정)
        saveLog("학사정보시스템", 120, "학생 및 성적 데이터 동기화 완료");

        // 2. LMS 데이터 수집 (가정)
        saveLog("LMS", 50, "온라인 강의 수강 이력 수집 완료");

        // 3. 전자출결 데이터 수집 (가정)
        saveLog("전자출결시스템", 30, "결석/지각 데이터 업데이트");
    }

    private void saveLog(String source, int count, String msg) {
        DataCollectionLog log = DataCollectionLog.builder()
                .sourceSystem(source)
                .status("SUCCESS")
                .recordCount(count)
                .message(msg)
                .collectedAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}
