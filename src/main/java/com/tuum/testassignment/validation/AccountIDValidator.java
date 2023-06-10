package com.tuum.testassignment.validation;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.tuum.testassignment.repository.AccountRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@AllArgsConstructor
@Component
public class AccountIDValidator implements ConstraintValidator<AccountIDIsValid, Integer> {
	AccountRepository repository;

	@Override public boolean isValid(Integer accountID, ConstraintValidatorContext context) {
		if (accountID == null) {
			// let @NonNull handle this
			return true;
		}

		return repository.exists(accountID);
	}
}
