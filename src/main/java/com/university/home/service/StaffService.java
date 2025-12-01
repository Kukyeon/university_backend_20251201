package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.StaffDto;
import com.university.home.entity.Staff;
import com.university.home.entity.Student;
import com.university.home.repository.StaffRepository;
import com.university.home.repository.StudentRepository;
import jakarta.transaction.Transactional;

@Service
public class StaffService {

    private final StudentRepository studentRepository;

	@Autowired
	StaffRepository staffRepository;

    StaffService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
	
	@Transactional
	public Long createStaff(StaffDto staffDto) {
		Staff staff = new Staff();
		staff.setName(staffDto.getName());
		staff.setAddress(staffDto.getAddress());
		staff.setBirthDate(staffDto.getBirthDate());
		staff.setEmail(staffDto.getEmail());
		staff.setGender(staffDto.getGender());
		staff.setTel(staffDto.getTel());
		staffRepository.save(staff);
		
		return staff.getId();
		
	}
	@Transactional
	public Staff readStaff(Long id) {
		return staffRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Staff not found"));
	}
	@Transactional
	public void updateStaff (StaffDto dto) {
		Staff staff = staffRepository.findById(dto.getId())
				.orElseThrow(() -> new RuntimeException("Staff not found"));
		staff.setTel(dto.getTel());
		staff.setAddress(dto.getAddress());;
		staff.setEmail(dto.getEmail());
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return staffRepository.findByNameAndEmail(name, email)
				.map(Staff::getId)
				.orElseThrow(() -> new RuntimeException("Staff not found"));
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return staffRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public List<Staff> getAllStaffs() {
		return staffRepository.findAll();
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
