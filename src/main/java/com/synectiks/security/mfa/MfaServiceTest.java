package com.synectiks.security.mfa;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.zxing.WriterException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class MfaServiceTest {
	
		public static String getGoogleAuthenticationKey(String userName) {
			GoogleAuthenticator gAuth = new GoogleAuthenticator();
			GoogleAuthenticatorKey googleAuthKey = gAuth.createCredentials();
			return googleAuthKey.getKey();
		}
		
		private static String generateGoogleAuthenticationUri(String account, String issuer,  String secret) {
		  	try {
		  		URI uri = new URI("otpauth", "totp", "/" + issuer + ":" + account,
		                "secret=" + secret + "&issuer=" + issuer, null);
		  		return uri.toASCIIString();
		  	}catch(URISyntaxException e) {
		  		e.printStackTrace();
		  		return null;
		  	}
		}
		
		public static void main(String[] args) {
			String userName = "abc@abc.com";
//			getGoogleAuthenticationKey(userName);
//			GoogleAuthenticator gAuth = new GoogleAuthenticator();
//			GoogleAuthenticatorKey googleAuthKey = gAuth.createCredentials();
			System.out.println("GoogleAuthenticatorKey: "+getGoogleAuthenticationKey(userName));
			String keyUri = generateGoogleAuthenticationUri(userName, "Synectiks", getGoogleAuthenticationKey(userName));
			
//			try {
////				keyUri= generateKeyUri("totp@example.com", "Vaadin TOTP",
////				        key.getKey());
//				keyUri= generateKeyUri(userName, "Vaadin TOTP", getMfaKey(userName));
//			} catch (URISyntaxException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			 
			 System.out.println("key url : "+keyUri);
//			 String filePath = "D.png";
//				int size = 125;
//				String fileType = "png";
//				File qrFile = new File(filePath);
//			 try {
//				GenerateQRCode.createQRImage(qrFile, keyUri, size, fileType);
//			} catch (WriterException | IOException e) {
//				e.printStackTrace();
//			}
		}

		
		
}
