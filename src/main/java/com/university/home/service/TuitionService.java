package com.university.home.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.entity.Tuition;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.TuitionRepository;

import jakarta.transaction.Transactional;

@Service
public class TuitionService {

	@Autowired
	ScholarshipRepository scholarshipRepository;
	@Autowired
	TuitionRepository tuitionRepository;
	@Autowired
	StudentService studentService;
	
	 public List<Tuition> tuitionList(Long studentId) {
	        return tuitionRepository.findByStudentId(studentId);
    }

    public List<Tuition> getStatusList(Long studentId, Boolean status) {
        return tuitionRepository.findByStudentIdAndStatus(studentId, status);
    }

    public Tuition getSemester(Long studentId ,Long tuiYear ,Long semester) {
        return tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, tuiYear, semester)
                .orElseThrow(() -> new CustomRestfullException("등록금 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
    @Transactional
    public void updateStatus(Long studentId) {
    	Long currentyear = (long)LocalDate.now().getYear();
    	Long currentSemester =(LocalDate.now().getMonthValue() <= 6) ? 1L : 2L;
    	
    	Tuition tuition = tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, currentyear, currentSemester)
    			.orElseThrow(() -> new CustomRestfullException("등록금 내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
    	if (Optional.ofNullable(tuition.getStatus()).orElse(false)) {
    	    throw new CustomRestfullException("이미 납부된 상태입니다.", HttpStatus.BAD_REQUEST);
    	}
    	tuition.setStatus(true);
    	
    }
}
