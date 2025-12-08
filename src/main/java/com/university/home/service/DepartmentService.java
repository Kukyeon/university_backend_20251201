package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.repository.CollegeRepository;
import com.university.home.repository.DepartmentRepository;

import jakarta.transaction.Transactional;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	CollegeRepository collegeRepository;
	
	public List<Department> departmentList() {
		return departmentRepository.findAll();
	}
	public DepartmentDto toDto(Department department) {
	    DepartmentDto dto = new DepartmentDto();
	    dto.setId(department.getId());
	    dto.setName(department.getName());

	    College college = department.getCollege();
	    CollegeDto collegeDto = new CollegeDto();
	    collegeDto.setId(college.getId());
	    collegeDto.setName(college.getName());
	    dto.setCollege(collegeDto);

	    return dto;
	}
	// 등록
    public DepartmentDto createDepartment(DepartmentDto dto, Long collegeId) {
    	College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("단과대학이 존재하지 않습니다."));
        Department department = new Department();
        department.setName(dto.getName());
        department.setCollege(college);
        departmentRepository.save(department);
        return toDto(department);
    }

    // 삭제
    @Transactional
    public void deleteDepartment(Long id) {
    	Department department = departmentRepository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("학과가 존재하지 않습니다."));
        departmentRepository.delete(department);
    }
    @Transactional
    public DepartmentDto updateDepartment(Long id ,DepartmentDto dto) {
    	Department department = departmentRepository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("학과가 존재하지 않습니다."));
         department.setName(dto.getName());

         departmentRepository.save(department);
         return toDto(department);
    }
}
