package com.university.home.dto;

import com.university.home.entity.Subject;
import com.university.home.entity.Syllabus; // Syllabus 엔티티 import

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyllabusDto {
    private Long subjectId;
    private String name;        
    private Long subYear;       
    private Long semester;      
    private Long grades;        
    private String type;        
    private String subDay;      
    private Long startTime;  
    private Long endTime;    
    private String roomId;      
    private String collegeName; 
    
    // 교수 정보
    private Long professorId;
    private String deptName;       
    private String professorName;  
    private String tel;            
    private String email;          
    
    // 상세 내용
    private String overview;    // 강의 개요
    private String objective;   // 강의 목표
    private String textbook;    // 교재
    private String program;     // 주간 계획

    // Entity -> DTO 변환 메서드
    public static SyllabusDto fromEntity(Subject subject) {
        // Subject와 1:1 연결된 Syllabus 가져오기
        Syllabus syllabus = subject.getSyllabus();

        return SyllabusDto.builder()
                .subjectId(subject.getId())
                .name(subject.getName())
                .subYear(subject.getSubYear())
                .semester(subject.getSemester())
                .grades(subject.getGrades())
                .type(subject.getType())
                .subDay(subject.getSubDay())
                .startTime(subject.getStartTime())
                .endTime(subject.getEndTime())
                .roomId(subject.getRoom() != null ? String.valueOf(subject.getRoom().getId()) : "미정")
                .collegeName(subject.getDepartment() != null && subject.getDepartment().getCollege() != null 
                        ? subject.getDepartment().getCollege().getName() : "")
                .professorId(subject.getProfessor() != null ? subject.getProfessor().getId() : null)
                .deptName(subject.getDepartment() != null ? subject.getDepartment().getName() : "")
                .professorName(subject.getProfessor() != null ? subject.getProfessor().getName() : "미정")
                .email(subject.getProfessor() != null ? subject.getProfessor().getEmail() : "")
                .tel("02-123-4567") 
                
                // ★ [핵심] Syllabus가 존재하면 내용을 넣고, 없으면 빈 문자열("") 반환
                .overview(syllabus != null ? syllabus.getOverview() : "")
                .objective(syllabus != null ? syllabus.getObjective() : "")
                .textbook(syllabus != null ? syllabus.getTextbook() : "")
                .program(syllabus != null ? syllabus.getProgram() : "")
                .build();
    }
}