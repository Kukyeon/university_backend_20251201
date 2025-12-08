package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.dto.ProfessorDto;
import com.university.home.dto.StudentDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.entity.Professor;
import com.university.home.entity.Student;
import com.university.home.entity.User;
import com.university.home.repository.DepartmentRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class StudentService {

	@Autowired
	StudentRepository studentRepository;
	@Autowired
	UserService userService;
	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	StuStatService stuStatService;
	@Transactional
	public Student createStudentWithStatus(StudentDto dto) {
	    Student student = createStudent(dto);  // 학생 생성
	    stuStatService.createFirstStatus(student); // 학적 상태 생성
	    return student;
	}
	public StudentDto toDto(Student student) {
	    StudentDto dto = new StudentDto();
	    dto.setId(student.getId());
	    dto.setName(student.getName());
	    dto.setBirthDate(student.getBirthDate());
	    dto.setGender(student.getGender());
	    dto.setAddress(student.getAddress());
	    dto.setTel(student.getTel());
	    dto.setEmail(student.getEmail());

	    Department dep = student.getDepartment();
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
	public Student createStudent(StudentDto dto) {
		Student student = new Student();
		student.setName(dto.getName());
		student.setAddress(dto.getAddress());
		student.setBirthDate(dto.getBirthDate());
		student.setEmail(dto.getEmail());
		student.setGender(dto.getGender());
		student.setTel(dto.getTel());
		student.setEntranceDate(dto.getEntranceDate());
		//student.setDepartment(dto.getDepartment());
		Department dept = departmentRepository.findById(dto.getDepartment().getId())
        .orElseThrow(() -> new RuntimeException("Department not found"));
		student.setDepartment(dept);
		studentRepository.save(student);
		
		User user = userService.createUser(student.getId(), "student");
		student.setUser(user);
		studentRepository.save(student);
		return student;
	}
	
	@Transactional
	public StudentDto readStudent(Long id) {
		Student student =  studentRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Student not found"));
		return toDto(student);
	}

	@Transactional
	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return studentRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return studentRepository.findByNameAndEmail(name, email)
				.map(Student::getId)
				.orElseThrow(() -> new RuntimeException("Student not found"));
	}
	// 전체 학생 조회
	public Page<StudentDto> getStudents(Pageable pageable) {
		 return studentRepository.findAll(pageable)
	                .map(this::toDto); // map으로 DTO 변환
	}
	// 학과별 학생 조회
	public Page<StudentDto> getStudentsByDep(Long deptId,Pageable pageable) {
		Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
		  return studentRepository.findByDepartment(dept, pageable)
	                .map(this::toDto);
	}
	// 학번 학생 조회
	public StudentDto getStudentById(Long studentId) {
		Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return toDto(student);
	}
	// StudentService
	public Student getStudentByIdEntity(Long studentId) {
	    return studentRepository.findById(studentId)
	            .orElseThrow(() -> new RuntimeException("학생이 존재하지 않습니다."));
	}

	@Transactional
	public int updateStudentGradeAndSemesters() {
		List<Student> students = studentRepository.findAll();
		int count = 0;
	    for (Student student : students) {
	        int grade = student.getGrade().intValue();
	        int semester = student.getSemester().intValue();

	        switch (grade) {
            case 1:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(2)); student.setSemester(Long.valueOf(1)); }
                break;
            case 2:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(3)); student.setSemester(Long.valueOf(1)); }
                break;
            case 3:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(4)); student.setSemester(Long.valueOf(1)); }
                break;
            case 4:
                if (semester == 1) student.setSemester(Long.valueOf(2));
	                break;
	        }
	        count++;
	    }
	    return count;
	}
}
