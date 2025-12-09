package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.dto.ProfessorDto;
import com.university.home.dto.SubjectForProfessorDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.entity.Professor;
import com.university.home.entity.Subject;
import com.university.home.entity.User;
import com.university.home.repository.DepartmentRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.SubjectRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfessorService {

	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	UserService userService;
	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	SubjectRepository subjectRepository;
	
	public ProfessorDto toDto(Professor professor) {
	    ProfessorDto dto = new ProfessorDto();
	    dto.setId(professor.getId());
	    dto.setName(professor.getName());
	    dto.setBirthDate(professor.getBirthDate());
	    dto.setGender(professor.getGender());
	    dto.setAddress(professor.getAddress());
	    dto.setTel(professor.getTel());
	    dto.setEmail(professor.getEmail());
	    dto.setHireDate(professor.getHireDate());

	    Department dep = professor.getDepartment();
	    if (dep != null) {
	        DepartmentDto depDto = new DepartmentDto();
	        depDto.setId(dep.getId());
	        depDto.setName(dep.getName());

	        College col = dep.getCollege();
	        if (col != null) {
	            CollegeDto colDto = new CollegeDto();
	            colDto.setId(col.getId());
	            colDto.setName(col.getName());
	            depDto.setCollege(colDto);
	        }

	        dto.setDepartment(depDto);
	    }

	    return dto;
	}
	@Transactional
	public ProfessorDto createProfessor(ProfessorDto dto) {
		Professor professor = new Professor();
		professor.setName(dto.getName());
		professor.setAddress(dto.getAddress());
		professor.setBirthDate(dto.getBirthDate());
		professor.setEmail(dto.getEmail());
		professor.setGender(dto.getGender());
		professor.setTel(dto.getTel());
		professor.setHireDate(LocalDate.now());
		//professor.setDepartment(dto.getDepartment());
		Department dept = departmentRepository.findById(dto.getDepartment().getId())
        .orElseThrow(() -> new RuntimeException("Department not found"));
		professor.setDepartment(dept);
		professorRepository.save(professor);
		
		User user =userService.createUser(professor.getId(), "professor");
		professor.setUser(user);
		professorRepository.save(professor);
		
		return toDto(professor);
	}
	@Transactional
	public ProfessorDto readProfessor(Long id) {
		Professor professor = professorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Professor not found"));
		return toDto(professor);
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
	public Page<ProfessorDto> getProfessors(Pageable pageable) {
		return professorRepository.findAll(pageable)
                .map(this::toDto);
	}
	// 학과별 교수 조회
	public Page<ProfessorDto> getProfessorsByDep(Long deptId,Pageable pageable) {
		Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
		 return professorRepository.findByDepartment(dept, pageable)
	                .map(this::toDto);
	}
	// id 교수 조회
	public ProfessorDto getProfessorById(Long professorId) {
		Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found"));
        return toDto(professor);
    }
//	===============내 강의 조희 서비스!@!!@! ============
	public SubjectForProfessorDto toDto(Subject subject) {
	    SubjectForProfessorDto dto = new SubjectForProfessorDto();
	    dto.setId(subject.getId());
	    dto.setName(subject.getName());
	    dto.setSubDay(subject.getSubDay());
	    dto.setStartTime(subject.getStartTime());
	    dto.setEndTime(subject.getEndTime());
	    dto.setRoomId(subject.getRoom().getId());
	    return dto;
	}

	public List<SubjectForProfessorDto> selectSubjectsByProfessor(Long professorId,Long subYear, Long semester) {
	    List<Subject> subjects;
	    
	    if (subYear != null && semester != null) {
	        subjects = subjectRepository.findByProfessor_IdAndSubYearAndSemester(professorId, subYear, semester);
	    } else {
	        subjects = subjectRepository.findByProfessor_Id(professorId);
	    }
	    
	    return subjects.stream()
	            .map(this::toDto)
	            .toList();
	}

}
