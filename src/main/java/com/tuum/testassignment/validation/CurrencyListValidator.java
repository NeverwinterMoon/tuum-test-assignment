package com.tuum.testassignment.validation;

import java.util.List;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.tuum.testassignment.repository.CurrencyRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@AllArgsConstructor
@Component
public class CurrencyListValidator implements ConstraintValidator<CurrencyListIsValid, List<String>> {
	CurrencyRepository currencyRepository;

	@Override public boolean isValid(List<String> currencyList, ConstraintValidatorContext context) {
		if (currencyList == null || currencyList.isEmpty()) {
			return true;
		}

		Integer count = currencyRepository.currencyCountByNameList(currencyList);

		return currencyList.size() == count;
	}
}
