package com.tuum.testassignment.integration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionListTest extends TransactionBaseTest {
	@Test
	void whenAccountIsMissing() {
		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/account/{accountID}/transaction",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<>() {
			},
			Map.of("accountID", 123)
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("accountID", "It looks like the account with the provided ID is missing."));
	}
}
