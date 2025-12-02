package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollTuitFormDto;
import com.university.home.entity.CollTuit;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.CollTuitRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

	@Autowired
	CollTuitRepository collTuitRepository;
	
	//===========등록금 관련 메서드================
	@Transactional
	public void createCollTuit(CollTuitFormDto  dto) {
		 if (collTuitRepository.existsByCollegeId(dto.getCollegeId())) {
	            throw new CustomRestfullException("이미 등록금이 입력된 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR);
	        }

	        // DTO → Entity 변환 후 저장
	        CollTuit collTuit = new CollTuit();
	        collTuit.setCollegeId(dto.getCollegeId());
	        collTuit.setAmount(dto.getAmount());

	        collTuitRepository.save(collTuit);
	}
	public List<CollTuitFormDto> getCollTuit() {
		List<CollTuit> collTuitList =collTuitRepository.findAll();
		
		List<CollTuitFormDto> dtoList = collTuitList.stream()
			        .map(c -> {
			            CollTuitFormDto dto = new CollTuitFormDto();
			            dto.setCollegeId(c.getCollegeId());
			            dto.setAmount(c.getAmount());
			            return dto;
			        })
			        .toList();

	   return dtoList;
	}
	@Transactional
	public void deleteCollTuit(Long collegeId) {
		CollTuit collTuit = collTuitRepository.findById(collegeId)
		        .orElseThrow(() -> new 
		        		CustomRestfullException("해당 학과 등록금이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
		collTuitRepository.delete(collTuit);
	}
	@Transactional
	public CollTuit updateCollTuit(CollTuitFormDto dto) {
		CollTuit collTuit = collTuitRepository.findById(dto.getCollegeId())
		        .orElseThrow(() -> new 
		        		CustomRestfullException("해당 학과 등록금이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
		collTuit.setAmount(dto.getAmount());
		return collTuit;
	}
}
