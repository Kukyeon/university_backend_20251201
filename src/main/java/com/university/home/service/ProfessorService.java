package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.university.home.dto.ProfessorDto;
import com.university.home.entity.Professor;
import com.university.home.entity.User;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfessorService {

	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;
	
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
		
		User user = new User();
		user.setId(professor.getId());
		user.setUserRole("professor");
		user.setPassword(encoder.encode(professor.getId().toString()));
		
		userRepository.save(user);
		
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
	// 전체 학생 조회
	public Page<Professor> getStudents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findAll(pageable);
	}
	// 학과별 학생 조회
	public Page<Professor> getStudentsByDep(Long deptId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findByDepartmentId(deptId, pageable);
	}
	// 학번 학생 조회
	public Page<Professor> getStudentsById(Long professorId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return professorRepository.findByProfessorId(professorId, pageable);
	}
}
