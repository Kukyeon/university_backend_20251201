package com.university.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollTuitFormDto;
import com.university.home.entity.College;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.CollTuitRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

	@Autowired
	CollTuitRepository collTuitRepository;
	
	@Transactional
	public void createCollTuit(CollTuitFormDto  collTuitFormDto) {
		if (collTuitRepository.existsByCollegeId(collTuitFormDto.getCollegeId())) {
	        throw new CustomRestfullException("이미 등록금이 입력된 학과입니다", HttpStatus.INTERNAL_SERVER_ERROR);
	    }

	}
}
