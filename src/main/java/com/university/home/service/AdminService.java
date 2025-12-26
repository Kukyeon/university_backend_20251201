package com.university.home.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollTuitFormDto;
import com.university.home.entity.CollTuit;
import com.university.home.entity.College;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.CollTuitRepository;
import com.university.home.repository.CollegeRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

	@Autowired
	CollTuitRepository collTuitRepository;
	@Autowired
	CollegeRepository collegeRepository;
	
	//===========등록금 관련 메서드================
	@Transactional
	public void createCollTuit(CollTuitFormDto  dto) {
		 if (collTuitRepository.existsById(dto.getCollegeId())) {
	            throw new CustomRestfullException("이미 등록금이 입력된 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR);
	        }

	        CollTuit collTuit = new CollTuit();
	        College college = collegeRepository.findById(dto.getCollegeId())
	        	    .orElseThrow(() -> new RuntimeException("College not found"));
	        collTuit.setCollege(college);
	        collTuit.setAmount(dto.getAmount());

	        collTuitRepository.save(collTuit);
	}
	public List<CollTuitFormDto> getCollTuit() {
		List<College> colleges = collegeRepository.findAll();
		    // 등록금 정보 가져오기
		List<CollTuit> collTuitList = collTuitRepository.findAll();
		
		List<CollTuitFormDto> dtoList = colleges.stream()
				 .map(c -> {
				        CollTuitFormDto dto = new CollTuitFormDto();
				        dto.setCollegeId(c.getId());
			            dto.setCollegeName(c.getName());
				        
			            collTuitList.stream()
		                .filter(t -> t.getCollege().getId().equals(c.getId()))
		                .findFirst()
		                .ifPresent(t -> dto.setAmount(t.getAmount()));

			            return dto;
				    })
				    .collect(Collectors.toList());

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
