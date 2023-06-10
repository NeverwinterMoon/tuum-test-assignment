package com.tuum.testassignment.validation;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.tuum.testassignment.repository.CurrencyRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@AllArgsConstructor
@Component
public class CurrencyValidator implements ConstraintValidator<CurrencyIsValid, String> {
	CurrencyRepository repository;

	@Override public boolean isValid(String currencyName, ConstraintValidatorContext context) {
		if (currencyName == null) {
			// @NonNull handles that
			return true;
		}

		return repository.isSupportedByName(currencyName);
	}
}
