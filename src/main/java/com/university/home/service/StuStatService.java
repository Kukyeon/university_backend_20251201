package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.StudentDto;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.repository.StuStatRepository;

import jakarta.transaction.Transactional;

@Service
public class StuStatService {

	@Autowired
	StuStatRepository stuStatRepository;
	
	// 학생의 현재 학적
	public StuStat getCurrentStatus(Long studentId) {
		
		StuStat stuStat = stuStatRepository.
				findByStudentIdOrderByIdDesc(studentId).get(0);
		return stuStat;
	}
	public List<StuStat> getStatusList(Long studentId) {
		List<StuStat> stuStats = stuStatRepository.findByStudentIdOrderByIdDesc(studentId);
		if (stuStats.isEmpty()) {
		    throw new RuntimeException("해당 학생의 학적 상태가 존재하지 않습니다.");
		}
		return stuStats;
	}
	 // 최초 학적 상태 생성
    @Transactional
    public void createFirstStatus(Student student) {
        StuStat stuStat = new StuStat();
        stuStat.setStudent(student);
        stuStat.setStatus("재학");
        stuStat.setFromDate(LocalDate.now());
        stuStat.setToDate(LocalDate.parse("9999-01-01"));
        stuStatRepository.save(stuStat);
    }

    // 학적 상태 업데이트
    @Transactional
    public void updateStatus(Student student, String newStatus, Long breakAppId) {
        // 가장 최근 상태 가져오기
        StuStat lastStatus = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId()).get(0);

        // 기존 상태 종료
        lastStatus.setToDate(LocalDate.now());
        stuStatRepository.save(lastStatus);

        // 새 상태 추가
        StuStat newStat = new StuStat();
        newStat.setStudent(student);
        newStat.setStatus(newStatus);
        newStat.setFromDate(LocalDate.now());
        newStat.setToDate(LocalDate.parse("9999-01-01"));
        newStat.setBreakAppId(breakAppId);
        stuStatRepository.save(newStat);
    }
}
