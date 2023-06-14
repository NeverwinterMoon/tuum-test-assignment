package com.tuum.testassignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Account {
	Integer id;

	Integer customerID;

	List<Balance> balanceList;
}
