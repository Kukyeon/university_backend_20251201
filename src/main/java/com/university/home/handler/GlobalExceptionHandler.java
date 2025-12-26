package com.university.home.handler;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.university.home.exception.CustomRestfullException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	 @ExceptionHandler(CustomRestfullException.class)
	    public ResponseEntity<Map<String, Object>> handleCustomException(CustomRestfullException ex) {
	        Map<String, Object> errorBody = Map.of(
	            "success", false,
	            "message", ex.getMessage()
	        );
	        return new ResponseEntity<>(errorBody, ex.getStatus());
	    }
	 @ExceptionHandler(MethodArgumentNotValidException.class)
	 public ResponseEntity<Map<String, Object>> handleValidationException(
	         MethodArgumentNotValidException ex) {

	     String message = ex.getBindingResult()
	             .getFieldErrors()
	             .stream()
	             .findFirst()
	             .map(error -> error.getDefaultMessage())
	             .orElse("요청 값이 올바르지 않습니다.");

	     return ResponseEntity.badRequest().body(
	             Map.of(
	                 "success", false,
	                 "message", message
	             )
	     );
	 }
	 @ExceptionHandler(DataIntegrityViolationException.class)
	 public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(Exception ex) {
	     return new ResponseEntity<>(
	         Map.of(
	             "success", false, 
	             "message", "데이터 제약 조건 위반으로 삭제할 수 없습니다. 연결된 다른 정보가 있는지 확인해주세요."
	         ),
	         HttpStatus.CONFLICT
	     );
	 }

}
