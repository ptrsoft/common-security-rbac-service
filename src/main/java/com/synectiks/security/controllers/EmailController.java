package com.synectiks.security.controllers;

import com.synectiks.security.config.Constants;
import com.synectiks.security.entities.EmailQueue;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.service.AppkubeAwsEmailService;
import com.synectiks.security.service.EmailQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_EMAIL)
@CrossOrigin
public class EmailController {

	private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private AppkubeAwsEmailService appkubeAwsEmailService;

    @Autowired
    private EmailQueueService emailQueueService;

    /**
     * Below spring boot cron job run every 1 minute.
     * @return
     */
    @Scheduled(cron = "0 */1 * ? * *")
    @RequestMapping("/send-new-user-registration-mail")
    public String sendNewUserRegistrationMail() {
        List<EmailQueue> pendingMails = emailQueueService.findByStatusAndMailType(Constants.STATUS_PENDING, Constants.TYPE_NEW_USER);
        for(EmailQueue emailQueue: pendingMails){
            updateMailStatus(emailQueue, Constants.STATUS_IN_PROCESS);
            appkubeAwsEmailService.sendNewUserRegistrationMail(emailQueue);
            updateMailStatus(emailQueue, Constants.STATUS_SENT);
        }
        return "Congratulations! Your mail has been sent to the user";
    }

    /**
     * Below spring boot cron job run every 1 minute.
     * @return
     */
    @Scheduled(cron = "0 */1 * ? * *")
    @RequestMapping("/send-new-org-user-registration-mail")
    public String sendNewOrgUserRegistrationMail() {
        List<EmailQueue> pendingMails = emailQueueService.findByStatusAndMailType(Constants.STATUS_PENDING, Constants.USER_REQUEST_TYPE_ONLINE);
        for(EmailQueue emailQueue: pendingMails){
            updateMailStatus(emailQueue, Constants.STATUS_IN_PROCESS);
            appkubeAwsEmailService.sendNewOrgUserRegistrationMail(emailQueue);
            updateMailStatus(emailQueue, Constants.STATUS_SENT);
        }
        return "Congratulations! Your new org user registration mail has been sent to the user";
    }

    private void updateMailStatus(EmailQueue emailQueue, String statusInProcess) {
        emailQueue.setStatus(statusInProcess);
        emailQueue.setUpdatedAt(new Date());
        emailQueue.setUpdatedBy(Constants.SYSTEM_ACCOUNT);
        emailQueueService.save(emailQueue);
    }

//    @RequestMapping("/sendTestMail")
//	public String sendTestMail() {
//
//		try {
//			SimpleMailMessage mail = new SimpleMailMessage();
//			mail.setFrom("manoj.sharma@synectiks.com");
//			mail.setTo("mohitksharmajpr@gmail.com");
//			mail.setSubject("Test mail from security service");
//			mail.setText("Hi...... This is Manoj ");
//
//			mailService.sendEmail(mail);
//
//		} catch (MailException mailException) {
//			System.out.println(mailException);
//		}
//		return "Congratulations! Your mail has been send to the user.";
//	}
//
//    @RequestMapping(path = "/testAwsEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Object> sendMail(@RequestBody ObjectNode reqObj) {
//        awsEmailService.sendMail(reqObj.get("accessKey").asText(),
//            reqObj.get("secretKey").asText(),
//            reqObj.get("senderEmail").asText(),
//            reqObj.get("recipientEmail").asText(),
//            reqObj.get("awsEmailServiceEndPoint").asText(),
//            reqObj.get("region").asText());
//        return ResponseEntity.status(HttpStatus.OK).body("Email sent");
//    }

}
