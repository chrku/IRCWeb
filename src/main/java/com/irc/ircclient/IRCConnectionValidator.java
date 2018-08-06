package com.irc.ircclient;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/*
 * Validates IRC connection requests according to
 * RFC 1459
 */
public class IRCConnectionValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object target, Errors errors) {
		// TODO Auto-generated method stub

	}

}
