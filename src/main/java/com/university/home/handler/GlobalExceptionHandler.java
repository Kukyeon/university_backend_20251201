package com.university.home.handler;

import java.util.Map;

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

}
