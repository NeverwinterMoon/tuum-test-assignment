package com.tuum.testassignment.api;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuum.testassignment.model.Transaction;
import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionCreateResponse;
import com.tuum.testassignment.service.TransactionService;
import com.tuum.testassignment.validation.AccountIDIsValid;
import com.tuum.testassignment.validation.OrderedValidation;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
@Validated // because we are doing @RequestParam validation
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

	@GetMapping("/list")
	public ResponseEntity<?> list(
		@Valid
		@AccountIDIsValid(message = "It looks like the account with the provided ID is missing.")
		@RequestParam Integer accountID
	) {
		List<Transaction> transactionList = transactionService.list(accountID);

		return new ResponseEntity<>(
			transactionList,
			HttpStatus.OK
		);
	}
}
