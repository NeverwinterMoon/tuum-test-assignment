package com.tuum.testassignment.util;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestRepository {
	@Delete("DELETE FROM balance CASCADE; DELETE FROM account;")
	void deleteAllData();
}
