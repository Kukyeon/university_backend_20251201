package com.university.home.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/break")
public class BreakAppController {


	@Autowired
	BreakAppService breakAppService;

	@PostMapping("/app")
	public ResponseEntity<?> createBreakApp(@RequestBody BreakAppDto dto, @RequestParam(name = "studentId") Long studentId) {
		dto.setStudentId(studentId);
		
		int month = LocalDate.now().getMonthValue();
		dto.setFromYear((long)LocalDate.now().getYear());
		dto.setFromSemester((month <= 6)? 1L : 2L);
		
		if (dto.getToYear().equals(dto.getFromYear()) && dto.getToSemester() <= dto.getFromSemester()) {
			 throw new CustomRestfullException("종료 학기가 시작 학기 이전입니다.", HttpStatus.BAD_REQUEST);
		}
		breakAppService.createBreakApp(dto);
		
		return ResponseEntity.ok("휴학 신청 완료");
	}
	@GetMapping("/list")
	public ResponseEntity<?> getByStudent(@RequestParam(name = "studentId") Long studentId) {
		List<BreakApp> apps = breakAppService.getByStudent(studentId);
		return ResponseEntity.ok(apps);
	}
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> getAppDetail(@PathVariable(name = "id") Long id) {
		BreakApp app = breakAppService.getById(id);
		return ResponseEntity.ok(app);
	}
	@DeleteMapping("/detail/{id}")
	public ResponseEntity<?> deleteApp(@PathVariable(name = "id") Long id) {
		breakAppService.deleteApp(id);
		return ResponseEntity.ok("신청 취소 완료");
	}
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateApp(@PathVariable(name = "id") Long id, @RequestParam(name = "status")String status) {
		breakAppService.updateStatus(id, status);
		return ResponseEntity.ok("휴학 신청 상태 변경 완료");
	}
}
