package com.synectiks.security.util;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomGenerator {

	public static String getRandomValue() {
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 10;
	    Random random = new Random();

	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();

	    return generatedString;
	}

	public static String getTemporaryPassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		String pwd = RandomStringUtils.random( 15, characters );
		return pwd;
	}

    public static String getRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String pwd = RandomStringUtils.random( length, characters );
        return pwd;
    }

    private static Serializable generateSessionId() {
        // Implement your session ID generation logic here
        // For simplicity, you can use UUID.randomUUID() or any other suitable method
        UUID uuid =  UUID.randomUUID();
        return uuid;
    }
	public static void main(String a[]) {
        generateSessionId();
	}
}
