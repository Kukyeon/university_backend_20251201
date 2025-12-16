package com.university.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.university.home.entity.Room;
import com.university.home.entity.Subject;
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
	@GetMapping("/college")
	public ResponseEntity<?> getCollegeList() {
		List<College> colleges = collegeService.collegeList();
		return ResponseEntity.ok(colleges);
	}
	@PostMapping("/college")
	public ResponseEntity<?> addCollege(@RequestBody CollegeDto dto) {
		College college = collegeService.createCollege(dto);
		return ResponseEntity.ok(college);
	}
	@DeleteMapping("/college/{id}")
	public ResponseEntity<?> deleteCollege(@PathVariable(name = "id") Long id) {
		collegeService.deleteCollege(id);
		return ResponseEntity.ok("삭제 완료");
	}
	@GetMapping("/department")
	public ResponseEntity<?> getDepartmentList() {
		List<Department> departments = departmentService.departmentList();
		List<DepartmentDto> dtoList = departments.stream()
                .map(departmentService::toDto)
                .toList();
		return ResponseEntity.ok(dtoList);
	}
	@PostMapping("/department")
	public ResponseEntity<?> addDepartment(@RequestBody @Valid DepartmentDto dto) {
		departmentService.createDepartment(dto, dto.getCollege().getId());
		    
		    return ResponseEntity.ok(dto);
	}
	@PutMapping("/department/{id}")
	public ResponseEntity<?> updateDepartment(@PathVariable(name = "id") Long id, @RequestBody @Valid DepartmentDto dto) {
		departmentService.updateDepartment(id, dto);
		    return ResponseEntity.ok(dto);
	}
	@DeleteMapping("/department/{id}")
	public ResponseEntity<?> deleteDepartment(@PathVariable(name = "id") Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok( "삭제 완료");
    }
	@GetMapping("/room")
	public ResponseEntity<?> getRoomList() {
		List<RoomDto> rooms = roomService.roomList();
		return ResponseEntity.ok(rooms);
	}

    @PostMapping("/room")
    public ResponseEntity<?> addRoom(@RequestBody RoomDto dto) {
        return ResponseEntity.ok(roomService.createRoom(dto));
    }

    @DeleteMapping("/room/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable(name = "id") String id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("삭제 완료");
    }
    @GetMapping("/subject")
	public ResponseEntity<?> getSubject() {
		List<SubjectDto> subjects = subjectService.getSubjects();
		return ResponseEntity.ok(subjects);
	}
    @PutMapping("/subject/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable(name = "id") Long id, @RequestBody @Valid SubjectDto dto){
    	subjectService.updateSubject(id, dto);
    	return ResponseEntity.ok(dto);
    }
    @PostMapping("/subject")
    public ResponseEntity<?> addSubject(@RequestBody @Valid SubjectDto dto) {
        return ResponseEntity.ok(subjectService.createSubject(dto));
    }

    @DeleteMapping("/subject/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable(name = "id") Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok("삭제 완료");
    }
	@GetMapping("/tuition")
	public ResponseEntity<?> getTuitionList() {
		List<CollTuitFormDto> tuits = adminService.getCollTuit();
		return ResponseEntity.ok(tuits);
	}
	@PostMapping("/tuition")
	public ResponseEntity<?> createTuition(@Valid@RequestBody CollTuitFormDto dto) {
		adminService.createCollTuit(dto);
		 return ResponseEntity.ok("등록금 등록 완료");
	}
	@PutMapping("/tuition")
	public ResponseEntity<?> updateTuition(@Valid@RequestBody CollTuitFormDto dto){
		CollTuit updated = adminService.updateCollTuit(dto);
		return ResponseEntity.ok(updated);
	}
	@DeleteMapping("/tuition/{collegeId}")
	public ResponseEntity<?> deleteTuition(@PathVariable("collegeId") Long collegeId) {
		adminService.deleteCollTuit(collegeId);
	 return ResponseEntity.ok("등록금 삭제 완료");
	}
}
