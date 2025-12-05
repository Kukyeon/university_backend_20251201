package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.StuSchDto;
import com.university.home.service.StuSchService;

@RestController
@RequestMapping("/api/stusch")
public class StuSchController {

	@Autowired
    private StuSchService stuSchService;

    @GetMapping("/list")
    public ResponseEntity<List<StuSchDto>> getByStudent(@RequestParam(name = "studentId") Long studentId) {
        return ResponseEntity.ok(stuSchService.getByStudent(studentId));
    }
    
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/create")
    public ResponseEntity<StuSchDto> createStuSch(@RequestBody StuSchDto dto) {
        return ResponseEntity.ok(
            stuSchService.createStuSch(dto.getStudentId(), dto.getScholarshipTypeId(), dto.getSchYear(), dto.getSemester())
        );
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStuSch(@PathVariable(name = "id") Long id) {
        stuSchService.deleteStuSch(id);
        return ResponseEntity.ok("장학금 내역 삭제 완료");
    }
}
