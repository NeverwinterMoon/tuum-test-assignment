package com.tuum.testassignment.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransactionCreateResponse {
	private final Integer id;

	private final Integer accountID;

	private final BigDecimal amount;

	private final BigDecimal balanceAmount;

	private final String currencyName;

	private final String description;

	private final TransactionDirection direction;
}
