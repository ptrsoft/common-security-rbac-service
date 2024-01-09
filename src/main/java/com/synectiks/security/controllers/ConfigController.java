package com.synectiks.security.controllers;

import com.synectiks.security.config.IConsts;
import com.synectiks.security.entities.Config;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.service.ConfigService;
import com.synectiks.security.util.EncryptionDecription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_CONFIG)
@CrossOrigin
public class ConfigController {

	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigService configService;

    @RequestMapping(path = IConsts.API_FIND_KEY, method = RequestMethod.GET)
    public ResponseEntity<Object> findByKey(@PathVariable("key") String key) {
        logger.info("Request tof ind config by key. Key: {}",key);
        List<Config> configList = configService.findByKey(key);
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }
    @RequestMapping(path = "/find-by-key-org-id", method = RequestMethod.GET)
    public ResponseEntity<Object> findByKeyAndOrganizationId(@RequestParam(name = "key", required = true) String key,
                                             @RequestParam(name = "organizationId", required = true) Long organizationId){
        logger.info("Request to find config by key and organization id. Key: {}, organization id: {}",key, organizationId);
        Config config = configService.findByKeyAndOrganizationId(key, organizationId);
        return ResponseEntity.status(HttpStatus.OK).body(config);
    }
    @RequestMapping(path = "/find-by-org-id/{organizationId}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByOrganizationId(@PathVariable("organizationId") Long organizationId){
        logger.info("Request to find config by organization id. organization id: {}",organizationId);
        List<Config> configList = configService.findByOrganizationId(organizationId);
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }
    @RequestMapping(path = "/find-by-created-by/{createdBy}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByCreatedBy(@PathVariable("createdBy") String createdBy){
        logger.info("Request to find config by created by. Created by: {}",createdBy);
        List<Config> configList = configService.findByCreatedBy(createdBy);
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }
    @RequestMapping(path = "/find-by-updated-by/{updatedBy}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByUpdatedBy(@PathVariable("updatedBy") String updatedBy){
        logger.info("Request to find config by updated by. Updated by: {}",updatedBy);
        List<Config> configList = configService.findByUpdatedBy(updatedBy);
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }
    @RequestMapping(path = "/find-by-status-by/{status}", method = RequestMethod.GET)
    public ResponseEntity<Object> findByStatus(@PathVariable("status") String status){
        logger.info("Request to find config by status. Status: {}",status);
        List<Config> configList = configService.findByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }

    @RequestMapping(path = IConsts.API_FIND_ID, method = RequestMethod.GET)
    public ResponseEntity<Object> findOne(@PathVariable("id") Long id) {
        logger.debug("Request to find a config by id. Id: {}", id);
        Config config = configService.findOne(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(config);
    }

    @RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
    public ResponseEntity<Object> findAll() {
        logger.debug("Request to find all configs irrespective of organizations");
        List<Config> configList = configService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(configList);
    }

    @RequestMapping(path = IConsts.API_CREATE, method = RequestMethod.POST)
    public ResponseEntity<Object> save(@RequestBody Config config) {
        logger.debug("Request to create config : {}", config);
        if(config.isEncrypted()){
            config.setValue(EncryptionDecription.encrypt(config.getValue()));
        }
        Config config1 = configService.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(config1);
    }

    @RequestMapping(path = "/create-encrypted", method = RequestMethod.POST)
    public ResponseEntity<Object> saveWithEncryption(@RequestBody Config config) {
        logger.debug("Request to create config with encrypted data");
        config.setValue(EncryptionDecription.encrypt(config.getValue()));
        config.setEncrypted(true);
        Config config1 = configService.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(config1);
    }

    @RequestMapping(path = IConsts.API_DELETE_ID, method = RequestMethod.DELETE)
    public ResponseEntity<Object> delete(@PathVariable("id") Long id) {
        logger.debug("Request to delete a config by id: Id: {}", id);
        configService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body("Config deleted successfully");
    }

}
