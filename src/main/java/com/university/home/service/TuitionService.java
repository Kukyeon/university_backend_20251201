package com.university.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.StuSchRepository;

@Service
public class TuitionService {

	@Autowired
	ScholarshipRepository scholarshipRepository;
	@Autowired
	StuSchRepository stuSchRepository;
}
