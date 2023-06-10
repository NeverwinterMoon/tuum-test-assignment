package com.tuum.testassignment.repository;

import java.util.List;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.tuum.testassignment.model.Account;
import com.tuum.testassignment.model.AccountCreateInput;
import com.tuum.testassignment.model.Balance;

@Mapper
@Repository
public interface AccountRepository {
	@Select("SELECT id, customer_id FROM account WHERE id = #{accountID}")
	@Results({
			@Result(property = "id", column = "id"),
			@Result(property = "customerID", column = "customer_id"),
			@Result(property = "createdAt", column = "created_id"),
			@Result(
					property = "balanceList",
					column = "id",
					javaType = List.class,
					many = @Many(select = "balanceListForAccountID")
			)
	})
	Account accountByAccountID(Integer accountID);

	// Used by @Many selector
	@Select("SELECT amount, currency_id FROM balance WHERE account_id = #{accountID}")
	@Results({
			@Result(
					property = "currencyName",
					column = "currency_id",
					javaType = String.class,
					one = @One(select = "currencyNameByID")
			)
	})
	List<Balance> balanceListForAccountID(Integer accountID);

	// Used by @Many selector
	@Select("SELECT name FROM currency WHERE id = #{currencyID}")
	String currencyNameByID(Integer currencyID);

	@Select({
		"INSERT INTO account(country_id, customer_id)",
		"VALUES((SELECT id FROM country WHERE name = #{countryName}), #{customerID})",
		"RETURNING id"
	})
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	Integer create(AccountCreateInput input);

	@Select("SELECT COUNT(*) > 0 FROM account WHERE id = #{accountID}")
	boolean exists(Integer accountID);
}
