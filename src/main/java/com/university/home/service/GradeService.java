package com.university.home.service;

import com.university.home.entity.StuSub;
import com.university.home.entity.Subject; // 추가
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.SubjectRepository; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository; // ★ [추가] 학기 정보 조회를 위해 필요

    /**
     * [기능 1] 학생의 '총 이수 학점' (기존 유지 - 졸업요건은 누적이어야 하므로)
     */
    @Transactional(readOnly = true)
    public Integer calculateTotalCredits(Long studentId) {
        List<StuSub> list = stuSubRepository.findByStudentId(studentId);
        int total = 0;
        for (StuSub sub : list) {
            if (sub.getCompleteGrade() != null) {
                total += sub.getCompleteGrade();
            }
        }
        return total;
    }

    /**
     * [기능 2] 위험 분석용: '최신 학기' 평균 학점만 계산 (★ 수정됨)
     * - 전체 평균이 아니라, 가장 최근 학기의 성적 부진 여부를 판단하기 위함
     */
    @Transactional(readOnly = true)
    public Double calculateCurrentSemesterAverageGrade(Long studentId) {
        
        // 1. DB에서 가장 최신(현재) 학년도와 학기 정보를 가져옴
        Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                .orElse(null);

        // 개설된 과목이 아예 없으면 0.0 리턴
        if (latestSubject == null) return 0.0;

        Long targetYear = latestSubject.getSubYear();
        Long targetSemester = latestSubject.getSemester();

        // 2. 해당 학생의 "이번 학기(또는 직전 학기)" 수강 내역만 조회
        List<StuSub> list = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                studentId, targetYear, targetSemester
        );

        if (list.isEmpty()) return 0.0;

        // 3. 평균 학점 계산 로직 (기존과 동일)
        double sumScore = 0.0;
        int count = 0;

        for (StuSub sub : list) {
            double score = convertGradeToScore(sub.getGrade());
            // F학점도 포함해서 계산할지 여부는 정책에 따름 (여기서는 포함해서 평균을 깎음)
            // 성적이 아직 안 나온 과목(null)은 계산에서 제외
            if (sub.getGrade() != null) { 
                sumScore += score;
                count++;
            }
        }

        if (count == 0) return 0.0;
        
        return Math.round((sumScore / count) * 100) / 100.0;
    }

    // [보조] 등급 -> 점수 변환
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
            case "F":  return 0.0; 
            default:   return 0.0; // P/F 과목 등
        }
    }
}