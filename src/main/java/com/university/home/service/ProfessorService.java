package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.university.home.dto.ProfessorDto;
import com.university.home.entity.Professor;
import com.university.home.entity.User;
import com.university.home.repository.ProfessorRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfessorService {

	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	UserService userService;
	
	@Transactional
	public Long createProfessor(ProfessorDto dto) {
		Professor professor = new Professor();
		professor.setName(dto.getName());
		professor.setAddress(dto.getAddress());
		professor.setBirthDate(dto.getBirthDate());
		professor.setEmail(dto.getEmail());
		professor.setGender(dto.getGender());
		professor.setTel(dto.getTel());
		professor.setDepartment(dto.getDepartment());
		//  Department dept = departmentRepository.findById(dto.getDepartmentId())
        // .orElseThrow(() -> new RuntimeException("Department not found"));
		// professor.setDepartment(dept);
		professorRepository.save(professor);
		
		User user =userService.createUser(professor.getId(), "professor");
		professor.setUser(user);
		professorRepository.save(professor);
		
		return professor.getId();
	}
	@Transactional
	public Professor readProfessor(Long id) {
		return professorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Professor not found"));
	}
	@Transactional
	public void updateProfessor(ProfessorDto dto) {
		Professor professor = professorRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException("Professor not found"));
		professor.setTel(dto.getTel());
		professor.setAddress(dto.getAddress());
		professor.setEmail(dto.getEmail());
	}
	@Transactional
	public List<Professor> getAllList () {
		return professorRepository.findAll();
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return professorRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return professorRepository.findByNameAndEmail(name, email)
				.map(Professor::getId)
				.orElseThrow(() -> new RuntimeException("Professor not found"));
	}
	// 전체 교수 조회
	public Page<Professor> getProffesors(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findAll(pageable);
	}
	// 학과별 교수 조회
	public Page<Professor> getProfessorsByDep(Long deptId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findByDepartmentId(deptId, pageable);
	}
	// id 교수 조회
	public Page<Professor> getProfessorsById(Long professorId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findById(professorId, pageable);
	}
}
