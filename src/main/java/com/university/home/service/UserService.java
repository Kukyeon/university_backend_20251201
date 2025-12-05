package com.university.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.university.home.dto.UserPwDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.Professor;
import com.university.home.entity.Staff;
import com.university.home.entity.Student;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StaffRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	StudentRepository studentRepository;
	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	StaffRepository staffRepository;
	@Autowired
	PasswordEncoder encoder;
	
	@Transactional
	public User createUser(Long id, String role) {
		User user = new User();
		user.setId(id);
		user.setUserRole(role);
		user.setPassword(encoder.encode(id.toString()));
		
		return userRepository.save(user);
	}
	@Transactional
	public User login(Long id, String rawPassword) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException("User not found", HttpStatus.NOT_FOUND));
		if (!encoder.matches(rawPassword, user.getPassword())) {
			throw new CustomRestfullException("Invalid password", HttpStatus.NOT_FOUND);
		}
		return user;
	}
	 @Transactional
	    public void resetPassword(Long id, String tempPassword) {
	        User user = userRepository.findById(id)
	                .orElseThrow(() -> new CustomRestfullException("User not found", HttpStatus.NOT_FOUND));
	        user.setPassword(encoder.encode(tempPassword));
	    }
	 @Transactional
	 public Object updateUser(UserUpdateDto dto) {
	        Long userId = dto.getId();

	        // student, professor, staff 중 먼저 존재하는 엔티티 찾기
	        if (studentRepository.existsById(userId)) {
	            Student student = studentRepository.findById(userId)
	                    .orElseThrow(() -> new RuntimeException("Student not found"));
	            student.setTel(dto.getTel());
	            student.setAddress(dto.getAddress());
	            student.setEmail(dto.getEmail());
	            return student;
	        } else if (professorRepository.existsById(userId)) {
	            Professor professor = professorRepository.findById(userId)
	                    .orElseThrow(() -> new RuntimeException("Professor not found"));
	            professor.setTel(dto.getTel());
	            professor.setAddress(dto.getAddress());
	            professor.setEmail(dto.getEmail());
	            return professor;
	        } else if (staffRepository.existsById(userId)) {
	            Staff staff = staffRepository.findById(userId)
	                    .orElseThrow(() -> new RuntimeException("Staff not found"));
	            staff.setTel(dto.getTel());
	            staff.setAddress(dto.getAddress());
	            staff.setEmail(dto.getEmail());
	            return staff;
	        } else {
	            throw new RuntimeException("User not found");
	        }
	    }
	 @Transactional
	 public void updatePw(UserPwDto dto) {
		 Long userId = dto.getUserId();
		 User user = userRepository.findById(userId)
		            .orElseThrow(() -> new RuntimeException("User not found"));
		 if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
			 throw new RuntimeException("Incorrect current password");
		}
		 user.setPassword(encoder.encode(dto.getNewPassword()));
	 }
}
