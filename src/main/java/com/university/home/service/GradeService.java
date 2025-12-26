package com.university.home.service;

import com.university.home.entity.StuSub;
import com.university.home.entity.Subject;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.SubjectRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository; 

     // 학생의 총 이수 학점
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

     // 위험 분석용: 최신 학기 평균 학점만 계산
    @Transactional(readOnly = true)
    public Double calculateCurrentSemesterAverageGrade(Long studentId) {
        
        Subject latestSubject = subjectRepository.findTopByOrderBySubYearDescSemesterDesc()
                .orElse(null);

        if (latestSubject == null) return 0.0;

        Long targetYear = latestSubject.getSubYear();
        Long targetSemester = latestSubject.getSemester();

        List<StuSub> list = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                studentId, targetYear, targetSemester
        );

        if (list.isEmpty()) return 0.0;

        double totalPoints = 0.0;
        long totalCredits = 0;

        for (StuSub sub : list) {
            String grade = sub.getGrade();
            Long credit = sub.getSubject().getGrades();

            if (grade != null) {
                double point = convertGradeToScore(grade); 
                totalPoints += point * credit;
                totalCredits += credit;
            }
        }

        if (totalCredits == 0) return 0.0;
        
        double gpa = totalPoints / totalCredits;
        return Math.round(gpa * 100) / 100.0;
    }

    // 등급 -> 점수 변환
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
            default:   return 0.0; 
        }
    }
}