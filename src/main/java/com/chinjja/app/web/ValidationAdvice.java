package com.chinjja.app.web;

import javax.validation.ValidationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ValidationAdvice {
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<?> handle(ValidationException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> illegal(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}
}
