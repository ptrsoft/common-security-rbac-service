package com.synectiks.security.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.security.email.MailService;
import com.synectiks.security.interfaces.IApiController;

@RestController
@RequestMapping(path = IApiController.SEC_API
		+ IApiController.URL_USER, method = RequestMethod.POST)
@CrossOrigin
public class EmailController {

	private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

	@Autowired
	private MailService mailService;

	@RequestMapping("/sendMail")
	public String sendMail() {

		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setFrom("manoj.sharma@synectiks.com");
			mail.setTo("manojkr.joshi@gmail.com");
			mail.setSubject("Test mail from security service");
			mail.setText("Hi...... This is Manoj ");

			mailService.sendEmail(mail);
			
		} catch (MailException mailException) {
			System.out.println(mailException);
		}
		return "Congratulations! Your mail has been send to the user.";
	}

}
