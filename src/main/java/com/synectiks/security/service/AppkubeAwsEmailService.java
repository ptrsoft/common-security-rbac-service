package com.synectiks.security.service;


import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.synectiks.security.config.Constants;
import com.synectiks.security.entities.Config;
import com.synectiks.security.entities.EmailQueue;
import com.synectiks.security.entities.User;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.util.EncryptionDecription;
import com.synectiks.security.util.RandomGenerator;
import com.synectiks.security.util.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppkubeAwsEmailService {
    private static final Logger logger = LoggerFactory.getLogger(AppkubeAwsEmailService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SesClient sesClient;

    @Autowired
    private AmazonSimpleEmailService amazonSimpleEmailService;

    public SendEmailResponse sendNewUserRegistrationMail(EmailQueue emailQueue) {
        logger.debug("Sending new user registration mail to user : {}", emailQueue.getUserName());
        User user = userRepository.findByUsername(emailQueue.getUserName());
        if(user == null){
            logger.error("User not found. User name: {}", emailQueue.getUserName());
            return null;
        }
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(emailQueue.getMailTo());
        String subject = Constants.MAIL_SUBJECT_NEW_APPKUBE_ACCOUNT_CREATED;
        String msg = Constants.MAIL_BODY_NEW_APPKUBE_ACCOUNT_CREATED
            .replaceAll("##USERNAME##",emailQueue.getUserName())
            .replaceAll("##PASSWORD##",EncryptionDecription.decrypt(user.getEncPassword()))
            .replaceAll("##OWNERNAME##","AppKube");
        Content subjectContent = Content.builder().data(subject).build();
        Content bodyContent = Content.builder().data(msg).build();
        Body body = Body.builder().html(bodyContent).build();
        Message message = Message.builder()
            .subject(subjectContent)
            .body(body)
            .build();
        Destination destination = Destination.builder()
            .toAddresses(toAddresses)
            .build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
            .source(emailQueue.getMailFrom())
            .destination(destination)
            .message(message)
            .build();
        SendEmailResponse sendEmailResponse = sesClient.sendEmail(sendEmailRequest);
        logger.debug("New user registration mail sent. Aws mail response message id: {}", sendEmailResponse.messageId());
        return sendEmailResponse;
    }

    public SendEmailResponse sendNewOrgUserRegistrationMail(EmailQueue emailQueue) {
        logger.debug("Sending new org user registration mail to user : {}", emailQueue.getUserName());
        User user = userRepository.findByUsername(emailQueue.getUserName());
        if(user == null){
            logger.error("User not found. User name: {}", emailQueue.getUserName());
            return null;
        }
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(emailQueue.getMailTo());
        String subject = Constants.MAIL_SUBJECT_NEW_ORG_USER_REQUEST;
        String msg = Constants.MAIL_BODY_NEW_ORG_USER_REQUEST
            .replaceAll("##USERNAME##",emailQueue.getUserName())
            .replaceAll("##OWNERNAME##","AppKube");
        Content subjectContent = Content.builder().data(subject).build();
        Content bodyContent = Content.builder().data(msg).build();
        Body body = Body.builder().html(bodyContent).build();
        Message message = Message.builder()
            .subject(subjectContent)
            .body(body)
            .build();
        Destination destination = Destination.builder()
            .toAddresses(toAddresses)
            .build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
            .source(emailQueue.getMailFrom())
            .destination(destination)
            .message(message)
            .build();
        SendEmailResponse sendEmailResponse = sesClient.sendEmail(sendEmailRequest);
        logger.debug("New org user registration mail sent. Aws mail response message id: {}", sendEmailResponse.messageId());
        return sendEmailResponse;
    }

    public SendEmailResponse sendForgotPasswordMail(User user, Config configEmailFrom) {
        String token = RandomGenerator.getRandomString(6);
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(user.getEmail());
        String subject = "OTP to reset password";
        String msg = "<h3>OTP to reset password will expire in 10 minutes.</h3><br>"+token;
        Content subjectContent = Content.builder().data(subject).build();
        Content bodyContent = Content.builder().data(msg).build();
        Body body = Body.builder().html(bodyContent).build();
        Message message = Message.builder()
            .subject(subjectContent)
            .body(body)
            .build();
        Destination destination = Destination.builder()
            .toAddresses(toAddresses)
            .build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
            .source(configEmailFrom.getValue())
            .destination(destination)
            .message(message)
            .build();
        Token.put(user.getUsername(), token);
        SendEmailResponse sendEmailResponse = sesClient.sendEmail(sendEmailRequest);
        logger.debug("OTP sent in forgot-password mail. Aws mail response message id: {}", sendEmailResponse.messageId());
        return sendEmailResponse;
    }
//    public void sendMail(String accessKey, String secretKey, String senderEmail, String recipientEmail, String awsEmailServiceEndPoint, String region) {
//
//        // Set up the AWS credentials
//        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
//
//        // Set up the AWS SES client
//        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
//            .withCredentials(new AWSStaticCredentialsProvider(credentials))
//            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEmailServiceEndPoint, region))
//            .build();
//
//        // Create a request to send an email
//        SendEmailRequest request = new SendEmailRequest()
//            .withSource(senderEmail)
//            .withDestination(
//                new Destination().withToAddresses(recipientEmail))
//            .withMessage(new Message()
//                .withSubject(new Content().withCharset("UTF-8").withData("Test Email"))
//                .withBody(new Body()
//                    .withHtml(new Content().withCharset("UTF-8").withData("<h1>This is a test email.</h1>"))));
//
//        // Send the email
//        SendEmailResult result = client.sendEmail(request);
//        System.out.println("Email sent. Message ID: " + result.getMessageId());
//    }



//    public SendEmailResult sendNewUserMail(User user) {
////        AmazonSimpleEmailService client = getAwsEmailClient();
//        String ownerName = user.getOwner() != null ? user.getOwner().getUsername() : "AppKube";
////        String subject = "New AppKube Account Created";
//        String subject = Constants.MAIL_SUBJECT_NEW_APPKUBE_ACCOUNT_CREATED;
//        String msg = Constants.MAIL_BODY_NEW_APPKUBE_ACCOUNT_CREATED
//            .replaceAll("##USERNAME##",user.getUsername())
//            .replaceAll("##PASSWORD##",EncryptionDecription.decrypt(user.getEncPassword()))
//            .replaceAll("##OWNERNAME##",ownerName);
//        // Create a request to send an email
//        SendEmailRequest request = new SendEmailRequest()
//            .withSource(awsSenderMail)
//            .withDestination(
//                new Destination().withToAddresses(user.getEmail()))
//            .withMessage(new Message()
//                .withSubject(new Content().withCharset("UTF-8").withData(subject))
//                .withBody(new Body()
//                    .withHtml(new Content().withCharset("UTF-8").withData(msg))));
//
//        SendEmailResult result = client.sendEmail(request);
//        logger.info("New user email sent");
//        return result;
//    }


//    public void sendScheduledMailWithAwsSimpleEmailService() {
//        AmazonSimpleEmailService client = getAwsEmailClient();
//        List<EmailQueue> pendingMails = emailQueueRepository.findByStatus(Constants.STATUS_PENDING);
//        SendEmailRequest request = null;
//        for (EmailQueue emailQueue: pendingMails){
//             request = new SendEmailRequest()
//                .withSource(emailQueue.getMailFrom())
//                .withDestination(
//                    new Destination().withToAddresses(emailQueue.getMailTo()))
//                .withMessage(new Message()
//                    .withSubject(new Content().withCharset("UTF-8").withData(emailQueue.getMailSubject()))
//                    .withBody(new Body()
//                        .withHtml(new Content().withCharset("UTF-8").withData(emailQueue.getMailBody()))));
//
//            SendEmailResult result = client.sendEmail(request);
//            logger.info("Send email result : {}",result.getSdkResponseMetadata().toString());
//        }
//    }

//    public void getAwsMailStatus() {
//        SesClient sesClient = getAwsSesClient();
//        // Get email sending statistics
//        GetSendStatisticsResponse sendStatistics = sesClient.getSendStatistics();
//        System.out.println("Email Sending Statistics: " + sendStatistics);
//
//        // List the last few sent emails
//        ListIdentitiesResponse listIdentitiesResponse = sesClient.listIdentities();
////        listIdentitiesResponse.identities().forEach(identity -> {
////            GetSendStatisticsResponse emailStats = sesClient.getSendStatistics(builder -> builder.identity(identity));
////            System.out.println("Email: " + identity + ", Statistics: " + emailStats);
////        });
//
//        // Close the SES client
//        sesClient.close();
//    }


//    private void sendMailWithAwsSesClient(){
////        List<EmailQueue> pendingMails = emailQueueRepository.findByStatus(Constants.STATUS_PENDING);
////        SesClient sesClient = getAwsSesClient();
//        List<String> toAddresses = new ArrayList<>();
////        for (EmailQueue emailQueue: pendingMails){
//            Content subjectContent = Content.builder().data("emailQueue.getMailSubject()").build();
//            Content bodyContent = Content.builder().data("emailQueue.getMailBody()").build();
//            Body body = Body.builder().text(bodyContent).build();
//            Message message = Message.builder()
//                .subject(subjectContent)
//                .body(body)
//                .build();
//
//            toAddresses.clear();
//            toAddresses.add("manoj.sharma@synectiks.com");
//
//            Destination destination = Destination.builder()
//                    .toAddresses(toAddresses)
//                    .build();
//            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
//                .source("manoj.sharma@synectiks.com")
//                .destination(destination)
//                .message(message)
//                .build();
//
//            SendEmailResponse sendEmailResponse = sesClient.sendEmail(sendEmailRequest);
//            System.out.println("Email sent. Message ID: " + sendEmailResponse);
////            SdkHttpResponse resp = sendEmailResponse.sdkHttpResponse();
////            System.out.println("Email http response: " + resp);
////            System.out.println("Email http response is successfule: " + resp.isSuccessful());
////            System.out.println("Email http response is status code: " + resp.statusCode());
////            System.out.println("Email http response is status text: " + resp.statusText());
//            //            GetSendStatisticsResponse sendStatistics = sesClient.getSendStatistics();
//            ListIdentitiesResponse listIdentitiesResponse = sesClient.listIdentities();
//            System.out.println(listIdentitiesResponse.identities());
//            if(listIdentitiesResponse.identities().contains("manoj.sharma@synectiks.com")){
//                GetSendStatisticsRequest req = GetSendStatisticsRequest.builder().build();
//
////                System.out.println(req.equalsBySdkFields("manoj.sharma@synectiks.com"));
//                GetSendStatisticsResponse sendStatistics = sesClient.getSendStatistics(req);
//
////                GetSendStatisticsResponse sendStatistics = sesClient.getSendStatistics(builder -> builder.sdkFields().);
//                System.out.println("Send Statistics for " + sendStatistics);
//                List<SendDataPoint> sendDataPointList = sendStatistics.sendDataPoints();
//
//                // Now sendDataPoints is sorted based on the timestamp
//                List<SendDataPoint> list = new ArrayList<>();
//                // Print the sortelist = {ArrayList@4825}  size = 22d list
//                for (SendDataPoint dataPoint : sendDataPointList) {
//                    list.add(dataPoint);
//                    System.out.println("Timestamp: " + dataPoint.timestamp() + ", Other properties: " + dataPoint.toString());
//                }
//                Collections.sort(list, Comparator.comparing(SendDataPoint::timestamp));
//
//                System.out.println("STATUS ::--->>>> "+list.get(list.size()-1));
//            }
////            for(String identity: listIdentitiesResponse.identities()){
////                GetSendStatisticsRequest req = GetSendStatisticsRequest.builder().build();
////                sesClient.getSendStatistics(req);
////            }
////            listIdentitiesResponse.identities().forEach(identity -> {
////                GetSendStatisticsResponse emailStats = sesClient.getSendStatistics(builder -> builder.equalsBySdkFields()identity(identity));
////                System.out.println("Email: " + identity + ", Statistics: " + emailStats);
////            });
//
////        }
//        // Close the SES client
//
//
//
//    }
//    public static void main(String a[]){
//        new AwsEmailService().sendMailWithAwsSesClient();
//    }
}

