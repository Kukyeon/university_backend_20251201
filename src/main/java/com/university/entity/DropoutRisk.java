package com.university.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropoutRisk {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id")
	private Student student;
	
	//위험도 점수 (0~100점, 높을수록 위험
	@Column(name = "risk_score")
	private Double riskScore;
	
	// 위험 단계 (정상, 주의, 경고, 위험)
    @Column(name = "risk_level")
    private String riskLevel;

    // 위험 원인 (예: "성적 저조", "장기 결석", "휴학 반복") - AI 분석 코멘트
    @Column(columnDefinition = "TEXT")
    private String reason;

    // 분석 일자 (최신 분석 날짜 확인용)
    @Column(name = "analyzed_date")
    private LocalDate analyzedDate;
	
}
