package com.tuum.testassignment.integration;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tuum.testassignment.model.AccountCreateInput;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountCreateTest {
	// Test tools
	@Autowired
	private TestRestTemplate testRestTemplate;

	// As it was not part of the test to create a customer, this data is populated on PostgreSQL Docker initialization
	private final Integer customerID = 19820130;

	@Test
	void whenCurrencyUnsupported_JPY() {
		/* Given */
		List<String> currencyList = List.of("USD", "EUR", "JPY");

		AccountCreateInput input = AccountCreateInput.builder()
			.customerID(customerID)
			.countryName("Estonia")
			.currencyList(currencyList)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/account",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		// Response checks
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("currencyList", "One of the currency values is not supported."));
	}

	@Test
	void whenCountryUnsupported_Japan() {
		/* Given */
		List<String> currencyList = List.of("USD", "EUR");

		AccountCreateInput input = AccountCreateInput.builder()
			.customerID(customerID)
			.countryName("Japan")
			.currencyList(currencyList)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/account",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		// Response checks
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("countryName", "Country is not supported."));
	}
}
