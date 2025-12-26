package com.university.home.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.university.home.dto.BreakAppDto;
import com.university.home.dto.FindUserDto;
import com.university.home.dto.UserDto;
import com.university.home.dto.UserPwDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.BreakApp;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.BreakAppService;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.ProfessorService;
import com.university.home.service.StaffService;
import com.university.home.service.StudentService;
import com.university.home.service.UserService;
import com.university.home.utils.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class PersonalController {


	@Autowired
	ProfessorService professorService;
	@Autowired
	StaffService staffService;
	@Autowired
	StudentService studentService;
	@Autowired
	UserService userService;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	BreakAppService breakAppService;

	// 비밀번호 찾기용 랜덤 비밀번호
	private String generateRandomPassword(int length) {
	    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < length; i++) {
	        int idx = (int) (Math.random() * chars.length());
	        sb.append(chars.charAt(idx));
	    }
	    return sb.toString();
	}
	// App.js에서 받을 user 객체
	 @GetMapping("/me")
	    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails loginUser) {
	        User user = userService.getUserById(loginUser.getUser().getId());
	        Long userId = user.getId();   
	        String role = user.getUserRole();
	        Object result;
	        
	        switch(role) {
	        case "student":
	            result = studentService.readStudent(userId);
	            break;
	        case "professor":
	        	result = professorService.readProfessor(userId);
	            break;
	        case "staff":
	        	result = staffService.readStaff(userId);
	            break;
	        default:
	            throw new CustomRestfullException("Unknown user role", HttpStatus.BAD_REQUEST);
	    }
	        return ResponseEntity.ok(Map.of(
	                "user", result,
	                "role", role
	            ));
	    }
	 // 로그인
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid UserDto dto) {
	    
	    User user = userService.login(dto.getId(), dto.getPassword());
	    String role = user.getUserRole();
	    String token = jwtUtil.generateToken(user);
	    
	    Object result;
	    switch(role) {
	        case "student":
	            result = studentService.readStudent(user.getId());
	            break;
	        case "professor":
	            result = professorService.readProfessor(user.getId());
	            break;
	        case "staff":
	            result = staffService.readStaff(user.getId());
	            break;
	        default:
	            throw new CustomRestfullException("Unknown user role", HttpStatus.BAD_REQUEST);
	    }

	    return ResponseEntity.ok(Map.of(
	            "token", token,
	            "user", result,
	            "role", role
	        ));
	}
	// 아이디 찾기
	@PostMapping("/findId")
	public ResponseEntity<?> findId(@RequestBody @Valid FindUserDto dto) {
		Long id;
		switch(dto.getUserRole().toLowerCase()) {
        case "student":
            id = studentService.findByNameEmail(dto.getName(), dto.getEmail());
            break;
        case "professor":
            id = professorService.findByNameEmail(dto.getName(), dto.getEmail());
            break;
        case "staff":
            id = staffService.findByNameEmail(dto.getName(), dto.getEmail());
            break;
        default:
            throw new CustomRestfullException("아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
    }
		return ResponseEntity.ok(Map.of("id", id, "message", "ID 조회 성공"));
	}
	// 비밀번호 찾기
	@PostMapping("/findPw")
	public ResponseEntity<?> findPw(@RequestBody @Valid FindUserDto dto) {
		 boolean exists = switch(dto.getUserRole()) {
	        case "student" -> studentService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        case "professor" -> professorService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        case "staff" -> staffService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        default -> false;
	    };
	    
	    if (!exists) {
	        throw new CustomRestfullException("정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
	    }
	    String tempPassword = generateRandomPassword(8);

	    userService.resetPassword(dto.getId(), tempPassword);

	    return ResponseEntity.ok(Map.of("message", "임시 비밀번호 발급 완료", "tempPassword", tempPassword));
	}
	// 내정보 수정
	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestBody @Valid UserUpdateDto dto, @AuthenticationPrincipal CustomUserDetails loginUser) {
		dto.setId(loginUser.getUser().getId());
		 Object updatedUser = userService.updateUser(dto);

		    return ResponseEntity.ok(updatedUser);
	}
	// 비밀번호 변경
	@PutMapping("/update/pw")
	public ResponseEntity<?> updatePassword(@RequestBody@Valid UserPwDto dto, @AuthenticationPrincipal CustomUserDetails loginUser ){
	dto.setUserId(loginUser.getUser().getId());
	userService.updatePw(dto);
	return ResponseEntity.ok("비밀번호 변경!");
	}
	// 학생 MY에서 학적상태 조회
	@GetMapping("/students")
	public ResponseEntity<?> getStudentBreakApps(@AuthenticationPrincipal CustomUserDetails loginUser){
	    Long studentId = loginUser.getUser().getId();
		List<BreakApp> apps = breakAppService.getByStudent(studentId);
	    List<BreakAppDto> dtos = apps.stream().map(b -> breakAppService.toDto(b)).toList();
	    return ResponseEntity.ok(dtos);
	}
}
