package com.tuum.testassignment.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
// Looks like JDK 16 records are not supported by MyBatis, at least out of the box
public class Transaction {
	private Integer id;

	private Integer accountID;

	private BigDecimal amount;

	private String currencyName;

	private String description;

	private TransactionDirection direction;
}
