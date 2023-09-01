package com.synectiks.security.controllers;

import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.email.AwsEmailService;
import com.synectiks.security.entities.Status;
import com.synectiks.security.entities.User;
import com.synectiks.security.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private AwsEmailService awsEmailService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/sendMail")
	public String sendMail() {

		try {
			SimpleMailMessage mail = new SimpleMailMessage();
			mail.setFrom("manoj.sharma@synectiks.com");
			mail.setTo("mohitksharmajpr@gmail.com");
			mail.setSubject("Test mail from security service");
			mail.setText("Hi...... This is Manoj ");

			mailService.sendEmail(mail);

		} catch (MailException mailException) {
			System.out.println(mailException);
		}
		return "Congratulations! Your mail has been send to the user.";
	}

    @RequestMapping(path = "/testAwsEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> sendMail(@RequestBody ObjectNode reqObj) {
        awsEmailService.sendMail(reqObj.get("accessKey").asText(),
            reqObj.get("secretKey").asText(),
            reqObj.get("senderEmail").asText(),
            reqObj.get("recipientEmail").asText(),
            reqObj.get("awsEmailServiceEndPoint").asText(),
            reqObj.get("region").asText());
        return ResponseEntity.status(HttpStatus.OK).body("Email sent");
    }

    @RequestMapping(path = "/forgot-password", method = RequestMethod.GET)
    public ResponseEntity<Object> sendForgotPasswordMail(@RequestParam String userName) {
        User user = this.userRepository.findByUsername(userName);
        if (user == null) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User Name :"+userName, null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if (StringUtils.isBlank(user.getEmail())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User's email not found. User Name :"+userName, null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        SendEmailResult status = awsEmailService.sendForgotPasswordMail(userName, user.getEmail());
        Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Mail sent", status);
        return ResponseEntity.status(HttpStatus.OK).body(st);
    }


    private Status setMessage(int stqtusCode, String stausType, String msg, Object obj){
        Status st = new Status();
        st.setCode(stqtusCode);
        st.setType(stausType);
        st.setMessage(msg);
        st.setObject(obj);
        return st;
    }
}
