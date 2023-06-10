package com.tuum.testassignment.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CountryRepository {
	@Select("SELECT id FROM country WHERE name = #{countryName}")
	Integer countryIDByName(String countryName);
}
