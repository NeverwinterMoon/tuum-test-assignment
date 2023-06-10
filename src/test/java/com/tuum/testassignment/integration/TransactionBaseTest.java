package com.tuum.testassignment.integration;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TransactionBaseTest {
	// Test tools
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public ObjectMapper objectMapper;

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Autowired
	public TestRestTemplate testRestTemplate;

	public Integer accountID;

	@BeforeEach
	void beforeEach() {
		// As it was not part of the test to create a customer, this data is populated on PostgreSQL Docker initialization
		Integer customerID = 19820130;
		String countryName = "Estonia";
		List<String> currencyList = List.of("USD", "EUR");

		AccountCreateInput input = AccountCreateInput.builder()
			.customerID(customerID)
			.countryName(countryName)
			.currencyList(currencyList)
			.build();

		ResponseEntity<Account> response = testRestTemplate.postForEntity("/api/account", input, Account.class);

		accountID = Objects.requireNonNull(response.getBody()).getId();
	}

	@AfterEach
	void afterEach() {
		rabbitAdmin.purgeQueue("account");

		jdbcTemplate.batchUpdate(
			"DELETE FROM transaction CASCADE;",
			"DELETE FROM balance CASCADE;",
			"DELETE FROM account;"
		);
	}
}
