package com.tuum.testassignment.service;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuum.testassignment.error.CustomValidationException;
import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;
import com.tuum.testassignment.repository.AccountRepository;
import com.tuum.testassignment.repository.BalanceRepository;

@RequiredArgsConstructor
@Service
public class AccountService {
	private final AccountRepository accountRepository;

	private final BalanceRepository balanceRepository;

	private final RabbitTemplate rabbitTemplate;

	public Account account(Integer accountID) {
		Account account = accountRepository.accountByAccountID(accountID);
		if (account == null) {
			throw new CustomValidationException("accountID", "No such account exists.", HttpStatus.NOT_FOUND);
		}

		return account;
	}

	@Transactional
	public Account create(AccountCreateInput input) {
		Integer accountID = accountRepository.create(input);
		balanceRepository.createSeveral(accountID, input.getCurrencyList());

		rabbitTemplate.convertAndSend("account", input);

		return accountRepository.accountByAccountID(accountID);
	}
}
