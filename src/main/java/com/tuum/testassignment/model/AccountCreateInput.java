package com.tuum.testassignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tuum.testassignment.validation.CountryIsValid;
import com.tuum.testassignment.validation.CurrencyListIsValid;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

//@Data // Might be overkill as it generates the equivalent of @Getter, @Setter, @EqualsAndHashCode, and @ToString
@Getter
// Needed just for spec, for example: assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCreateInput {
	Integer customerID;

	@NotBlank(message = "Country cannot be blank.")
	@CountryIsValid(message = "Country is not supported.")
	String countryName;

	@NotEmpty(message = "Currency list is mandatory.")
	@CurrencyListIsValid(message = "One of the currency values is not supported.")
	List<String> currencyList;
}
