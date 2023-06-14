package com.tuum.testassignment.e2e;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;
import com.tuum.testassignment.model.Balance;
import com.tuum.testassignment.model.Transaction;
import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionCreateResponse;
import com.tuum.testassignment.model.TransactionDirection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// To have non-static beforeAll / afterAll
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PositiveFlowAccountTransactionTest {

	// Test tools
	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TestRestTemplate testRestTemplate;

	// As it was not part of the test to create a customer, this data is populated on PostgreSQL Docker initialization
	private final Integer customerID = 19820130;

	// Shared and mutated between ordered tests
	private Integer accountID;
	private BigDecimal balanceAmountEUR = BigDecimal.ZERO;
	private BigDecimal balanceAmountUSD = BigDecimal.ZERO;

	@Test
	@Order(1)
	void whenCreatingAccount() {
		// TODO: Check when customer ID does not exist - mega fail now

		/* Given */
		// Same, pre-populated
		String countryName = "Estonia";
		List<String> currencyList = List.of("USD", "EUR");

		AccountCreateInput input = AccountCreateInput.builder()
			.customerID(customerID)
			.countryName(countryName)
			.currencyList(currencyList)
			.build();

		/* When */
		ResponseEntity<Account> response = testRestTemplate.postForEntity("/api/account", input, Account.class);

		/* Then */

		// Response checks
		assertThat(response.getStatusCode())
			.isEqualTo(HttpStatus.CREATED);

		Account account = response.getBody();
		assertThat(account).isNotNull();
		assertThat(account.getId()).isNotZero();
		assertThat(account.getCustomerID()).isEqualTo(customerID);
		assertThat(account.getBalanceList())
			.containsExactlyInAnyOrderElementsOf(
				currencyList.stream()
					.map(currencyName -> Balance.builder()
						.amount(BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY))
						.currencyName(currencyName)
						.build())
					.toList()
			);

		// Repository checks
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM account;", Integer.class))
			.isOne();
		Map<String, ?> accountFromRepository = jdbcTemplate.queryForMap("""
			SELECT country.name as country_name, customer_id
			FROM account
					JOIN country ON country_id = country.id
					JOIN customer ON account.customer_id = customer.id
				""");

		// Because type of countryName is CHAR(50), this returns trailing spaces to add up to 50 characters
		assertThat(((String) accountFromRepository.get("country_name")).trim()).isEqualTo(countryName);
		assertThat(accountFromRepository.get("customer_id")).isEqualTo(customerID);

		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM balance;", Integer.class))
			.isEqualTo(currencyList.size());

		List<Map<String, Object>> balanceList = jdbcTemplate.queryForList("""
			SELECT account_id, amount, currency.name as currency_name
			FROM balance
			         JOIN currency on balance.currency_id = currency.id;
			""");

		assertThat(balanceList).containsExactlyInAnyOrderElementsOf(List.of(
			Map.of(
				"account_id", account.getId(),
				"amount", BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY),
				"currency_name", "EUR"
			),
			Map.of(
				"account_id", account.getId(),
				"amount", BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY),
				"currency_name", "USD"
			)
		));

		// RabbitMQ checks
		assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo("account")).getMessageCount()).isEqualTo(1);

		List<AccountCreateInput> rabbitMQMessageList = Stream.generate(() ->
				(AccountCreateInput) rabbitTemplate.receiveAndConvert("account")
			)
			.takeWhile(Objects::nonNull)
			.collect(Collectors.toList());
		assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));

		// Sharing data between specs
		accountID = account.getId();
	}

	@Test
	@Order(2)
	void whenCreatingTransaction_IN_10_EUR() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN.setScale(2, RoundingMode.UNNECESSARY))
			.currencyName("EUR")
			.description("Test transaction 01")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<TransactionCreateResponse> response = testRestTemplate.postForEntity(
			"/api/transaction",
			input,
			TransactionCreateResponse.class
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		TransactionCreateResponse transactionCreateResponse = response.getBody();
		assertThat(transactionCreateResponse).isNotNull();
		assertThat(transactionCreateResponse.getAccountID()).isEqualTo(accountID);
		assertThat(transactionCreateResponse.getAmount()).isEqualTo(input.getAmount());
		assertThat(transactionCreateResponse.getCurrencyName()).isEqualTo(input.getCurrencyName());
		assertThat(transactionCreateResponse.getDirection()).isEqualTo(input.getDirection());
		assertThat(transactionCreateResponse.getDescription().trim()).isEqualTo(input.getDescription());
		assertThat(transactionCreateResponse.getBalanceAmount()).isEqualTo(input.getAmount());

		// Checking the message count first
		assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo("transaction")).getMessageCount()).isEqualTo(1);

		// Fetching all the messages and verifying
		List<TransactionCreateInput> rabbitMQMessageList = Stream.generate(() -> (TransactionCreateInput) rabbitTemplate.receiveAndConvert(
				"transaction"))
			.takeWhile(Objects::nonNull)
			.collect(Collectors.toList());

		assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));

		// Sharing data between specs
		balanceAmountEUR = balanceAmountEUR.add(input.getAmount());
	}

	@Test
	@Order(3)
	void whenCreatingTransaction_OUT_1_EUR() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.ONE.setScale(2, RoundingMode.UNNECESSARY))
			.currencyName("EUR")
			.description("Test transaction 02")
			.direction(TransactionDirection.OUT)
			.build();

		/* When */
		ResponseEntity<TransactionCreateResponse> response = testRestTemplate.postForEntity(
			"/api/transaction",
			input,
			TransactionCreateResponse.class
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		TransactionCreateResponse transactionCreateResponse = response.getBody();
		assertThat(transactionCreateResponse).isNotNull();
		assertThat(transactionCreateResponse.getAccountID()).isEqualTo(accountID);
		assertThat(transactionCreateResponse.getAmount()).isEqualTo(input.getAmount());
		assertThat(transactionCreateResponse.getCurrencyName()).isEqualTo(input.getCurrencyName());
		assertThat(transactionCreateResponse.getDirection()).isEqualTo(input.getDirection());
		assertThat(transactionCreateResponse.getDescription().trim()).isEqualTo(input.getDescription());
		assertThat(transactionCreateResponse.getBalanceAmount()).isEqualTo(balanceAmountEUR.subtract(input.getAmount()));

		// Checking the message count first
		assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo("transaction")).getMessageCount()).isEqualTo(1);

		// Fetching all the messages and verifying
		List<TransactionCreateInput> rabbitMQMessageList = Stream.generate(() -> (TransactionCreateInput) rabbitTemplate.receiveAndConvert(
				"transaction"))
			.takeWhile(Objects::nonNull)
			.collect(Collectors.toList());

		assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));

		// Sharing data between specs
		balanceAmountEUR = balanceAmountEUR.subtract(input.getAmount());
	}

	@Test
	@Order(4)
	void whenCreatingTransaction_IN_1_USD() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.ONE.setScale(2, RoundingMode.UNNECESSARY))
			.currencyName("USD")
			.description("Test transaction 03")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<TransactionCreateResponse> response = testRestTemplate.postForEntity(
			"/api/transaction",
			input,
			TransactionCreateResponse.class
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		TransactionCreateResponse transactionCreateResponse = response.getBody();
		assertThat(transactionCreateResponse).isNotNull();
		assertThat(transactionCreateResponse.getAccountID()).isEqualTo(accountID);
		assertThat(transactionCreateResponse.getAmount()).isEqualTo(input.getAmount());
		assertThat(transactionCreateResponse.getCurrencyName()).isEqualTo(input.getCurrencyName());
		assertThat(transactionCreateResponse.getDirection()).isEqualTo(input.getDirection());
		assertThat(transactionCreateResponse.getDescription().trim()).isEqualTo(input.getDescription());
		assertThat(transactionCreateResponse.getBalanceAmount()).isEqualTo(input.getAmount());

		// Checking the message count first
		assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo("transaction")).getMessageCount()).isEqualTo(1);

		// Fetching all the messages and verifying
		List<TransactionCreateInput> rabbitMQMessageList = Stream.generate(() -> (TransactionCreateInput) rabbitTemplate.receiveAndConvert(
				"transaction"))
			.takeWhile(Objects::nonNull)
			.collect(Collectors.toList());

		assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));

		// Sharing data between specs
		balanceAmountUSD = balanceAmountUSD.add(input.getAmount());
	}

	@Test
	@Order(5)
	void whenCreatingTransaction_IN_10_USD() {
		/* Given */
		TransactionCreateInput input = TransactionCreateInput.builder()
			.accountID(accountID)
			.amount(BigDecimal.TEN.setScale(2, RoundingMode.UNNECESSARY))
			.currencyName("USD")
			.description("Test transaction 04")
			.direction(TransactionDirection.IN)
			.build();

		/* When */
		ResponseEntity<TransactionCreateResponse> response = testRestTemplate.postForEntity(
			"/api/transaction",
			input,
			TransactionCreateResponse.class
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		TransactionCreateResponse transactionCreateResponse = response.getBody();
		assertThat(transactionCreateResponse).isNotNull();
		assertThat(transactionCreateResponse.getAccountID()).isEqualTo(accountID);
		assertThat(transactionCreateResponse.getAmount()).isEqualTo(input.getAmount());
		assertThat(transactionCreateResponse.getCurrencyName()).isEqualTo(input.getCurrencyName());
		assertThat(transactionCreateResponse.getDirection()).isEqualTo(input.getDirection());
		assertThat(transactionCreateResponse.getDescription().trim()).isEqualTo(input.getDescription());
		assertThat(transactionCreateResponse.getBalanceAmount()).isEqualTo(balanceAmountUSD.add(input.getAmount()));

		// Checking the message count first
		assertThat(Objects.requireNonNull(rabbitAdmin.getQueueInfo("transaction")).getMessageCount()).isEqualTo(1);

		// Fetching all the messages and verifying
		List<TransactionCreateInput> rabbitMQMessageList = Stream.generate(() -> (TransactionCreateInput) rabbitTemplate.receiveAndConvert(
				"transaction"))
			.takeWhile(Objects::nonNull)
			.collect(Collectors.toList());

		assertThat(rabbitMQMessageList).containsExactlyInAnyOrderElementsOf(List.of(input));

		// Sharing data between specs
		balanceAmountUSD = balanceAmountUSD.add(input.getAmount());
	}

	@Test
	@Order(6)
	void whenListingTransactionsForAccount() {
		/* When */
		ResponseEntity<List<Transaction>> response = testRestTemplate.exchange(
			"/api/account/{accountID}/transaction",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<>() {
			},
			Map.of("accountID", accountID)
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		List<Transaction> transactionList = response.getBody();
		assertThat(transactionList).isNotNull();
		assertThat(transactionList.size()).isEqualTo(4);

		assertThat(transactionList)
			.usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
			.containsExactlyInAnyOrderElementsOf(List.of(
				Transaction.builder()
					.accountID(accountID)
					.amount(BigDecimal.TEN.setScale(2, RoundingMode.UNNECESSARY))
					.currencyName("EUR")
					.description(padTo140("Test transaction 01"))
					.direction(TransactionDirection.IN)
					.build(),
				Transaction.builder()
					.accountID(accountID)
					.amount(BigDecimal.ONE.setScale(2, RoundingMode.UNNECESSARY))
					.currencyName("EUR")
					.description(padTo140("Test transaction 02"))
					.direction(TransactionDirection.OUT)
					.build(),
				Transaction.builder()
					.accountID(accountID)
					.amount(BigDecimal.ONE.setScale(2, RoundingMode.UNNECESSARY))
					.currencyName("USD")
					.description(padTo140("Test transaction 03"))
					.direction(TransactionDirection.IN)
					.build(),
				Transaction.builder()
					.accountID(accountID)
					.amount(BigDecimal.TEN.setScale(2, RoundingMode.UNNECESSARY))
					.currencyName("USD")
					.description(padTo140("Test transaction 04"))
					.direction(TransactionDirection.IN)
					.build()
			));
	}

	@Test
	@Order(7)
	void whenFetchingAccount() {
		/* When */
		ResponseEntity<Account> response = testRestTemplate.getForEntity(
			"/api/account/{accountID}",
			Account.class,
			Map.of("accountID", accountID)
		);

		/* Then */
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Account account = response.getBody();
		assertThat(account).isNotNull();
		assertThat(account)
			.usingRecursiveComparison()
			.ignoringCollectionOrder()
			.isEqualTo(
				Account.builder()
					.id(accountID)
					.customerID(customerID)
					.balanceList(List.of(
						Balance.builder()
							.amount(balanceAmountEUR)
							.currencyName("EUR")
							.build(),
						Balance.builder()
							.amount(balanceAmountUSD)
							.currencyName("USD")
							.build()
					))
					.build()
			);
	}

	String padTo140(String input) {
		return String.format("%-" + 140 + "s", input);
	}

	@AfterAll
	void afterAll() {
		rabbitAdmin.purgeQueue("account");
		rabbitAdmin.purgeQueue("transaction");

		jdbcTemplate.batchUpdate(
			"DELETE FROM transaction CASCADE;",
			"DELETE FROM balance CASCADE;",
			"DELETE FROM account;"
		);
	}
}
