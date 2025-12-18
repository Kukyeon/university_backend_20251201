package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollegeDto;
import com.university.home.entity.College;
import com.university.home.repository.CollegeRepository;

@Service
public class CollegeService {

	@Autowired
	CollegeRepository collegeRepository;
	
	public List<College> collegeList() {
		return collegeRepository.findAll();
	}
	// 등록
    public College createCollege(CollegeDto dto) {
        College college = new College();
        college.setName(dto.getName());
        return collegeRepository.save(college);
    }

    // 삭제
    public void deleteCollege(Long id) {
        collegeRepository.deleteById(id);
    }
}
