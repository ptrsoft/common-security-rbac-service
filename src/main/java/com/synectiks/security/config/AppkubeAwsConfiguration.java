package com.synectiks.security.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.synectiks.security.entities.Config;
import com.synectiks.security.entities.Organization;
import com.synectiks.security.service.ConfigService;
import com.synectiks.security.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AppkubeAwsConfiguration {

    @Autowired
    public ConfigService configService;

    @Autowired
    public OrganizationService organizationService;


    @Bean
    public BasicAWSCredentials getBasicAwsCredentials(){
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsAcKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_ACCESS_KEY, organization.getId());
        Config configAwsSecKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_SECRET_KEY, organization.getId());
        BasicAWSCredentials credentials = new BasicAWSCredentials(configAwsAcKey.getValue(), configAwsSecKey.getValue());
        return credentials;
    }

    @Bean
    public AwsBasicCredentials getAwsBasicCredentials(){
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsAcKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_ACCESS_KEY, organization.getId());
        Config configAwsSecKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_SECRET_KEY, organization.getId());
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(configAwsAcKey.getValue(), configAwsSecKey.getValue());
        return awsCredentials;
    }
    @Bean
    public S3Client getAwsS3Client(){
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsRegionKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_REGION, organization.getId());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(getAwsBasicCredentials());
        S3Client s3Client = S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.of(configAwsRegionKey.getValue()))
            .build();
        return s3Client;
    }

    @Bean
    public SesClient getAwsSesClient(){
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsRegionKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_REGION, organization.getId());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(getAwsBasicCredentials());
        SesClient sesClient = SesClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.of(configAwsRegionKey.getValue()))
            .build();
        return sesClient;
    }

    @Bean
    public AmazonSimpleEmailService getAmazonSimpleEmailServiceClient(){
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsRegionKey = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_REGION, organization.getId());
        Config configAwsEmailEndPoint = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_EMAIL_END_POINT, organization.getId());
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(getBasicAwsCredentials()))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(configAwsEmailEndPoint.getValue(), configAwsRegionKey.getValue()))
            .build();
        return client;
    }
}

