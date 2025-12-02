package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.university.home.dto.StudentDto;
import com.university.home.entity.Student;
import com.university.home.entity.User;
import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class StudentService {

	@Autowired
	StudentRepository studentRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder encoder;
	
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
		
		User user = new User();
		user.setId(student.getId());
		user.setUserRole("student");
		user.setPassword(encoder.encode(student.getId().toString()));
		userRepository.save(user);
		
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
		student.setTel(dto.getTel());
		student.setAddress(dto.getAddress());
		student.setEmail(dto.getEmail());
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
	public Page<Student> getStudents(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return studentRepository.findAll(pageable);
	}
	// 학과별 학생 조회
	public Page<Student> getStudentsByDep(Long deptId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return studentRepository.findByDepartmentId(deptId, pageable);
	}
	// 학번 학생 조회
	public Page<Student> getStudentsById(Long studentId,int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return studentRepository.findByStudentId(studentId, pageable);
	}
	@Transactional
	public void updateStudentGradeAndSemesters() {
		List<Student> students = studentRepository.findAll();
		
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
	    }
	}
}
