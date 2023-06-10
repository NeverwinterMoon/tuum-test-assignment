package com.tuum.testassignment.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CurrencyRepository {
	@Select({
		"<script>",
		"SELECT COUNT(id) FROM currency WHERE name IN",
		"<foreach item='name' collection='currencyNameList' open='(' separator=',' close=')'>",
		"#{name}",
		"</foreach>",
		"</script>"
	})
	Integer currencyCountByNameList(@Param("currencyNameList") List<String> currencyNameList);

	@Select("SELECT COUNT(*) > 0 FROM currency WHERE name = #{currencyName}")
	boolean isSupportedByName(String currencyName);
}
