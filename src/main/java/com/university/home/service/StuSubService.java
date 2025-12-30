package com.university.home.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.university.home.dto.GradeDto;
import com.university.home.dto.GradeTotalDto;
import com.university.home.entity.PreStuSub;
import com.university.home.entity.StuSub;
import com.university.home.entity.Student; 
import com.university.home.entity.Subject;
import com.university.home.repository.EvaluationRepository;
import com.university.home.repository.PreStuSubRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.SubjectRepository;
import com.university.home.repository.StudentRepository; 

@Service
@RequiredArgsConstructor
public class StuSubService {

    private final SubjectRepository subjectRepository;
    private final PreStuSubRepository preStuSubRepository;
    private final StuSubRepository stuSubRepository;
    private final StudentRepository studentRepository;
    private final EvaluationRepository evaluationRepository;
    
    // 예비 수강 신청 -> 본 수강 신청 일괄 처리
    @Transactional
    public void createStuSubByPreStuSub() {
        
        List<PreStuSub> allPreAppList = preStuSubRepository.findAll();

        List<Long> distinctSubjectIds = allPreAppList.stream()
                .map(pre -> pre.getId().getSubjectId())
                .distinct()
                .collect(Collectors.toList());

        for (Long subjectId : distinctSubjectIds) {
            
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) continue;

            long applicantCount = preStuSubRepository.countByIdSubjectId(subjectId);

            if (applicantCount <= subject.getCapacity()) {
                
                List<PreStuSub> subjectPreList = preStuSubRepository.findByIdSubjectId(subjectId);
                
                for (PreStuSub pre : subjectPreList) {
                    
                    Long studentId = pre.getId().getStudentId();
                    
                    boolean exists = stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId);
                    
                    if (!exists) {
                        Student student = studentRepository.findById(studentId).orElse(null);
                        
                        if (student != null) {
                            StuSub stuSub = new StuSub();
                            
                            stuSub.setStudent(student); 
                            stuSub.setSubject(subject); 
                            
                            stuSubRepository.save(stuSub);
                        }
                    }
                }
            }
        }
    }
    private GradeDto toDto(StuSub stuSub) {
        GradeDto dto = new GradeDto();
        dto.setStuSubId(stuSub.getId());
        dto.setSubjectId(stuSub.getSubject().getId());
        dto.setSubjectName(stuSub.getSubject().getName());
        dto.setMajorType(stuSub.getSubject().getType());
        dto.setCredit(stuSub.getSubject().getGrades());
        dto.setGrade(stuSub.getGrade());
        dto.setConvertedMark(stuSub.getCompleteGrade());

        boolean evaluated = evaluationRepository.existsByStuSub_Id(stuSub.getId());
        dto.setEvaluated(evaluated);

        dto.setSubYear(stuSub.getSubject().getSubYear());
        dto.setSemester(stuSub.getSubject().getSemester());
        return dto;
    }
    // 금학기 성적 조회
    public List<GradeDto> getThisSemesterGrades(Long studentId) {
        
    	Subject latest = subjectRepository.findTopByOrderBySubYearDescSemesterDesc().orElse(null);
        if (latest == null) return Collections.emptyList();
        
        Long currentYear = latest.getSubYear();
        Long currentSemester = latest.getSemester();
    	
    	List<StuSub> stuSubs = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
            studentId, currentYear, currentSemester
        );

        List<StuSub> filtered = stuSubs.stream()
                .filter(s -> s.getGrade() != null) 
                .toList();

            return filtered.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
    }
    // 학기별 성적 조회
    public List<GradeDto> getGradeBySemester(Long studentId, Long year, Long semester, String type) {

        List<StuSub> list;

        if (type == null || type.isEmpty()) {
            // 전체 조회
            list = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                studentId, year, semester
            );
        } else {
            list = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemesterAndSubjectType(
                studentId, year, semester, type
            );
        }

        list = list.stream().filter(s -> s.getGrade() != null).toList();

        return list.stream().map(this::toDto).toList();
    }
    // 전체 누계 성적 조회
    public List<GradeTotalDto> readGradeInquiryList(Long studentId) {
        List<StuSub> stuSubs = stuSubRepository.findByStudentId(studentId);
        for (StuSub s : stuSubs) {
        }
        List<StuSub> gradedSubs = stuSubs.stream()
            .filter(s -> s.getGrade() != null)
            .toList();

        if (gradedSubs.isEmpty()) {
            return List.of();
        }

        Map<String, List<StuSub>> grouped = gradedSubs.stream()
            .collect(Collectors.groupingBy(
                s -> s.getSubject().getSubYear() + "-" + s.getSubject().getSemester()
            ));

        return grouped.entrySet().stream().map(entry -> {
            List<StuSub> list = entry.getValue();

            Long subYear = list.get(0).getSubject().getSubYear();
            Long semester = list.get(0).getSubject().getSemester();

            Long totalCredit = list.stream()
                .mapToLong(s -> s.getSubject().getGrades()) // 신청학점
                .sum();

            Long earnedCredit = list.stream()
        	    .mapToLong(s -> {
        	        String grade = s.getGrade();
        	        Long credit = s.getSubject().getGrades();
        	        return grade != null && !grade.equals("F") ? credit : 0;
        	    })
        	    .sum();


            double totalPoints = list.stream()
        	    .mapToDouble(s -> {
        	        double point = switch (s.getGrade()) {
        	            case "A+" -> 4.5;
        	            case "A0" -> 4.0;
        	            case "B+" -> 3.5;
        	            case "B0" -> 3.0;
        	            case "C+" -> 2.5;
        	            case "C0" -> 2.0;
        	            case "D+" -> 1.5;
        	            case "D0" -> 1.0;
        	            default -> 0.0;
        	        };
        	        return point * s.getSubject().getGrades();
        	    })
        	    .sum();
            double avgScore = totalCredit == 0 ? 0.0 : totalPoints / totalCredit;
        	
            GradeTotalDto dto = new GradeTotalDto();
            dto.setSubYear(subYear);
            dto.setSemester(semester);
            dto.setTotalCredit(totalCredit);
            dto.setEarnedCredit(earnedCredit);
            dto.setAverageScore(avgScore);
            
            return dto;
            
        }).toList();
        
    }
    @Transactional(readOnly = true)
    public List<Long> getTakenYears(Long studentId) {
        return stuSubRepository.findDistinctYearsByStudentId(studentId);
    }

}