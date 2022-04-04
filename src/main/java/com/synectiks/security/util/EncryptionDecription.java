package com.synectiks.security.util;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class EncryptionDecription {
	
	private static final Logger logger = LoggerFactory.getLogger(EncryptionDecription.class);
	private static final String SECRET_KEY = "BLACKANDWHITE";
	private static final String SALT = "applegingerlemon!!!!";
	private static final String SECRET_ALGO_KEY = "PBKDF2WithHmacSHA256";
	private static final String ALGO = "AES";
	private static final String CIPHER_CODE = "AES/CBC/PKCS5Padding";
	
	public static String encrypt(String strToEncrypt) {
		try {
	      byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	      IvParameterSpec ivspec = new IvParameterSpec(iv);
	 
	      SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_ALGO_KEY);
	      KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
	      SecretKey tmp = factory.generateSecret(spec);
	      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO);
	 
	      Cipher cipher = Cipher.getInstance(CIPHER_CODE);
	      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
	      return Base64.getEncoder()
	          .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			logger.error("Error while encrypting: ",e);
		}
		return null;
	}
  
	public static String decrypt(String strToDecrypt) {
		try {
	      byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	      IvParameterSpec ivspec = new IvParameterSpec(iv);
	 
	      SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_ALGO_KEY);
	      KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
	      SecretKey tmp = factory.generateSecret(spec);
	      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), ALGO);
	 
	      Cipher cipher = Cipher.getInstance(CIPHER_CODE);
	      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
	      return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
	    } catch (Exception e) {
	      logger.error("Error while decrypting: ",e);
	    }
	    return null;
	}
  
	public static void main(String a[]) {
		String enc = encrypt("manojkr.joshi@gmail.com");
		System.out.println(enc);
		String dec = decrypt(enc);
		System.out.println(dec);
	}
}
