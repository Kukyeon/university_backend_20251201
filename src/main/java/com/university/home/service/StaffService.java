package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.StaffDto;
import com.university.home.entity.Staff;
import com.university.home.repository.StaffRepository;

import jakarta.transaction.Transactional;

@Service
public class StaffService {

	@Autowired
	StaffRepository staffRepository;
	
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
}
