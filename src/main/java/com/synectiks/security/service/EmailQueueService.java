package com.synectiks.security.service;

import com.synectiks.security.entities.EmailQueue;
import com.synectiks.security.repositories.EmailQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmailQueueService {

    private final Logger logger = LoggerFactory.getLogger(EmailQueueService.class);

    @Autowired
    private EmailQueueRepository emailQueueRepository;

    public List<EmailQueue> findByCreatedBy(String createdBy){
        logger.info("Find emails by created by. Created by: {}",createdBy);
        return emailQueueRepository.findByCreatedBy(createdBy);
    }

    public List<EmailQueue> findByUpdatedBy(String updatedBy){
        logger.info("Find emails by updated by. Updated by: {}",updatedBy);
        return emailQueueRepository.findByUpdatedBy(updatedBy);
    }

    public List<EmailQueue> findByStatus(String status) {
        logger.info("Find emails by status. Status: {}",status);
        return emailQueueRepository.findByStatus(status);
    }

    public List<EmailQueue> findByStatusAndMailType(String status, String mailType) {
        logger.info("Find emails by status and mail type. Status: {}, mail type: {}",status, mailType);
        return emailQueueRepository.findByStatusAndMailType(status, mailType);
    }
    public List<EmailQueue> findByStatusAndOrganizationId(String status, Long organizationId){
        logger.info("Find emails by status and organization id. Key: {}, organization id: {}",status, organizationId);
        return emailQueueRepository.findByStatusAndOrganizationId(status, organizationId);
    }

    public List<EmailQueue> findByOrganizationId(Long organizationId){
        logger.info("Find emails by organization id. organization id: {}",organizationId);
        return emailQueueRepository.findByOrganizationId(organizationId);
    }

    public List<EmailQueue> findByMailSubject(String mailSubject) {
        logger.info("Find emails by mail subject. Subject: {}",mailSubject);
        return emailQueueRepository.findByMailSubject(mailSubject);
    }

    public List<EmailQueue> findByMailTo(String mailTo) {
        logger.info("Find emails by to email id. To email id: {}",mailTo);
        return emailQueueRepository.findByMailTo(mailTo);
    }
    public List<EmailQueue> findByMailFrom(String mailFrom) {
        logger.info("Find emails by from email id. From email id: {}",mailFrom);
        return emailQueueRepository.findByMailFrom(mailFrom);
    }
    public List<EmailQueue> findByMailType(String mailType) {
        logger.info("Find emails by from email type. Email type: {}",mailType);
        return emailQueueRepository.findByMailType(mailType);
    }

    public Optional<EmailQueue> findOne(Long id) {
        logger.debug("Get a emails by id. Id: {}", id);
        return emailQueueRepository.findById(id);
    }

    public List<EmailQueue> findAll() {
        logger.debug("Get all emails");
        return emailQueueRepository.findAll();
    }

    public EmailQueue save(EmailQueue emailQueue) {
        logger.debug("Save email in email_queue : {}", emailQueue);
        return emailQueueRepository.save(emailQueue);
    }

    public void delete(Long id) {
        logger.debug("Delete an email by id: Id: {}", id);
        emailQueueRepository.deleteById(id);
    }
}
