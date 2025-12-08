package com.university.home.controller;

import com.university.home.entity.Department;
import com.university.home.repository.DepartmentRepository; // Repository 필요
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

    // 학과 전체 목록 조회 API
    // GET /api/department/list
    @GetMapping("/list")
    public ResponseEntity<List<Department>> getDepartmentList() {
        // 모든 학과를 이름순으로 가져오기 (Sort 안 해도 되지만 하면 좋음)
        return ResponseEntity.ok(departmentRepository.findAll());
    }
}