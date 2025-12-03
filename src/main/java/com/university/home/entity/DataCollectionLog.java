package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_collection_log_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataCollectionLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceSystem; // 예: "LMS", "전자출결", "학사시스템"
    private String status;       // "SUCCESS", "FAIL"
    private Integer recordCount; // 가져온 데이터 수 (예: 500)
    private String message;      // 비고 (예: "수강내역 업데이트 완료")
    
    @Column(name = "collected_at")
    private LocalDateTime collectedAt;
}