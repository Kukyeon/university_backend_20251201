package com.university.home.service;

import com.university.home.entity.StuSub;
import com.university.home.repository.StuSubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final StuSubRepository stuSubRepository;

    /**
     * [기능 1] 학생의 '총 이수 학점' 계산 (졸업 요건 확인용)
     */
    @Transactional(readOnly = true)
    public Integer calculateTotalCredits(Long studentId) {
        // 1. DB에서 학생의 모든 수강 내역 가져오기 (SQL 아님!)
        List<StuSub> list = stuSubRepository.findByStudentId(studentId);

        // 2. 자바 반복문으로 더하기
        int total = 0;
        for (StuSub sub : list) {
            if (sub.getCompleteGrade() != null) {
                total += sub.getCompleteGrade();
            }
        }
        return total;
    }

    /**
     * [기능 2] 학생의 '평균 학점(GPA)' 계산 (위험 분석용)
     */
    @Transactional(readOnly = true)
    public Double calculateAverageGrade(Long studentId) {
        List<StuSub> list = stuSubRepository.findByStudentId(studentId);

        if (list.isEmpty()) return 0.0;

        double sumScore = 0.0;
        int count = 0;

        for (StuSub sub : list) {
            // 등급(A+, B0)을 점수(4.5, 3.0)로 변환
            double score = convertGradeToScore(sub.getGrade());
            if (score > 0) { // F학점이나 성적 없는 건 제외하고 계산할 경우
                sumScore += score;
                count++;
            }
        }

        if (count == 0) return 0.0;
        
        // 소수점 둘째 자리까지만 예쁘게 잘라서 반환
        return Math.round((sumScore / count) * 100) / 100.0;
    }

    // [보조] 등급 문자열 -> 점수 변환기 (if문 노가다)
    private double convertGradeToScore(String grade) {
        if (grade == null) return 0.0;
        switch (grade) {
            case "A+": return 4.5;
            case "A0": return 4.0;
            case "B+": return 3.5;
            case "B0": return 3.0;
            case "C+": return 2.5;
            case "C0": return 2.0;
            case "D+": return 1.5;
            case "D0": return 1.0;
            case "F":  return 0.0; // F는 0점 처리 (필요시 로직 수정)
            default:   return 0.0; // P/F 과목 등은 0점 처리
        }
    }
}