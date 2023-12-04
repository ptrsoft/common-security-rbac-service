package com.synectiks.security.email;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.synectiks.security.entities.User;
import com.synectiks.security.util.EncryptionDecription;
import com.synectiks.security.util.RandomGenerator;
import com.synectiks.security.util.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AwsEmailService {

    @Value("${synectiks.aws.mail.key}")
    private String awsMailKey;

    @Value("${synectiks.aws.mail.end-point}")
    private String awsMailEndPoint;

    @Value("${synectiks.aws.mail.region}")
    private String awsMailRegion;

    @Value("${synectiks.aws.mail.sender}")
    private String awsSenderMail;
    public void sendMail(String accessKey, String secretKey, String senderEmail, String recipientEmail, String awsEmailServiceEndPoint, String region) {

        // Set up the AWS credentials
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // Set up the AWS SES client
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEmailServiceEndPoint, region))
            .build();

        // Create a request to send an email
        SendEmailRequest request = new SendEmailRequest()
            .withSource(senderEmail)
            .withDestination(
                new Destination().withToAddresses(recipientEmail))
            .withMessage(new Message()
                .withSubject(new Content().withCharset("UTF-8").withData("Test Email"))
                .withBody(new Body()
                    .withHtml(new Content().withCharset("UTF-8").withData("<h1>This is a test email.</h1>"))));

        // Send the email
        SendEmailResult result = client.sendEmail(request);
        System.out.println("Email sent. Message ID: " + result.getMessageId());
    }

    public SendEmailResult sendForgotPasswordMail(String userName, String recipientEmail) {
        AmazonSimpleEmailService client = getAwsEmailClient();
        String token = RandomGenerator.getRandomString(6);

        String msg = "<h1>OTP to reset password will expire in 10 minutes.</h1><br>"+token;
        // Create a request to send an email
        SendEmailRequest request = new SendEmailRequest()
            .withSource(awsSenderMail)
            .withDestination(
                new Destination().withToAddresses(recipientEmail))
            .withMessage(new Message()
                .withSubject(new Content().withCharset("UTF-8").withData("OTP to reset password"))
                .withBody(new Body()
                    .withHtml(new Content().withCharset("UTF-8").withData(msg))));

        // Send the email
        Token.put(userName, token);
        SendEmailResult result = client.sendEmail(request);
        System.out.println("OTP to rest password sent in mail");
        return result;
    }

    public SendEmailResult sendNewUserMail(User user) {
        AmazonSimpleEmailService client = getAwsEmailClient();
        String ownerName = user.getOwner() != null ? user.getOwner().getUsername() : "AppKube";
        String subject = "Your AppKube Account Information";
        String msg = "Dear "+user.getUsername()+",\n" +
            "Welcome to AppKube. We're delighted to have you on board.\n" +
            "As part of the onboarding process, we are providing you with your login credentials to access AppKube services. Please keep this information secure and do not share it with anyone.\n" +
            "\n" +
            "Your login details are as follows:\n" +
            "Login ID: "+user.getUsername()+"\n" +
            "Password: "+EncryptionDecription.decrypt(user.getEncPassword())+"\n" +
            "\n" +
            "You can use the below link to login\n" +
            "\n" +
            "https://appkube.synectiks.net\n" +
            "\n" +
            "Best regards,\n" +ownerName;
        // Create a request to send an email
        SendEmailRequest request = new SendEmailRequest()
            .withSource(awsSenderMail)
            .withDestination(
                new Destination().withToAddresses(user.getEmail()))
            .withMessage(new Message()
                .withSubject(new Content().withCharset("UTF-8").withData(subject))
                .withBody(new Body()
                    .withHtml(new Content().withCharset("UTF-8").withData(msg))));

        SendEmailResult result = client.sendEmail(request);
        System.out.println("New user email sent");
        return result;
    }

    public AmazonSimpleEmailService getAwsEmailClient(){
        String decAwsAccSecKey = EncryptionDecription.decrypt(awsMailKey);
        String emailKey[] = decAwsAccSecKey.split(",");
        String awsEmailServiceEndPoint = EncryptionDecription.decrypt(awsMailEndPoint);
        String region = EncryptionDecription.decrypt(awsMailRegion);
        BasicAWSCredentials credentials = new BasicAWSCredentials(emailKey[0].split("=")[1], emailKey[1].split("=")[1]);
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEmailServiceEndPoint, region))
            .build();
        return client;
    }
}
