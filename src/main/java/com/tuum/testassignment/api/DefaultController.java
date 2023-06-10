package com.tuum.testassignment.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

	@RequestMapping(value = "/")
	public String message() {
		return "Tuum Test Exercise";
	}
}
