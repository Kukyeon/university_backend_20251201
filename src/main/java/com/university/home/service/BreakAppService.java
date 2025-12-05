package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.BreakAppDto;
import com.university.home.entity.BreakApp;
import com.university.home.entity.Student;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.BreakAppRepository;

import jakarta.transaction.Transactional;

@Service
public class BreakAppService {

    private final StuStatService stuStatService;

	@Autowired
	BreakAppRepository breakAppRepository;
	@Autowired
	StudentService studentService;

    BreakAppService(StuStatService stuStatService) {
        this.stuStatService = stuStatService;
    }
	
	public List<BreakApp> getByStudent(Long studentId) {
		Student student = studentService.getStudentById(studentId);
		List<BreakApp> breakAppList = breakAppRepository
				.findByStudentOrderByIdDesc(student);
		
		return breakAppList;
	}
	public BreakApp getById(Long id) {
		return breakAppRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException("휴학 신청 내역이 없습니다.", HttpStatus.NOT_FOUND));
	}
	
	public void createBreakApp(BreakAppDto dto) {
		
		List<BreakApp> breakApps = getStudentApps(dto.getStudentId());
		for (BreakApp b : breakApps) {
			if(b.getStatus().equals("처리중")) {
				throw new CustomRestfullException("이미 처리중인 신청내역이 존재합니다.", HttpStatus.BAD_REQUEST);
			}
		}
		Student student = studentService.getStudentById(dto.getStudentId());
		BreakApp breakApp = new BreakApp();
		breakApp.setStudent(student);
        breakApp.setFromYear(dto.getFromYear());
        breakApp.setFromSemester(dto.getFromSemester());
        breakApp.setToYear(dto.getToYear());
        breakApp.setToSemester(dto.getToSemester());
        breakApp.setType(dto.getType());
        breakApp.setStatus("처리중");
        breakApp.setAppDate(LocalDate.now());

        breakAppRepository.save(breakApp);
	}
	public List<BreakApp> getStudentApps(Long studentId) {
		List<BreakApp> breakApps = breakAppRepository.findByStudentId(studentId);
		
		return breakApps;
	}
	@Transactional
	public void deleteApp(Long id) {
		BreakApp app = getById(id);
		
		if (!app.getStatus().equals("처리중")) {
			throw new CustomRestfullException("이미 처리가 완료되어 취소가 불가합니다.", HttpStatus.BAD_REQUEST);
		}
		breakAppRepository.deleteById(id);
	}
	@Transactional
	public void updateStatus(Long id, String status) {
		BreakApp app = getById(id);
		
		app.setStatus(status);
		breakAppRepository.save(app);
		
		if (status.equals("승인")) {
			LocalDate toDate;
			Student student = studentService.getStudentById(app.getStudent().getId());
			if (app.getToSemester() ==1) {
				toDate = LocalDate.of(app.getToYear().intValue(), 8, 31);
			} else {
				toDate = LocalDate.of(app.getToYear().intValue() + 1, 2, 28);
			}
			stuStatService.updateStatus(student,"휴학", app.getId());
		}
	}
	
}
