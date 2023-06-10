package com.tuum.testassignment.validation;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.repository.BalanceRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@AllArgsConstructor
@Component
public class CurrencyIsSupportedByAccountValidator implements ConstraintValidator<CurrencyIsSupportedByAccount, TransactionCreateInput> {
	BalanceRepository repository;

	@Override public boolean isValid(TransactionCreateInput input, ConstraintValidatorContext context) {
		Integer accountID = input.getAccountID();
		if (accountID == null) {
			return true;
		}

		String currencyName = input.getCurrencyName();
		if (currencyName == null) {
			return true;
		}

		return repository.isCurrencySupportedByCurrencyName(currencyName);
	}
}
