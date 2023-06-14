package com.tuum.testassignment.api;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;
import com.tuum.testassignment.model.Transaction;
import com.tuum.testassignment.service.AccountService;
import com.tuum.testassignment.service.TransactionService;
import com.tuum.testassignment.validation.AccountIDIsValid;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
@Validated // because we are doing @PathVariable validation
public class AccountController {
	private final AccountService accountService;

	private final TransactionService transactionService;

	@GetMapping("{accountID}")
	public ResponseEntity<?> fetchAccount(@PathVariable Integer accountID) {
		Account account = accountService.account(accountID);

		return new ResponseEntity<>(
			account,
			HttpStatus.OK
		);
	}

	@PostMapping
	public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreateInput input) {
		Account account = accountService.create(input);

		return new ResponseEntity<>(
			account,
			HttpStatus.CREATED
		);
	}

	@GetMapping("{accountID}/transaction")
	public ResponseEntity<?> fetchTransactionListForAccount(
		@AccountIDIsValid(message = "It looks like the account with the provided ID is missing.")
		@PathVariable Integer accountID
	) {
		System.out.println("Here " + accountID);
		List<Transaction> transactionList = transactionService.list(accountID);
		System.out.println("transactionList " + transactionList);
		return new ResponseEntity<>(
			transactionList,
			HttpStatus.OK
		);
	}
}
