package com.university.home.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.StuSchDto;
import com.university.home.entity.Scholarship;
import com.university.home.entity.StuSch;
import com.university.home.entity.Student;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.StuSchRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;

@Service
public class StuSchService {

	@Autowired
	StudentRepository studentRepository;
	@Autowired
	StuSchRepository stuSchRepository;
	@Autowired
	ScholarshipRepository scholarshipRepository;
	public StuSchDto toDto(StuSch stuSch) {
		Scholarship sch = stuSch.getScholarshipType();
	    return new StuSchDto(
	        stuSch.getId(),
	        stuSch.getStudent().getId(),
	        stuSch.getSchYear(),
	        stuSch.getSemester(),
	        sch != null ? sch.getType() : null,
            sch != null ? sch.getMaxAmount() : null
	    );
	}
	   // 학생 장학금 리스트 조회
    public List<StuSchDto> getByStudent(Long studentId) {
	    Student student = studentRepository.findById(studentId)
	            .orElseThrow(() -> new CustomRestfullException("학생을 찾을 수 없습니다.", null));
	    return stuSchRepository.findByStudent(student)
	            .stream()
	            .map(this::toDto)
	            .collect(Collectors.toList());
    }
    // 장학금 신청 생성
    @Transactional
    public StuSchDto createStuSch(Long studentId, Long scholarshipTypeId, Long schYear, Long semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomRestfullException("학생을 찾을 수 없습니다.", null));
        Scholarship sch = scholarshipRepository.findById(scholarshipTypeId)
                .orElseThrow(() -> new CustomRestfullException("장학금을 찾을 수 없습니다.", null));

        StuSch stuSch = new StuSch();
        stuSch.setStudent(student);
        stuSch.setScholarshipType(sch);
        stuSch.setSchYear(schYear);
        stuSch.setSemester(semester);

        stuSchRepository.save(stuSch);
        return toDto(stuSch);
    }

    // 장학금 삭제
    @Transactional
    public void deleteStuSch(Long id) {
        if (!stuSchRepository.existsById(id)) {
            throw new CustomRestfullException("장학금 내역이 없습니다.", null);
        }
        stuSchRepository.deleteById(id);
    }
}
