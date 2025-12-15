package com.university.home.handler;

import java.util.Map;

import org.springframework.http.ResponseEntity;
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
}
