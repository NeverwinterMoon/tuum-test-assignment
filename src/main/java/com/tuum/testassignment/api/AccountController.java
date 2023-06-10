package com.tuum.testassignment.api;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;
import com.tuum.testassignment.service.AccountService;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {
	private final AccountService accountService;

	@GetMapping
	public ResponseEntity<?> account(@RequestParam Integer accountID) {
		Account account = accountService.account(accountID);

		return new ResponseEntity<>(
			account,
			HttpStatus.OK
		);
	}

	@PostMapping
	public ResponseEntity<?> create(@Valid @RequestBody AccountCreateInput input) {
		Account account = accountService.create(input);

		return new ResponseEntity<>(
			account,
			HttpStatus.CREATED
		);
	}
}
