package com.synectiks.security.util;

import java.util.Random;

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
	    
	    System.out.println(generatedString);
	    return generatedString;
	}
	
	public static String getTemporaryPassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		String pwd = RandomStringUtils.random( 15, characters );
		System.out.println( pwd );
		return pwd;
	}
	
	public static void main(String a[]) {
		getTemporaryPassword();
	}
}
