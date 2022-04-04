package com.synectiks.security.email;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);
	private JavaMailSender javaMailSender;
	
	@Autowired
	public MailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public void sendEmail(SimpleMailMessage mail) throws MailException {
		javaMailSender.send(mail);
	}

	public void sendEmail(MimeMessage mail) throws MailException {
		javaMailSender.send(mail);
	}
	
	public JavaMailSender getJavaMailSender() {
		return this.javaMailSender;
	}
	
	public MimeMessage createHtmlMailMessage(String templateData, String to, String subject) {
		logger.info("Creating mime message to send html email");
		MimeMessage mimeMessage = getJavaMailSender().createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(templateData, true);
			
		} catch (Exception e) {
			logger.error("Exception in creating mime message ",e);
			return null;
		}
		return mimeMessage;
	}
	
	public MimeMessageHelper createHtmlMailMessageWithImage(MimeMessage mimeMessage, String templateData, String to, String subject) {
		logger.info("Creating mime message with image to send html email");
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(templateData, true);
			
		} catch (Exception e) {
			logger.error("Exception in creating mime message ",e);
			return null;
		}
		return helper;
	}
}
