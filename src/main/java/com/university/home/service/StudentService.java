package com.university.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.StudentDto;
import com.university.home.entity.Student;
import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class StudentService {

    private final ScholarshipRepository scholarshipRepository;

	@Autowired
	StudentRepository studentRepository;

    StudentService(ScholarshipRepository scholarshipRepository) {
        this.scholarshipRepository = scholarshipRepository;
    }
	
	@Transactional
	public Long createStudent(StudentDto dto) {
		Student student = new Student();
		student.setName(dto.getName());
		student.setAddress(dto.getAddress());
		student.setBirthDate(dto.getBirthDate());
		student.setEmail(dto.getEmail());
		student.setGender(dto.getGender());
		student.setTel(dto.getTel());
		student.setEntranceDate(dto.getEntranceDate());
		student.setDepartment(dto.getDepartment());
		//  Department dept = departmentRepository.findById(dto.getDepartmentId())
        // .orElseThrow(() -> new RuntimeException("Department not found"));
		// student.setDepartment(dept);
		studentRepository.save(student);
		return student.getId();
	}
	
	@Transactional
	public Student readStudent(Long id) {
		return studentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Student not found"));
	}
	@Transactional
	public void updateStudent(StudentDto dto) {
		Student student = studentRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException("Student not found"));
	}
}
