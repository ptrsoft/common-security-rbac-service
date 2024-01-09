package com.synectiks.security.service;

import com.synectiks.security.entities.Config;
import com.synectiks.security.repositories.ConfigRepository;
import com.synectiks.security.util.EncryptionDecription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigService {

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @Autowired
    private ConfigRepository configRepository;


    public List<Config> findByKey(String key) {
        logger.info("Find config by key. Key: {}",key);
        List<Config> configList = configRepository.findByKey(key);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public Config findByKeyAndOrganizationId(String key, Long organizationId){
        logger.info("Find config by key and organization id. Key: {}, organization id: {}",key, organizationId);
        Config config = configRepository.findByKeyAndOrganizationId(key, organizationId);
        if(config.isEncrypted()){
            config.setValue(EncryptionDecription.decrypt(config.getValue()));
        }
        return config;
    }

    public List<Config> findByOrganizationId(Long organizationId){
        logger.info("Find config by organization id. organization id: {}",organizationId);
        List<Config> configList = configRepository.findByOrganizationId(organizationId);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public List<Config> findByCreatedBy(String createdBy){
        logger.info("Find config by created by. Created by: {}",createdBy);
        List<Config> configList = configRepository.findByCreatedBy(createdBy);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public List<Config> findByUpdatedBy(String updatedBy){
        logger.info("Find config by updated by. Updated by: {}",updatedBy);
        List<Config> configList = configRepository.findByUpdatedBy(updatedBy);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public List<Config> findByStatus(String status){
        logger.info("Find config by status. Status: {}",status);
        List<Config> configList = configRepository.findByStatus(status);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public List<Config> findByCreatedAt(Date date){
        logger.info("Find config by created at. Created at: {}",date);
        List<Config> configList = configRepository.findByCreatedAt(date);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public List<Config> findByUpdatedAt(Date date){
        logger.info("Find config by updated at. Updated at: {}",date);
        List<Config> configList = configRepository.findByUpdatedAt(date);
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public Optional<Config> findOne(Long id) {
        logger.debug("Get a config by id. Id: {}", id);
        Optional<Config> oConfig = configRepository.findById(id);
        if(oConfig.isPresent() && oConfig.get().isEncrypted()){
            oConfig.get().setValue(EncryptionDecription.decrypt(oConfig.get().getValue()));
        }
        return oConfig;
    }

    public List<Config> findAll() {
        logger.debug("Get all configs irrespective of organizations");
        List<Config> configList = configRepository.findAll();
        for(Config config: configList){
            if(config.isEncrypted()){
                config.setValue(EncryptionDecription.decrypt(config.getValue()));
            }
        }
        return configList;
    }

    public Config save(Config config) {
        logger.debug("Save config : {}", config);
        Config config1 = configRepository.save(config);
        if(config1.isEncrypted()){
            config1.setValue(EncryptionDecription.decrypt(config1.getValue()));
        }
        return config1;
    }

    public void delete(Long id) {
        logger.debug("Delete a config by id: Id: {}", id);
        configRepository.deleteById(id);
    }
}
