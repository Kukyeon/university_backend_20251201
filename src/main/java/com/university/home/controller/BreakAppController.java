package com.university.home.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.BreakAppDto;
import com.university.home.entity.BreakApp;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.BreakAppService;
import com.university.home.service.CustomUserDetails;

@RestController
@RequestMapping("/api/break")
public class BreakAppController {


	@Autowired
	BreakAppService breakAppService;

	@PostMapping("/app")
	public ResponseEntity<?> createBreakApp(@RequestBody BreakAppDto dto, @AuthenticationPrincipal CustomUserDetails loginUser) {
		Long studentId = loginUser.getUser().getId();
		
		dto.setStudentId(studentId);
		
		int month = LocalDate.now().getMonthValue();
		dto.setFromYear((long)LocalDate.now().getYear());
		dto.setFromSemester((month <= 6)? 1L : 2L);
		
	    if (dto.getToYear() == null || dto.getToSemester() == null) {
	        throw new CustomRestfullException("종료 학기 정보가 누락되었습니다.", HttpStatus.BAD_REQUEST);
	    }

	    if (dto.getToYear().equals(dto.getFromYear()) && dto.getToSemester() < dto.getFromSemester()) {
	        throw new CustomRestfullException("종료 학기가 시작 학기 이전입니다.", HttpStatus.BAD_REQUEST);
	    }
		breakAppService.createBreakApp(dto);
		
		return ResponseEntity.ok("휴학 신청 완료");
	}
	@GetMapping("/list")
	public ResponseEntity<?> getByStudent(@AuthenticationPrincipal CustomUserDetails loginUser) {
		List<BreakApp> apps;
		String role = loginUser.getUser().getUserRole();
		
		if(role.equals("student")) {
			Long studentId = loginUser.getUser().getId();
			apps = breakAppService.getByStudent(studentId);
		}else if(role.equals("staff")) {
	        // 관리자는 처리중 신청만 조회
	        apps = breakAppService.getBreakApps()
	                              .stream()
	                              .filter(a -> a.getStatus().equals("처리중"))
	                              .toList();
	    } else {
	        apps = new ArrayList<>();
	    }
		return ResponseEntity.ok(apps);
	}
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> getAppDetail(@PathVariable(name = "id") Long id, @AuthenticationPrincipal CustomUserDetails loginUser) {
		BreakApp app = breakAppService.getById(id);
		
		if (!app.getStudent().getId().equals(loginUser.getUser().getId())) {
	        throw new CustomRestfullException("본인 신청 내역만 조회할 수 있습니다.", HttpStatus.FORBIDDEN);
	    }
		
		return ResponseEntity.ok(app);
	}
	@DeleteMapping("/detail/{id}")
	public ResponseEntity<?> deleteApp(@PathVariable(name = "id") Long id, @AuthenticationPrincipal CustomUserDetails loginUser) {
		BreakApp app = breakAppService.getById(id);
		    
	    if (!app.getStudent().getId().equals(loginUser.getUser().getId())) {
	        throw new CustomRestfullException("본인 신청만 취소할 수 있습니다.", HttpStatus.FORBIDDEN);
	    }

	    breakAppService.deleteApp(id);
		return ResponseEntity.ok("신청 취소 완료");
	}
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateApp(@PathVariable(name = "id") Long id,  @RequestBody Map<String, String> body, @AuthenticationPrincipal CustomUserDetails loginUser) {
		if (!loginUser.getUser().getUserRole().equals("staff") &&
			!loginUser.getUser().getUserRole().equals("student")	) {
			throw new CustomRestfullException("권한이 없습니다.", HttpStatus.FORBIDDEN);
		}
		String status = body.get("status");
		breakAppService.updateStatus(id, status);
		return ResponseEntity.ok("휴학 신청 상태 변경 완료");
	}
}
