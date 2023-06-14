package com.tuum.testassignment.api;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionCreateResponse;
import com.tuum.testassignment.service.TransactionService;
import com.tuum.testassignment.validation.OrderedValidation;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
	private final TransactionService transactionService;

	@PostMapping
	public ResponseEntity<?> create(
		@Validated(OrderedValidation.class)
		@RequestBody TransactionCreateInput input
	) {
		TransactionCreateResponse response = transactionService.create(input);

		return new ResponseEntity<>(
			response,
			HttpStatus.CREATED
		);
	}
}
