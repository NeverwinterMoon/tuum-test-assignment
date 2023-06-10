package com.tuum.testassignment.integration;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionDirection;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCreateTest extends TransactionBaseTest {
	@Test
	void whenCurrencyUnsupported_JPY() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN)
			.currencyName("JPY")
			.description("NeverwinterMoon")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("currencyName", "Unsupported currency name."));
	}

	@Test
	void whenTransactionDirectionUnsupported_UP() throws JsonProcessingException {
		/* Given */
		Map<String, Object> inputMap = Map.of(
			"accountID", accountID,
			"amount", BigDecimal.TEN,
			"currencyName", "EUR",
			"description", "NeverwinterMoon",
			"direction", "UP"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String input = objectMapper.writeValueAsString(inputMap);

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input, headers),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("direction", "Unsupported transaction direction. Supported values are [IN, OUT]."));
	}

	@Test
	void whenUnsupportedAmount_negative() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN.negate())
			.currencyName("EUR")
			.description("NeverwinterMoon")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("amount",
				"Amount should not be zero or negative. Zero amount is moot. Negative amount is indicated by the transaction direction instead."));
	}

	@Test
	void whenInsufficientFunds() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN)
			.currencyName("EUR")
			.description("NeverwinterMoon")
			.direction(TransactionDirection.OUT)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.PAYMENT_REQUIRED);

		assertThat(response.getBody())
			.isEqualTo(Map.of("balance", "Not enough funds to complete the transaction."));
	}

	@Test
	void whenAccountIsMissing() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(123)
			.amount(BigDecimal.TEN)
			.currencyName("EUR")
			.description("NeverwinterMoon")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("accountID", "It looks like the account with the provided ID is missing."));
	}

	@Test
	void whenDescriptionIsEmpty() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN)
			.currencyName("EUR")
			.description("")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("description", "Description is mandatory and should be no longer than 140 characters."));
	}

	@Test
	void whenDescriptionIsMissing() throws JsonProcessingException {
		/* Given */
		Map<String, Object> inputMap = Map.of(
			"accountID", accountID,
			"amount", BigDecimal.TEN,
			"currencyName", "EUR",
			"direction", "IN"
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String input = objectMapper.writeValueAsString(inputMap);

		/* When */
		ResponseEntity<Map<String, ?>> response = testRestTemplate.exchange(
			"/api/transaction",
			HttpMethod.POST,
			new HttpEntity<>(input, headers),
			new ParameterizedTypeReference<>() {
			}
		);

		/* Then */
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThat(response.getBody())
			.isEqualTo(Map.of("description", "must not be null"));
	}
}
