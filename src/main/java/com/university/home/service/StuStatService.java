package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Transactional
    public void revertToRegular(Student student) {
        List<StuStat> stats = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId());
        
        for (StuStat s : stats) {
            if ("휴학".equals(s.getStatus())) {
                stuStatRepository.delete(s);
            }
        }
        
        // 재학 상태 확인
        boolean hasRegular = stats.stream().anyMatch(s -> "재학".equals(s.getStatus()));
        if (!hasRegular) {
            StuStat re = new StuStat();
            re.setStudent(student);
            re.setStatus("재학");
            re.setFromDate(LocalDate.now());
            re.setToDate(LocalDate.parse("9999-01-01"));
            stuStatRepository.save(re);
        }
    }

    // 학적 상태 업데이트
    @Transactional
    public void updateStatus(Student student, String newStatus, Long breakAppId) {
        StuStat lastStatus = stuStatRepository.findByStudentIdOrderByIdDesc(student.getId()).get(0);

        lastStatus.setToDate(LocalDate.now());
        stuStatRepository.save(lastStatus);

        StuStat newStat = new StuStat();
        newStat.setStudent(student);
        newStat.setStatus(newStatus);
        newStat.setFromDate(LocalDate.now());
        newStat.setToDate(LocalDate.parse("9999-01-01"));
        newStat.setBreakAppId(breakAppId);
        stuStatRepository.save(newStat);
    }
    public List<Long> getAllStudentIds() {
        return stuStatRepository.findAll()
                .stream()
                .map(stuStat -> stuStat.getStudent().getId())
                .distinct()
                .toList();
    }

}
