package com.university.home.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.university.home.entity.Student; // Student 엔티티 import
import com.university.home.entity.Subject;
import com.university.home.repository.EvaluationRepository;
import com.university.home.repository.PreStuSubRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.SubjectRepository;
import com.university.home.repository.StudentRepository; // Student 레포지토리 import

@Service
@RequiredArgsConstructor
public class StuSubService {

    private final SubjectRepository subjectRepository;
    private final PreStuSubRepository preStuSubRepository;
    private final StuSubRepository stuSubRepository;
    private final StudentRepository studentRepository; // 학생 조회를 위해 추가
    private final EvaluationRepository evaluationRepository;
    /**
     * 예비 수강 신청 -> 본 수강 신청 일괄 처리
     */
    @Transactional
    public void createStuSubByPreStuSub() {
        
        // 1. 예비 수강 신청 테이블의 모든 데이터를 가져옵니다.
        List<PreStuSub> allPreAppList = preStuSubRepository.findAll();

        // 2. 자바 Stream을 이용해 신청된 '과목 ID'만 중복 없이 추출
        List<Long> distinctSubjectIds = allPreAppList.stream()
                .map(pre -> pre.getId().getSubjectId())
                .distinct()
                .collect(Collectors.toList());

        // 3. 각 과목별로 순회
        for (Long subjectId : distinctSubjectIds) {
            
            // 강의 정보 조회 (Subject 객체 획득)
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) continue;

            // 신청 인원 조회
            long applicantCount = preStuSubRepository.countByIdSubjectId(subjectId);

            // 4. (신청 인원 <= 정원) 조건 만족 시 자동 이관
            if (applicantCount <= subject.getCapacity()) {
                
                // 해당 과목을 신청한 예비 내역 리스트 가져오기
                List<PreStuSub> subjectPreList = preStuSubRepository.findByIdSubjectId(subjectId);
                
                for (PreStuSub pre : subjectPreList) {
                    
                    Long studentId = pre.getId().getStudentId();
                    
                    // 5. 중복 방지 (쿼리 메서드 이름 변경됨: ByStudent_Id...)
                    boolean exists = stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId);
                    
                    if (!exists) {
                        // ★ 변경된 부분: ID가 아니라 객체를 채워 넣어야 함 ★
                        
                        // 5-1. 학생 엔티티 조회
                        Student student = studentRepository.findById(studentId).orElse(null);
                        
                        if (student != null) {
                            StuSub stuSub = new StuSub();
                            
                            // 외래키 객체 설정 (@ManyToOne 관계)
                            stuSub.setStudent(student); // 학생 객체 주입
                            stuSub.setSubject(subject); // 위에서 찾은 강의 객체 재활용 주입
                            
                            // 필요한 경우 기본값 설정
                            // stuSub.setGrade(null); 
                            // stuSub.setCompleteGrade(null);

                            // DB 저장 (id는 Auto Increment로 자동 생성됨)
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

        // 평가 완료 여부 조회
        boolean evaluated = evaluationRepository.existsByStuSub_Id(stuSub.getId());
        dto.setEvaluated(evaluated);

        dto.setSubYear(stuSub.getSubject().getSubYear());
        dto.setSemester(stuSub.getSubject().getSemester());
        return dto;
    }
 // 금학기 성적 조회
    public List<GradeDto> getThisSemesterGrades(Long studentId, Long currentYear, Long currentSemester) {
        List<StuSub> stuSubs = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
            studentId, currentYear, currentSemester
        );

        List<StuSub> filtered = stuSubs.stream()
                .filter(s -> s.getGrade() != null) // 성적 입력된 항목만
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
            // 전공/교양 필터 포함 조회
            list = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemesterAndSubjectType(
                studentId, year, semester, type
            );
        }

        list = list.stream().filter(s -> s.getGrade() != null).toList();

        return list.stream().map(this::toDto).toList();
    }
 // 전체 누계 성적 조회
    public List<GradeTotalDto> readGradeInquiryList(Long studentId) {
        // 해당 학생의 모든 StuSub 조회
        List<StuSub> stuSubs = stuSubRepository.findByStudentId(studentId);
        for (StuSub s : stuSubs) {
            System.out.println(s.getCompleteGrade());
        }
     // 성적 입력된 항목만 필터 (completeGrade != null)
        List<StuSub> gradedSubs = stuSubs.stream()
            .filter(s -> s.getGrade() != null)
            .toList();

        // 성적이 없는 경우 빈 리스트 반환
        if (gradedSubs.isEmpty()) {
            return List.of();
        }

        // 연도+학기별 그룹화
        Map<String, List<StuSub>> grouped = gradedSubs.stream()
            .collect(Collectors.groupingBy(
                s -> s.getSubject().getSubYear() + "-" + s.getSubject().getSemester()
            ));

        // DTO 변환
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
        	        // F면 0, 나머지는 학점 그대로
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


}