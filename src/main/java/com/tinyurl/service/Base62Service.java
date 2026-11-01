package com.tinyurl.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class Base62Service {

    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

	public String randomCode() {
		StringBuilder code = new StringBuilder(CODE_LENGTH);
		for (int i = 0; i < CODE_LENGTH; i++) {
			code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		return code.toString();
	}
}
