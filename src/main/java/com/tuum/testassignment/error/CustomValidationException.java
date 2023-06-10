package com.tuum.testassignment.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatusCode;

@AllArgsConstructor
@Getter
public class CustomValidationException extends RuntimeException {
	private String field;
	private String message;
	private HttpStatusCode status;
}
