package com.tuum.testassignment.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.tuum.testassignment.model.Transaction;
import com.tuum.testassignment.model.TransactionCreateInput;
import com.tuum.testassignment.model.TransactionCreateResponse;

@Mapper
@Repository
public interface TransactionRepository {
	@Select({
		"WITH currency AS (SELECT id, name FROM currency WHERE name = #{currencyName})",
		"INSERT INTO transaction(account_id, amount, balance_id, currency_id, description, direction)",
		"SELECT #{accountID}, #{amount}, balance.id, currency.id, #{description}, #{direction}",
		"FROM balance",
		"		JOIN currency ON balance.currency_id = currency.id",
		"WHERE balance.account_id = #{accountID}",
		"RETURNING",
		"		id,",
		"		account_id,",
		"		amount,",
		"		(SELECT amount FROM balance WHERE balance.id = transaction.balance_id) AS balance_amount,",
		"		(SELECT name FROM currency) AS currency_name,",
		"		description,",
		"		direction"
	})
	@Results({
		@Result(property = "accountID", column = "account_id"),
		@Result(property = "balanceAmount", column = "balance_amount"),
		@Result(property = "currencyName", column = "currency_name")
	})
	TransactionCreateResponse create(TransactionCreateInput transaction);

	@Select({
		"SELECT transaction.id, account_id, amount, currency.name, description, direction",
		"FROM transaction",
		"    JOIN currency ON currency_id = currency.id",
		"WHERE account_id = #{accountID}",
		"ORDER BY created_at DESC"
	})
	@Results({
		@Result(property = "accountID", column = "account_id"),
		@Result(property = "createdAt", column = "created_at"),
		@Result(property = "currencyName", column = "name")
	})
	List<Transaction> listByAccountID(Integer accountID);
}
