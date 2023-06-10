package com.tuum.testassignment.integration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountFetchTest {
	// Test tools
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	void whenAccountNotFound() {
		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/account?accountID={accountID}",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<>() {
			},
			Map.of("accountID", 123)
		);

		/* Then */
		// Response checks
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.NOT_FOUND);

		assertThat(response.getBody())
			.isEqualTo(Map.of(
				"accountID", "No such account exists."
			));
	}
}
