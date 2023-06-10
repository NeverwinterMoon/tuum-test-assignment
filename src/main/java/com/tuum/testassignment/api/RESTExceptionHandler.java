package com.tuum.testassignment.api;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.tuum.testassignment.error.CustomValidationException;
import com.tuum.testassignment.model.TransactionDirection;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class RESTExceptionHandler {
	@ExceptionHandler(CustomValidationException.class)
	public ResponseEntity<Object> handleCustomValidationException(CustomValidationException exception) {
		Map<String, String> errorMap = Map.of(exception.getField(), exception.getMessage());

		return new ResponseEntity<>(
			errorMap,
			exception.getStatus()
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException exception) {
		List<ObjectError> errorList = exception.getBindingResult().getAllErrors();

		Map<String, String> fieldErrorMap = errorList.stream()
			.filter(error -> error instanceof FieldError)
			.map(error -> (FieldError) error)
			.map(error -> {
				String fieldName = (error).getField();
				String errorMessage = error.getDefaultMessage();

				return new AbstractMap.SimpleEntry<>(fieldName, errorMessage);
			})
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(oldValue, newValue) -> newValue)
			);

		List<ObjectError> genericErrorList = errorList.stream()
			.filter(error -> !(error instanceof FieldError))
			.toList();

		Map<String, String> genericErrorMap = IntStream.range(0, genericErrorList.size())
			.mapToObj(index -> new AbstractMap.SimpleEntry<>(
				"inputError_" + (index + 1),
				genericErrorList.get(index).getDefaultMessage()
			))
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(oldValue, newValue) -> newValue)
			);

		Map<String, String> errorMap = Stream.concat(fieldErrorMap.entrySet().stream(),
				genericErrorMap.entrySet().stream())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(oldValue, newValue) -> newValue
			));

		return new ResponseEntity<>(
			errorMap,
			HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Object> handleValidationErrors(HttpMessageNotReadableException exception) {
		if (exception.getCause() instanceof JsonMappingException) {
			if (((JsonMappingException) exception.getCause()).getPathReference().contains("direction")) {
				return new ResponseEntity<>(
					new AbstractMap.SimpleEntry<>(
						"direction",
						"Unsupported transaction direction. Supported values are [" + Stream.of(TransactionDirection.values())
							.map(Enum::name)
							.collect(Collectors.joining(", ")) + "]."
					),
					HttpStatus.BAD_REQUEST
				);
			}
		}

		return new ResponseEntity<>(
			exception.getMessage(),
			HttpStatus.BAD_REQUEST
		);
	}

	// For validation on @RequestParam
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException exception) {
		Map<String, String> errorMap = exception.getConstraintViolations().stream()
			.map(violation -> {
				String fieldName = violation.getPropertyPath().toString().replaceFirst("^list\\.", "");
				String errorMessage = violation.getMessage();

				return new AbstractMap.SimpleEntry<>(fieldName, errorMessage);
			})
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(oldValue, newValue) -> newValue)
			);

		return new ResponseEntity<>(
			errorMap,
			HttpStatus.BAD_REQUEST
		);
	}
}
