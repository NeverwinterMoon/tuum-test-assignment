package com.tuum.testassignment.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.tuum.testassignment.model.TransactionCreateInput;

@Mapper
@Repository
public interface BalanceRepository {
	@Insert({
		"<script>",
		"INSERT INTO balance(account_id, amount, currency_id) VALUES",
		"<foreach collection='currencyList' item='currencyName' separator=','>",
		"(#{accountID}, 0.0, (SELECT id FROM currency WHERE name = #{currencyName}))",
		"</foreach>",
		"</script>"
	})
	void createSeveral(Integer accountID, @Param("currencyList") List<String> currencyList);

	@Select("SELECT COUNT(*) > 0 FROM balance INNER JOIN currency ON balance.currency_id = currency.id WHERE currency.name = #{currencyName}")
	boolean isCurrencySupportedByCurrencyName(String currencyName);

	// It's also possible to extract lock to a separate mapping which will be executed within the same @Transaction
	@Update({
		"WITH updated AS (",
		"    SELECT id, amount",
		"    FROM balance",
		"    WHERE account_id = #{accountID}",
		"        AND currency_id = (SELECT id FROM currency WHERE name = #{currencyName})",
		"            FOR UPDATE", // locking row
		")",
		"UPDATE balance",
		"SET amount = updated.amount + #{amount}",
		"FROM updated",
		"WHERE balance.id = updated.id"
	})
	void updateBalanceIN(TransactionCreateInput input);

	@Update({
		"WITH updated AS (",
		"    SELECT id, amount",
		"    FROM balance",
		"    WHERE account_id = #{accountID}",
		"        AND currency_id = (SELECT id FROM currency WHERE name = #{currencyName})",
		"            FOR UPDATE", // locking row
		")",
		"UPDATE balance",
		"SET amount = updated.amount - #{amount}",
		"FROM updated",
		"WHERE balance.id = updated.id"
	})
	void updateBalanceOUT(TransactionCreateInput input);
}
