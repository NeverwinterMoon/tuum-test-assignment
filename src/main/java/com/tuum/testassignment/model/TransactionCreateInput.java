package com.tuum.testassignment.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;

import com.tuum.testassignment.validation.AccountIDIsValid;
import com.tuum.testassignment.validation.CurrencyIsSupportedByAccount;
import com.tuum.testassignment.validation.CurrencyIsValid;
import com.tuum.testassignment.validation.ValidationGroup1;
import com.tuum.testassignment.validation.ValidationGroup2;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Validations that involve checks against PostgreSQL here should be safe to perform outside the transaction, as the data checked here is not subject to change (account, supported currencies, currencies of account balances - these are created once in scope of this application initialization and never added/removed/modified)
 */
@Builder
@Data
@AllArgsConstructor
// Required for Jackson deserialization, otherwise fails, as Jackson looks for default (no arguments) constructor
@NoArgsConstructor
@CurrencyIsSupportedByAccount(message = "None of the balances on this account support the provided currency.", groups = ValidationGroup2.class)
public class TransactionCreateInput {
	@AccountIDIsValid(message = "It looks like the account with the provided ID is missing.", groups = ValidationGroup1.class)
	@NotNull(groups = ValidationGroup1.class)
	Integer accountID;

	@Positive(message = "Amount should not be zero or negative. Zero amount is moot. Negative amount is indicated by the transaction direction instead.", groups = ValidationGroup1.class)
	@NotNull(groups = ValidationGroup1.class)
	BigDecimal amount;

	@CurrencyIsValid(message = "Unsupported currency name.", groups = ValidationGroup1.class)
	@NotNull(groups = ValidationGroup1.class)
	String currencyName;

	@Length(min = 1, max = 140, message = "Description is mandatory and should be no longer than 140 characters.", groups = ValidationGroup1.class)
	@NotNull(groups = ValidationGroup1.class)
	String description;

	@NotNull(groups = ValidationGroup1.class)
	TransactionDirection direction;
}

