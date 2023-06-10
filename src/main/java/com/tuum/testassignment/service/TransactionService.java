package com.tuum.testassignment.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tuum.testassignment.error.CustomValidationException;
import com.tuum.testassignment.model.Transaction;
import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionCreateResponse;
import com.tuum.testassignment.repository.BalanceRepository;
import com.tuum.testassignment.repository.TransactionRepository;

@RequiredArgsConstructor
@Service
public class TransactionService {
	private final BalanceRepository balanceRepository;

	private final RabbitTemplate rabbitTemplate;

	private final TransactionRepository transactionRepository;

	@Transactional
	public TransactionCreateResponse create(TransactionCreateInput input) {
		try {
			switch (input.getDirection()) {
				case IN -> {
					balanceRepository.updateBalanceIN(input);
				}

				case OUT -> {
					balanceRepository.updateBalanceOUT(input);
				}
			}
		} catch (DataIntegrityViolationException exception) {
			throw new CustomValidationException(
				"balance",
				"Not enough funds to complete the transaction.",
				HttpStatus.PAYMENT_REQUIRED
			);
		}

		TransactionCreateResponse transaction = transactionRepository.create(input);

		// If this fails, transaction is rolled back
		// Default exchange, "transaction" queue
		rabbitTemplate.convertAndSend("transaction", input);

		return transaction;
	}

	public List<Transaction> list(Integer accountID) {
		return transactionRepository.listByAccountID(accountID);
	}
}
