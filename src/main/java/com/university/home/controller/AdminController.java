package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.university.home.dto.CollTuitFormDto;
import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.dto.RoomDto;
import com.university.home.dto.SubjectDto;
import com.university.home.entity.CollTuit;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.service.AdminService;
import com.university.home.service.CollegeService;
import com.university.home.service.DepartmentService;
import com.university.home.service.RoomService;
import com.university.home.service.SubjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	
	@Autowired
	AdminService adminService;
	@Autowired
	CollegeService collegeService;
	@Autowired
	DepartmentService departmentService;
	@Autowired
	RoomService roomService;
	@Autowired
	SubjectService subjectService;
	// 단과대 목록
	@GetMapping("/college")
	public ResponseEntity<?> getCollegeList() {
		List<College> colleges = collegeService.collegeList();
		return ResponseEntity.ok(colleges);
	}
	// 단과대 등록
	@PostMapping("/college")
	public ResponseEntity<?> addCollege(@RequestBody CollegeDto dto) {
		College college = collegeService.createCollege(dto);
		return ResponseEntity.ok(college);
	}
	// 단과대 삭제
	@DeleteMapping("/college/{id}")
	public ResponseEntity<?> deleteCollege(@PathVariable(name = "id") Long id) {
		collegeService.deleteCollege(id);
		return ResponseEntity.ok("삭제 완료");
	}
	// 학과 목록
	@GetMapping("/department")
	public ResponseEntity<?> getDepartmentList() {
		List<Department> departments = departmentService.departmentList();
		List<DepartmentDto> dtoList = departments.stream()
                .map(departmentService::toDto)
                .toList();
		return ResponseEntity.ok(dtoList);
	}
	// 학과 등록
	@PostMapping("/department")
	public ResponseEntity<?> addDepartment(@RequestBody @Valid DepartmentDto dto) {
		departmentService.createDepartment(dto, dto.getCollege().getId());
		    
		    return ResponseEntity.ok(dto);
	}
	// 학과 수정
	@PutMapping("/department/{id}")
	public ResponseEntity<?> updateDepartment(@PathVariable(name = "id") Long id, @RequestBody @Valid DepartmentDto dto) {
		departmentService.updateDepartment(id, dto);
		    return ResponseEntity.ok(dto);
	}
	// 학과 삭제
	@DeleteMapping("/department/{id}")
	public ResponseEntity<?> deleteDepartment(@PathVariable(name = "id") Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok( "삭제 완료");
    }
	// 강의실 목록
	@GetMapping("/room")
	public ResponseEntity<?> getRoomList(Pageable pageable) {
		Page<RoomDto> rooms = roomService.roomList(pageable);
		return ResponseEntity.ok(rooms);
	}
	// 강의실 등록
    @PostMapping("/room")
    public ResponseEntity<?> addRoom(@RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.createRoom(dto));
    }
    // 강의실 삭제
    @DeleteMapping("/room/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable(name = "id") String id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("삭제 완료");
    }
    // 강의 목록
    @GetMapping("/subject")
	public ResponseEntity<?> getSubject(Pageable pageable) {
		Page<SubjectDto> subjects = subjectService.getSubjects(pageable);
		return ResponseEntity.ok(subjects);
	}
    // 강의 수정
    @PutMapping("/subject/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable(name = "id") Long id, @RequestBody @Valid SubjectDto dto){
    	subjectService.updateSubject(id, dto);
    	return ResponseEntity.ok(dto);
    }
    // 강의 등록
    @PostMapping("/subject")
    public ResponseEntity<?> addSubject(@RequestBody @Valid SubjectDto dto) {
        return ResponseEntity.ok(subjectService.createSubject(dto));
    }
    // 강의 삭제
    @DeleteMapping("/subject/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable(name = "id") Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok("삭제 완료");
    }
    // 학과등록금 목록
	@GetMapping("/tuition")
	public ResponseEntity<?> getTuitionList() {
		List<CollTuitFormDto> tuits = adminService.getCollTuit();
		return ResponseEntity.ok(tuits);
	}
   // 학과등록금 등록
	@PostMapping("/tuition")
	public ResponseEntity<?> createTuition(@Valid@RequestBody CollTuitFormDto dto) {
		adminService.createCollTuit(dto);
		 return ResponseEntity.ok("등록금 등록 완료");
	}
   // 학과등록금 수정
	@PutMapping("/tuition")
	public ResponseEntity<?> updateTuition(@Valid@RequestBody CollTuitFormDto dto){
		CollTuit updated = adminService.updateCollTuit(dto);
		return ResponseEntity.ok(updated);
	}
   // 학과등록금 삭제
	@DeleteMapping("/tuition/{collegeId}")
	public ResponseEntity<?> deleteTuition(@PathVariable("collegeId") Long collegeId) {
		adminService.deleteCollTuit(collegeId);
	 return ResponseEntity.ok("등록금 삭제 완료");
	}
}
