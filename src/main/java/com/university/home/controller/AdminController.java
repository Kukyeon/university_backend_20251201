package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.CollTuitFormDto;
import com.university.home.entity.CollTuit;
import com.university.home.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	
	@Autowired
	AdminService adminService;
	
	@GetMapping("/tuition")
	public ResponseEntity<?> getTuitionList() {
		List<CollTuitFormDto> tuits = adminService.getCollTuit();
		return ResponseEntity.ok(tuits);
	}
	@PostMapping("/tuition")
	public ResponseEntity<?> createTuition(@Valid@RequestBody CollTuitFormDto dto) {
		adminService.createCollTuit(dto);
		 return ResponseEntity.ok("등록금 등록 완료");
	}
	@PutMapping("/tuition")
	public ResponseEntity<?> updateTuition(@Valid@RequestBody CollTuitFormDto dto){
		CollTuit updated = adminService.updateCollTuit(dto);
		return ResponseEntity.ok(updated);
	}
	@DeleteMapping("/tuition/{collegeId}")
	public ResponseEntity<?> deleteTuition(@PathVariable("collegeId") Long collegeId) {
		adminService.deleteCollTuit(collegeId);
	 return ResponseEntity.ok("등록금 삭제 완료");
	}
}
