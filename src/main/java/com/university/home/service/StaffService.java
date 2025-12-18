package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.StaffDto;
import com.university.home.entity.Staff;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.StaffRepository;
import jakarta.transaction.Transactional;

@Service
public class StaffService {


	@Autowired
	StaffRepository staffRepository;
	@Autowired
	UserService userService;

	public StaffDto toDto(Staff staff) {
	    StaffDto dto = new StaffDto();
	    dto.setId(staff.getId());
	    dto.setName(staff.getName());
	    dto.setBirthDate(staff.getBirthDate());
	    dto.setGender(staff.getGender());
	    dto.setAddress(staff.getAddress());
	    dto.setTel(staff.getTel());
	    dto.setEmail(staff.getEmail());
	    dto.setHireDate(staff.getHireDate());


	    return dto;
	}
	@Transactional
	public StaffDto createStaff(StaffDto staffDto) {
		Staff staff = new Staff();
		staff.setName(staffDto.getName());
		staff.setAddress(staffDto.getAddress());
		staff.setBirthDate(staffDto.getBirthDate());
		staff.setEmail(staffDto.getEmail());
		staff.setGender(staffDto.getGender());
		staff.setTel(staffDto.getTel());
		staff.setHireDate(LocalDate.now());
		staffRepository.save(staff);
		
		User user = userService.createUser(staff.getId(), "staff");
		staff.setUser(user);
		staffRepository.save(staff);
		
		return toDto(staff);
		
	}
	@Transactional
	public Staff readStaff(Long id) {
		return staffRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException(
                        "직원 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return staffRepository.findByNameAndEmail(name, email)
				.map(Staff::getId)
				.orElseThrow(() -> new CustomRestfullException(
                        "직원 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return staffRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public List<Staff> getAllStaffs() {
		return staffRepository.findAll();
	}

}
