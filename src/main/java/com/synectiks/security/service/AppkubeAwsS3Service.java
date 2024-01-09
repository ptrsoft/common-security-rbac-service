package com.synectiks.security.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class AppkubeAwsS3Service {
    private static final Logger logger = LoggerFactory.getLogger(AppkubeAwsS3Service.class);

    @Autowired
    private S3Client s3Client;
    public boolean uploadToS3(String bucketName, String folderLocation, String fileName, File file) {
        logger.info("Uploading file to aws s3 bucket. Bucket: {}, file: {}", bucketName, fileName);
        boolean isSuccess = false;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(folderLocation+fileName)
                    .build();
            s3Client.putObject(request, RequestBody.fromFile(file));
            isSuccess = true;
            logger.info("File uploaded to S3 successfully!");
        } catch (AmazonServiceException e) {
            logger.error("AmazonServiceException : ",e);
        } catch (SdkClientException e) {
            logger.error("SdkClientException : ",e);
        }
        return isSuccess;
    }

    public File downloadFromS3(String bucketName,String folderLocation, String fileName) {
        logger.info("Downloading file from aws s3. Bucket: {}, file: {}", bucketName, fileName);
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(folderLocation+fileName)
            .build();
        ResponseInputStream<GetObjectResponse> response = null;
        BufferedOutputStream outputStream = null;
        File file = null;
        try{
            response = s3Client.getObject(request);
            file = new File(fileName);
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = response.read(buffer)) !=  -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }catch (IOException ioException){
            logger.error("IOException while downloading file from s3. ",ioException);
        }finally {
            try{
                if(response != null) {
                    response.close();
                }
                if(outputStream != null) {
                    outputStream.close();
                }
            }catch (IOException e){
                logger.error("IOException while closing streams",e);
            }

        }
        return file;
    }
}
