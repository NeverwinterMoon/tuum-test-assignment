package com.tuum.testassignment.validation;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.tuum.testassignment.repository.CountryRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@AllArgsConstructor
@Component
public class CountryValidator implements ConstraintValidator<CountryIsValid, String> {
	CountryRepository countryRepository;

	@Override public boolean isValid(String countryName, ConstraintValidatorContext context) {
		if (countryName == null || countryName.trim().isEmpty()) {
			return true;
		}

		Integer countryID = countryRepository.countryIDByName(countryName);

		return countryID != null;
	}
}
