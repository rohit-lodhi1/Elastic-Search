package com.search.app.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.search.app.exception.ResourceNotFoundException;
import com.search.app.payload.ApiResponse;

@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException ex){
		return new ResponseEntity<>(ApiResponse.builder().message(ex.getMessage()).status(HttpStatus.NOT_FOUND.value()).build(),HttpStatus.NOT_FOUND);
	}
}
