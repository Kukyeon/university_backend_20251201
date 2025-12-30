package com.university.home.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.entity.BreakApp;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.entity.Tuition;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.BreakAppService;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.StuStatService;
import com.university.home.service.StudentService;
import com.university.home.service.TuitionService;

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

	@GetMapping("/me")
	public ResponseEntity<?> getTuitionList(@AuthenticationPrincipal CustomUserDetails loginUser){
		Long studentId = loginUser.getUser().getId();
		List<Tuition> tuitions = tuitionService.getStatusList(studentId, true);
		return ResponseEntity.ok(tuitions);
	}
	@GetMapping("/payment")
	public ResponseEntity<?> getTuitionPayment(@AuthenticationPrincipal CustomUserDetails loginUser) {
		Long studentId = loginUser.getUser().getId();
	    Student student = studentService.getStudentByIdEntity(studentId);

	    StuStat stuStat = stuStatService.getCurrentStatus(student.getId());
	    List<BreakApp> breakAppList = breakAppService.getByStudent(studentId);
	    
	    if (stuStat.getStatus().equals("졸업") || stuStat.getStatus().equals("자퇴")) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", null);
	        response.put("error", "등록금 납부 대상이 아닙니다.");
	        return ResponseEntity.ok(response);
	    }

	    for (BreakApp b : breakAppList) {
	        if ("승인".equals(b.getStatus()) && b.getToYear() >= LocalDate.now().getYear()) {
	            Map<String, Object> response = new HashMap<>();
	            response.put("status", null);
	            response.put("error", "현재 학기 휴학 중이므로 등록금 납부 불가");
	            return ResponseEntity.ok(response);
	        }
	    }

	    Tuition tuition = null;
	    try {
	        tuition = tuitionService.getSemester(studentId, (long) LocalDate.now().getYear(),
	                    (LocalDate.now().getMonthValue() <= 6 ? 1L : 2L));
	    } catch (CustomRestfullException e) {
	    }

	    if (tuition == null) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("status", null);
	        response.put("error", "등록금 고지서가 없습니다.");
	        return ResponseEntity.ok(response);
	    }
	    return ResponseEntity.ok(tuition);
	}
	@PostMapping("/payment")
	public ResponseEntity<?> payTuition(@AuthenticationPrincipal CustomUserDetails loginUser){
		Long studentId = loginUser.getUser().getId();
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
		 Map<String, Object> response = new HashMap<>();
		    response.put("createdCount", createdCount);
		    response.put("message", createdCount + "건의 등록금 고지서가 생성되었습니다.");

		    return ResponseEntity.ok(response);
	}
	
}
