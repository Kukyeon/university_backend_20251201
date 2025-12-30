package com.university.home.controller;

import com.university.home.dto.DepartmentDto;
import com.university.home.entity.Department;
import com.university.home.repository.DepartmentRepository; // Repository 필요
import com.university.home.service.DepartmentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;

    // 학과 전체 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<Department>> getDepartmentList() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }
    
    @GetMapping
    public List<DepartmentDto> getDepartments() {
        return departmentService.getDepartments();
    }
}