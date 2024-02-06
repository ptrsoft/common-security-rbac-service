package com.synectiks.security.controllers;

import com.synectiks.security.config.Constants;
import com.synectiks.security.entities.Config;
import com.synectiks.security.entities.Organization;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.service.AppkubeAwsS3Service;
import com.synectiks.security.service.ConfigService;
import com.synectiks.security.service.OrganizationService;
import com.synectiks.security.util.IUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_ORGANIZATION, method = RequestMethod.POST)
@CrossOrigin
public class OrganizationController {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

//    @Value("${synectiks.cmdb.organization.url}")
//    private String cmdbOrgUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrganizationService organizationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

    @Autowired
    private ConfigService configService;

    @Autowired
    private AppkubeAwsS3Service appkubeAwsS3Service;


    @GetMapping("/profile-by-id/{id}")
    public ResponseEntity<Organization> getOrgProfileById(@PathVariable Long id) {
        logger.debug("Request to get organization and its profile image by id: {}", id);
        Optional<Organization> oo = organizationService.findOne(id);
        if(!oo.isPresent()){
            logger.error("Organization not found");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
        if(StringUtils.isBlank(oo.get().getFileName())){
            logger.error("Organization does not have profile image");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
        Organization defaultOrganization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsBucket = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_S3_BUCKET_NAME_FOR_ORG_PROFILE_IMAGES, defaultOrganization.getId());
        Config configAwsBucketFolderLocation = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_S3_FOLDER_LOCATION_FOR_ORG_PROFILE_IMAGES, defaultOrganization.getId());
        Organization organization = oo.get();
        File f = appkubeAwsS3Service.downloadFromS3(configAwsBucket.getValue(), configAwsBucketFolderLocation.getValue(), organization.getFileName());
        organization.setProfileImage(IUtils.convertFileToByteArray(f));
        return ResponseEntity.status(HttpStatus.OK).body(organization);
    }

    @GetMapping("/profile-by-name/{name}")
    public ResponseEntity<Organization> getOrgProfileByName(@PathVariable String name) {
        logger.debug("Request to get organization and its profile image by name: {}", name);
        Organization organization = organizationService.getOrganizationByName(name);
        if(organization == null){
            logger.error("Organization not found");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
        if(StringUtils.isBlank(organization.getFileName())){
            logger.error("Organization does not have profile image");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
        Organization defaultOrganization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configAwsBucket = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_S3_BUCKET_NAME_FOR_ORG_PROFILE_IMAGES, defaultOrganization.getId());
        Config configAwsBucketFolderLocation = configService.findByKeyAndOrganizationId(Constants.GLOBAL_AWS_S3_FOLDER_LOCATION_FOR_ORG_PROFILE_IMAGES, defaultOrganization.getId());
        File f = appkubeAwsS3Service.downloadFromS3(configAwsBucket.getValue(), configAwsBucketFolderLocation.getValue(), organization.getFileName());
        organization.setProfileImage(IUtils.convertFileToByteArray(f));
        return ResponseEntity.status(HttpStatus.OK).body(organization);
    }

	@GetMapping("/getAllOrganizations")
	private List<Organization> getAllOrganization() {
        logger.debug("Request to get all organizations");
        return organizationService.findAll();
	}

	@GetMapping("/id/{id}")
    public ResponseEntity<Organization> getOrganization(@PathVariable Long id) {
		logger.debug("Request to get organization by id: {}", id);
        Optional<Organization> oo = organizationService.findOne(id);
        if(oo.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(oo.get());
        }
        logger.error("Organization not found");
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
    }

	@RequestMapping(path = "/getOrganizationByUserName", method = RequestMethod.GET)
	public Organization getOrganizationByUserName(@RequestParam String userName) {
        logger.debug("Request to get organization by user name : {}", userName);
        User user = this.userRepository.findByUsername(userName);
        if(user != null) {
            logger.info("User's organization found: "+  user.getOrganization().getName());
            return user.getOrganization();
        }
		logger.error("User's organization not found:");
		return null;
	}

    @RequestMapping(path = "/name/{organizationName}", method = RequestMethod.GET)
    public Organization getOrganizationByName(@PathVariable String organizationName) {
        logger.debug("Request to get organization by name : {}", organizationName);
        return organizationService.getOrganizationByName(organizationName);
    }

    @RequestMapping(path = "/add")
    public ResponseEntity<Object> addOrganization(@RequestBody Organization organization, @RequestParam boolean pushToCmdb) throws URISyntaxException {
        logger.debug("Request to add organization ");
        if(organization.getId() != null){
            logger.error("Id not null");
            Status st = new Status(417, "Id not null");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if(StringUtils.isBlank(organization.getName())){
            logger.error("organization name null");
            Status st = new Status(417, "organization name null");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        Organization existingOrganization = getOrganizationByName(organization.getName());
        if(existingOrganization != null){
            logger.error("organization already exists");
            Status st = new Status(417, "organization already exists");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        organization.setName(organization.getName().toUpperCase());
        Organization result = organizationService.save(organization);
        logger.info("Organization created successfully");
        if(pushToCmdb){
            logger.info("Creating same organization in CMDB. pushToCmdb is {}",pushToCmdb);
            Organization defaultOrg = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
            Config config = configService.findByKeyAndOrganizationId(Constants.CMDB_ORGANIZATION_URL, defaultOrg.getId());
            URI uri = new URI(config.getValue());

            Organization org = new Organization();
            org.setName(result.getName());
            org.setSecurityServiceOrgId(result.getId());
            try{
                ResponseEntity<Organization> cmdbOrgResp = restTemplate.postForEntity(uri, org, Organization.class);
                if(cmdbOrgResp != null && cmdbOrgResp.getBody() != null){
                    Organization cmdbOrg = cmdbOrgResp.getBody();
                    result.setCmdbOrgId(cmdbOrg.getId());
                    result = organizationService.save(result);
                }
            }catch (Exception e){
                logger.error("Organization could not be created in cmdb: ",e);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @RequestMapping(path = "/push-to-cmdb/{organizationName}")
    public ResponseEntity<Object> pushOrganizationToCmdb(@PathVariable String organizationName) throws URISyntaxException {
        logger.debug("Request to push organization in CMDB");
        Organization existingOrganization = getOrganizationByName(organizationName);
        if(existingOrganization == null){
            logger.error("organization not found");
            Status st = new Status(417, "organization not found");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }

        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config config = configService.findByKeyAndOrganizationId(Constants.CMDB_ORGANIZATION_URL, organization.getId());
        URI uri = new URI(config.getValue());

        Organization org = new Organization();
        org.setName(existingOrganization.getName().toUpperCase());
        org.setSecurityServiceOrgId(existingOrganization.getId());

        try{
            ResponseEntity<Organization> cmdbOrgResp = restTemplate.postForEntity(uri, org, Organization.class);
            if(cmdbOrgResp != null && cmdbOrgResp.getBody() != null){
                Organization cmdbOrg = cmdbOrgResp.getBody();
                existingOrganization.setCmdbOrgId(cmdbOrg.getId());
                existingOrganization = organizationService.save(existingOrganization);
            }
        }catch (Exception e){
            logger.error("Organization could not be created in cmdb: ",e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(existingOrganization);
    }

    private class Status{
        private int code;
        private String message;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }


        public Status(int code, String message){
            this.code = code;
            this.message = message;
        }
    }
}
