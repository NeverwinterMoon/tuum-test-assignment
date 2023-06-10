package com.tuum.testassignment.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
	Integer id;

	Integer customerID;

	List<Balance> balanceList;
}
