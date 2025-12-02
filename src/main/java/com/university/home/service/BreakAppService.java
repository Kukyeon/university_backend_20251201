package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.entity.BreakApp;
import com.university.home.entity.Student;
import com.university.home.repository.BreakAppRepository;

@Service
public class BreakAppService {

	@Autowired
	BreakAppRepository breakAppRepository;
	@Autowired
	StudentService studentService;
	public List<BreakApp> getByStudent(Long studentId) {
		Student student = studentService.getStudentById(studentId);
		List<BreakApp> breakAppList = breakAppRepository
				.findByStudentOrderByIdDesc(student);
		
		return breakAppList;
	}
	
}
