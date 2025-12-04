package com.university.home.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.university.home.entity.DataCollectionLog;
import com.university.home.repository.DataCollectionLogRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StuSubDetailRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIntegrationService {

    private final DataCollectionLogRepository logRepository;
    
    // [추가] 실제 데이터를 세기 위한 리포지토리들
    private final StudentRepository studentRepository;       // 학사 정보 (학생 수)
    private final StuSubRepository stuSubRepository;         // 수강 정보 (수강 신청 건수)
    private final StuSubDetailRepository stuSubDetailRepository; // 출결/과제 (LMS 데이터 건수)

    // [FUN-001] 데이터 수집 및 통합 자동화 파이프라인
    @Transactional
    public void syncAllData() {
        try {
            // 1. [학사정보시스템] 학생 데이터 수집
            // student_tb의 전체 행 개수를 셉니다.
            long studentCount = studentRepository.count(); 
            saveLog("학사정보시스템", (int) studentCount, "전체 학생 기본 정보 동기화 완료");

            // 2. [수강/성적시스템] 수강신청 내역 수집
            // stu_sub_tb의 전체 행 개수를 셉니다.
            long courseCount = stuSubRepository.count();
            saveLog("학사성적시스템", (int) courseCount, "수강신청 및 성적 이력 업데이트 완료");

            // 3. [LMS/전자출결] 출석 및 과제 데이터 수집
            // stu_sub_detail_tb (출석, 과제 점수 등 상세정보) 개수를 셉니다.
            long lmsCount = stuSubDetailRepository.count();
            saveLog("LMS/전자출결", (int) lmsCount, "출석/지각 및 과제 활동 로그 수집 완료");
            
            log.info("데이터 통합 완료 - 학생: {}, 수강: {}, 활동: {}", studentCount, courseCount, lmsCount);

        } catch (Exception e) {
            // 실패 시 실패 로그 남기기
            log.error("데이터 동기화 중 오류 발생", e);
            saveErrorLog("통합시스템", "데이터 동기화 중 오류 발생: " + e.getMessage());
        }
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

    private void saveErrorLog(String source, String msg) {
        DataCollectionLog log = DataCollectionLog.builder()
                .sourceSystem(source)
                .status("FAIL")
                .recordCount(0)
                .message(msg)
                .collectedAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
    
    @Transactional
    public void clearAllLogs() {
    	logRepository.deleteAll();
    	log.info("수집된 데이터 로그가 초기화되었습니다");
    		
    }
 }