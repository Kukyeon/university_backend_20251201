package com.university.home.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.entity.BreakApp;
import com.university.home.entity.CollTuit;
import com.university.home.entity.College;
import com.university.home.entity.Scholarship;
import com.university.home.entity.StuSch;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.entity.Tuition;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.CollTuitRepository;
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
	@Autowired
	StuStatService stuStatService;
	@Autowired
	BreakAppService breakAppService;
	@Autowired
	CollTuitRepository collTuitRepository;
	
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
    	
    	StuStat stuStat = stuStatService.getCurrentStatus(studentId);
    	if ("휴학".equals(stuStat.getStatus())) {
			stuStatService.updateStatus(stuStat.getStudent(),"재학", null);
		}
    	
    }
    private int getCurrentSemester() {
        return (LocalDate.now().getMonthValue() <= 6) ? 1 : 2;
    }
//    @Transactional
//    public int createTuition(Long studentId) {
//    	Student student = studentService.getStudentById(studentId);
//    	
//    	StuStat stuStat = stuStatService.getCurrentStatus(studentId);
//    	 
//    	if (stuStat.getStatus().equals("졸업") || stuStat.getStatus().equals("자퇴")) {
//			return 0;
//		}
//    	
//    	List<BreakApp> breakApps = breakAppService.getByStudent(studentId);
//    	int currentYear = LocalDate.now().getYear();
//    	int currentSemester = getCurrentSemester();
//    	for (BreakApp b : breakApps) {
//			if (b.getStatus().equals("승인")) {
//				if(b.getToYear() > LocalDate.now().getYear()) return 0;
//				if(b.getToYear() == LocalDate.now().getYear() && b.getToSemester() >= getCurrentSemester()) return 0;
//			}
//		}
//    	Optional<Tuition> existing = tuitionRepository
//	            .findByStudentIdAndTuiYearAndSemester(studentId, (long)currentYear, (long)currentSemester);
//    	if (tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, (long)currentYear, (long)currentSemester).isPresent()) {
//            return 0;
//        }
//    	College college = student.getDepartment().getCollege();
//    	CollTuit collTuit = collTuitRepository.findByCollege(college).orElseThrow(() -> new RuntimeException("등록금 정보가 없습니다."));
//    	Long tuiAmount = collTuit.getAmount();
//    	
//
//    }
}
