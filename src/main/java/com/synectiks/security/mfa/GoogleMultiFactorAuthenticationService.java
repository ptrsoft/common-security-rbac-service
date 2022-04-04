package com.synectiks.security.mfa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

@Service
public class GoogleMultiFactorAuthenticationService {
	
		public String getGoogleAuthenticationKey(String userName) {
			GoogleAuthenticator gAuth = new GoogleAuthenticator();
			GoogleAuthenticatorKey googleAuthKey = gAuth.createCredentials();
			return googleAuthKey.getKey();
		}
		
		public String generateGoogleAuthenticationUri(String account, String issuer,  String secret) {
		  	try {
		  		URI uri = new URI("otpauth", "totp", "/" + issuer + ":" + account,
		                "secret=" + secret + "&issuer=" + issuer, null);
		  		return uri.toASCIIString();
		  	}catch(URISyntaxException e) {
		  		e.printStackTrace();
		  		return null;
		  	}
		}
		
		public void createQRImage(File qrFile, String qrCodeText, int size, String fileType)
				throws WriterException, IOException {
			// Create the ByteMatrix for the QR-Code that encodes the given String
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
			// Make the BufferedImage that are to hold the QRCode
			int matrixWidth = byteMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ImageIO.write(image, fileType, qrFile);
		}
		
}
