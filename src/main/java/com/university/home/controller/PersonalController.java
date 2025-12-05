package com.university.home.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.university.home.config.WebMvcConfig;
import com.university.home.dto.FindUserDto;
import com.university.home.dto.UserDto;
import com.university.home.dto.UserPwDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
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

	
	private String generateRandomPassword(int length) {
	    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < length; i++) {
	        int idx = (int) (Math.random() * chars.length());
	        sb.append(chars.charAt(idx));
	    }
	    return sb.toString();
	}
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
	        return ResponseEntity.ok(result);
	    }
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid UserDto dto, BindingResult bindingResult) {
	    if (bindingResult.hasErrors()) {
	        StringBuilder sb = new StringBuilder();
	        bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
	        throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
	    }

	    
	    User user = userService.login(dto.getId(), dto.getPassword());
	    
	    String token = jwtUtil.generateToken(user);
	    
	    Object result;
	    switch(user.getUserRole()) {
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

	    // 3️⃣ 반환
	    return ResponseEntity.ok(Map.of(
	            "token", token,
	            "user", result
	        ));
	}
	@PostMapping("/findId")
	public ResponseEntity<?> findId(@RequestBody @Valid FindUserDto dto, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
	        StringBuilder sb = new StringBuilder();
	        bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
	        throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
	    }
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
            throw new CustomRestfullException("Invalid role", HttpStatus.BAD_REQUEST);
    }
		return ResponseEntity.ok(Map.of("id", id, "message", "ID 조회 성공"));
	}
	@PostMapping("/findPw")
	public ResponseEntity<?> findPw(@RequestBody @Valid FindUserDto dto, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
	        StringBuilder sb = new StringBuilder();
	        bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
	        throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
	    }
		 boolean exists = switch(dto.getUserRole()) {
	        case "student" -> studentService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        case "professor" -> professorService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        case "staff" -> staffService.checkExistsForPasswordReset(dto.getId(), dto.getName(), dto.getEmail());
	        default -> false;
	    };
	    
	    if (!exists) {
	        throw new CustomRestfullException("정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
	    }
	 // 2️⃣ 랜덤 임시 비밀번호 생성
	    String tempPassword = generateRandomPassword(8);

	    // 3️⃣ User에 임시 비밀번호 설정
	    userService.resetPassword(dto.getId(), tempPassword);

	    return ResponseEntity.ok(Map.of("message", "임시 비밀번호 발급 완료", "tempPassword", tempPassword));
	}
	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestBody @Valid UserUpdateDto dto, BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetails loginUser) {
		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}
		dto.setId(loginUser.getUser().getId());
		 Object updatedUser = userService.updateUser(dto);

		    // 최신 객체를 그대로 반환
		    return ResponseEntity.ok(updatedUser);
	}
	@PutMapping("/update/pw")
	public ResponseEntity<?> updatePassword(@RequestBody@Valid UserPwDto dto, BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetails loginUser ){
	if (bindingResult.hasErrors()) {
        StringBuilder sb = new StringBuilder();
        bindingResult.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append("\n"));
        throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
    }
	dto.setUserId(loginUser.getUser().getId());
	userService.updatePw(dto);
	return ResponseEntity.ok("비밀번ㄴ호 변경!");
}
	
}
