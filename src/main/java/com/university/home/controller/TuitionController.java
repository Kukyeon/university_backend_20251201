package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.entity.BreakApp;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.entity.Tuition;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.BreakAppService;
import com.university.home.service.StuStatService;
import com.university.home.service.StudentService;
import com.university.home.service.TuitionService;
import com.university.home.service.UserService;

@RestController
@RequestMapping("/api/tuition")
public class TuitionController {

	@Autowired
	TuitionService tuitionService;
	@Autowired
	StudentService studentService;
	@Autowired
	StuStatService stuStatService;
	@Autowired
	BreakAppService breakAppService;

	@GetMapping("/{studentId}")
	public ResponseEntity<?> getTuitionList(@PathVariable(name = "studentId") Long studentId){
		List<Tuition> tuitions = tuitionService.tuitionList(studentId);
		return ResponseEntity.ok(tuitions);
	}
	@GetMapping("/payment/{studentId}")
	public ResponseEntity<?> getTuitionPayment(@PathVariable(name = "studentId") Long studentId) {
	    // 학생 정보 확인
	    Student student = studentService.getStudentById(studentId);

	    // 학적 상태 + 휴학 체크
	    StuStat stuStat = stuStatService.getCurrentStatus(studentId);
	    List<BreakApp> breakAppList = breakAppService.getByStudent(studentId);
	    if (stuStat.getStatus().equals("졸업") || stuStat.getStatus().equals("자퇴")) {
            throw new CustomRestfullException("등록금 납부 대상이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        for (BreakApp b : breakAppList) {
            if ("승인".equals(b.getStatus()) && b.getToYear() >= java.time.LocalDate.now().getYear()) {
                throw new CustomRestfullException("현재 학기 휴학 중이므로 등록금 납부 불가", HttpStatus.BAD_REQUEST);
            }
        }

	    // 등록금 고지서 조회
	    Tuition tuition = tuitionService.getSemester(studentId,  (long) java.time.LocalDate.now().getYear(),
                (java.time.LocalDate.now().getMonthValue() <= 6 ? 1L : 2L));
	    return ResponseEntity.ok(tuition);
	}
	@PostMapping("/payment/{studentId}")
	public ResponseEntity<?> payTuition(@PathVariable Long studentId){
		tuitionService.updateStatus(studentId);
		return ResponseEntity.ok("등록금 남부 완료");
	}
	@PostMapping("/create")
	public ResponseEntity<?> createBills() {
		List<Long> studentIds = stuStatService.getAllStudentIds();
		
		int createdCount = 0;
		for (Long studentId : studentIds) {
			createdCount += tuitionService.createTuition(studentId);
		}
		return ResponseEntity.ok("등록금 고지서 생성 완료 : " + createdCount + "건");
	}
	
}
