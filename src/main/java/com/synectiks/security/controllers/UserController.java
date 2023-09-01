/**
 *
 */
package com.synectiks.security.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.Constants;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.email.MailService;
import com.synectiks.security.entities.*;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.mfa.GoogleMultiFactorAuthenticationService;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.RoleRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.service.DocumentService;
import com.synectiks.security.util.IUtils;
import com.synectiks.security.util.RandomGenerator;
import com.synectiks.security.util.TemplateReader;
import com.synectiks.security.util.Token;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_USER, method = RequestMethod.POST)
@CrossOrigin
public class UserController implements IApiController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Value("${synectiks.cmdb.organization.url}")
	private String cmdbOrgUrl;
	private DefaultPasswordService pswdService = new DefaultPasswordService();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private TemplateReader templateReader;

	@Autowired
	private GoogleMultiFactorAuthenticationService googleMultiFactorAuthenticationService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DocumentService documentService;

	@Override
	@RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
	// @RequiresRoles("ROLE_" + IConsts.ADMIN)
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<User> entities = null;
		try {
			entities = (List<User>) userRepository.findAll();
			for (User by : entities) {
				getDocumentList(by);
				setProfileImage(by);
			}
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entities);
	}

	private void setProfileImage(User by) throws IOException {
		Map<String, String> requestObj = new HashMap<>();
		requestObj.put("sourceId", String.valueOf(by.getId()));
		requestObj.put("identifier", Constants.IDENTIFIER_PROFILE_IMAGE);
		List<Document> docList = documentService.searchDocument(requestObj);
		for (Document doc : docList) {
			if (doc.getIdentifier().equalsIgnoreCase(Constants.IDENTIFIER_PROFILE_IMAGE)) {
				if (doc.getLocalFilePath() != null) {
                    if(Files.exists(Paths.get(doc.getLocalFilePath()))){
                        byte[] bytes = Files.readAllBytes(Paths.get(doc.getLocalFilePath()));
                        by.setProfileImage(bytes);
                    }
				}
				break;
			}
		}
	}

	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestParam(name = "type", required = false) String type,
                                         @RequestParam(name = "organization", required = false) String organization,
                                         @RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         @RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
		User user = this.userRepository.findByUsername(username);
		if (user != null) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Login id already exists", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		// check for duplicate email
		user = new User();
		user.setEmail(email);
		Optional<User> oUser = this.userRepository.findOne(Example.of(user));
		if (oUser.isPresent()) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email already exists", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		user = new User();
		try {
//			String signedInUser = IUtils.getUserFromRequest(request);
//			user = IUtils.createEntity(service, signedInUser, User.class);
			createUserFromJson(user, type, organization, username, password, email);
			// Encrypt the password
			if (!IUtils.isNullOrEmpty(user.getPassword()) && !user.getPassword().startsWith("$shiro1$")) {
				user.setPassword(pswdService.encryptPassword(user.getPassword()));
			}
			Date currentDate = new Date();
            user.setCreatedAt(currentDate);
            user.setUpdatedAt(currentDate);

			saveOrUpdateOrganization(organization, user, currentDate);
            user.setCreatedBy(Constants.SYSTEM_ACCOUNT);
            user.setUpdatedBy(Constants.SYSTEM_ACCOUNT);

			logger.info("Saving user: " + user);
			user = userRepository.save(user);
			addProfileImage(file, user, currentDate);
			getDocumentList(user);
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error("Exception: ",th);
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Service issues. User data cannot be saved", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	private void getDocumentList(User user) {
		Map<String, String> requestObj = new HashMap<>();
		requestObj.put("sourceId", String.valueOf(user.getId()));
		List<Document> docList = documentService.searchDocument(requestObj);
		List<Document> finalDocList = new ArrayList<>();
		for (Document doc : docList) {
			if (!doc.getIdentifier().equalsIgnoreCase(Constants.IDENTIFIER_PROFILE_IMAGE)) {
				finalDocList.add(doc);
			}
		}
		user.setDocumentList(finalDocList);
	}

	private void addProfileImage(MultipartFile file, User user, Date currentDate) throws IOException {
		if (file != null) {
			byte[] bytes = file.getBytes();
			String orgFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
			String ext = "";
			if (orgFileName.lastIndexOf(".") != -1) {
				ext = orgFileName.substring(orgFileName.lastIndexOf(".") + 1);
			}
			String filename = orgFileName;
//			if (json.get("name") != null) {
//				filename = json.get("name").toString();
//			}
			filename = filename.toLowerCase().replaceAll(" ", "-") + "_" + System.currentTimeMillis() + "." + ext;
			File localStorage = new File(Constants.LOCAL_PROFILE_IMAGE_STORAGE_DIRECTORY);
			if (!localStorage.exists()) {
				localStorage.mkdirs();
			}
			Path path = Paths.get(localStorage.getAbsolutePath() + File.separatorChar + filename);
			Files.write(path, bytes);

			Document document = new Document();
			document.setFileName(filename);
			document.setFileExt(ext);
//			document.setFileType(Constants.FILE_TYPE_IMAGE);
			document.setFileSize(file.getSize());
			document.setStorageLocation(Constants.FILE_STORAGE_LOCATION_LOCAL);
			document.setLocalFilePath(localStorage.getAbsolutePath() + File.separatorChar + filename);
//			document.setSourceOfOrigin(this.getClass().getSimpleName());
			document.setSourceId(user.getId().toString());
			document.setIdentifier(Constants.IDENTIFIER_PROFILE_IMAGE);
			document.setCreatedOn(currentDate);
			document.setUpdatedBy(Constants.SYSTEM_ACCOUNT);
			document.setCreatedBy(user.getCreatedBy());
			document.setUpdatedOn(user.getUpdatedAt());
			document = documentService.saveDocument(document);
			user.setProfileImage(bytes);
			logger.debug("user profile image saved successfully");
		}

	}

	@Override
	@RequestMapping(IConsts.API_FIND_ID)
	public ResponseEntity<Object> findById(@PathVariable("id") Long id) {
		User entity = null;
		try {
			entity = userRepository.findById(id).orElse(null);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(IConsts.API_DELETE_ID)
	public ResponseEntity<Object> deleteById(@PathVariable("id") Long id) {
		try {
			userRepository.deleteById(id);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body("User removed Successfully");
	}

	@Override
	@RequestMapping(IConsts.API_UPDATE)
	public ResponseEntity<Object> update(@RequestBody ObjectNode reqObje, HttpServletRequest request) {
		logger.debug("Request to update user" + reqObje.get("username").asText());
		User user = new User();
		Optional<User> oUser = null;
		if (!StringUtils.isBlank(reqObje.get("email").asText())) {
			user.setEmail(reqObje.get("email").asText());
			try {
				oUser = this.userRepository.findOne(Example.of(user));
				if (oUser.isPresent()) {
					if (!oUser.get().getUsername().equals(reqObje.get("username").asText())) {
                        Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email id already exists", null);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
					}
				}
			} catch (IncorrectResultSizeDataAccessException e) {
                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email id already exists", null);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
		}

		try {
			logger.debug("user json: " + reqObje);
			if (oUser != null && oUser.isPresent()) {
				user = oUser.get();
			} else {
				user = new User();
				user.setUsername(reqObje.get("username").asText());
				user = this.userRepository.findOne(Example.of(user)).orElse(null);
			}
			if (!StringUtils.isBlank(reqObje.get("email").asText())) {
				user.setEmail(reqObje.get("email").asText());
			}

			if (!StringUtils.isBlank(reqObje.get("password").asText())) {
				// Encrypt the password
				user.setPassword(pswdService.encryptPassword(reqObje.get("password").asText()));
			}

			Date currentDate = new Date();
			saveUpdateOrganization(reqObje, user, currentDate);
			user.setUpdatedAt(currentDate);

			if (reqObje.get("username") != null) {
				user.setUpdatedBy(reqObje.get("username").asText());
			} else {
				user.setUpdatedBy(Constants.SYSTEM_ACCOUNT);
			}
			logger.info("Updating user: " + user);
			userRepository.save(user);

		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR",th.getMessage(), null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}

	private void saveOrUpdateOrganization(String orgName, User user, Date currentDate) throws URISyntaxException {
		if (!StringUtils.isBlank(orgName)) {
			Organization organization = new Organization();
			organization.setName(orgName.toUpperCase());
			Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
			if (oOrg.isPresent()) {
				user.setOrganization(oOrg.get());
			} else {
				logger.info("Saving new organization: " + organization);
				organization.setCreatedAt(currentDate);
				organization.setUpdatedAt(currentDate);
                organization.setCreatedBy(user.getCreatedBy());
                organization.setUpdatedBy(user.getUpdatedBy());

				organization = this.organizationRepository.save(organization);

				URI uri = new URI(cmdbOrgUrl);
				Organization org = new Organization();
				org.setName(organization.getName());
				org.setSecurityServiceOrgId(organization.getId());
				ResponseEntity<Organization> result = restTemplate.postForEntity(uri, org, Organization.class);
                if(result != null && result.getBody() != null){
                    try{
                        Organization cmdbOrg = result.getBody();
                        organization.setCmdbOrgId(cmdbOrg.getId());
                        organization = this.organizationRepository.save(organization);
                        user.setOrganization(organization);
                    }catch (Exception e){
                        logger.error("Organization could not be retrieved from dmdb: ",e);
                        user.setOrganization(organization);
                    }

                }

            }
		}
	}

	private void saveUpdateOrganization(ObjectNode  reqObje, User user, Date currentDate) throws URISyntaxException {
		if (!StringUtils.isBlank(reqObje.get("organization").toString())) {
			Organization organization = new Organization();
			organization.setName(reqObje.get("organization").toString().toUpperCase());
			Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
			if (oOrg.isPresent()) {
				user.setOrganization(oOrg.get());
			} else {
				logger.info("Saving new organization: " + organization);
				organization.setCreatedAt(currentDate);
				organization.setUpdatedAt(currentDate);
				if (reqObje.get("username") != null) {
					organization.setCreatedBy(reqObje.get("username").toString());
					organization.setUpdatedBy(reqObje.get("username").toString());
				} else {
					organization.setCreatedBy(Constants.SYSTEM_ACCOUNT);
					organization.setUpdatedBy(Constants.SYSTEM_ACCOUNT);
				}
				organization = this.organizationRepository.save(organization);
				user.setOrganization(organization);

				URI uri = new URI(cmdbOrgUrl);
				Organization org = new Organization();
				org.setName(organization.getName());
				org.setSecurityServiceOrgId(organization.getId());
				ResponseEntity<Organization> result = restTemplate.postForEntity(uri, org, Organization.class);
			}
		}
	}

	@Override
	@RequestMapping(IConsts.API_DELETE)
	public ResponseEntity<Object> delete(@RequestBody ObjectNode entity) {
		if (!IUtils.isNull(entity.get(IDBConsts.Col_ID))) {
			return deleteById(entity.get(IDBConsts.Col_ID).asLong());
		}
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Not a valid entity");
	}

	@RequestMapping(method = RequestMethod.GET, path = IApiController.URL_SEARCH)
	public ResponseEntity<Object> search(@RequestBody ObjectNode reqObj) {
		boolean isFilter = false;
		User user = null;
		List<User> list = null;
		try {
			if (reqObj.get("isExternalSecurityEnable ") != null
					&& reqObj.get("isExternalSecurityEnable ").asBoolean() == false) {
				list = this.userRepository.findAll(Sort.by(Direction.ASC, "username"));
				logger.debug("Getting all the users");
				return ResponseEntity.status(HttpStatus.OK).body(list);
			}

			if (reqObj.get("id") != null) {
				user.setId(Long.parseLong(reqObj.get("id").asText()));
				isFilter = true;
			}
			if (reqObj.get("username") != null) {
				user.setUsername(reqObj.get("username").asText());
				isFilter = true;
			}
			if (reqObj.get("email") != null) {
				user.setEmail(reqObj.get("email").asText());
				isFilter = true;
			}
			if (reqObj.get("type") != null) {
				user.setType(reqObj.get("type").asText());
				isFilter = true;
			}
			if (reqObj.get("organization") != null) {
				Organization organization = new Organization();
				organization.setName(reqObj.get("organization").asText().toUpperCase());
				Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
				if (oOrg.isPresent()) {
					user.setOrganization(oOrg.get());
				}
				isFilter = true;
			}

			if (reqObj.get("active") != null) {
				user.setActive(reqObj.get("active").asBoolean());
				isFilter = true;
			}

			if (reqObj.get("ownerId") != null) {
				Optional<User> parent = this.userRepository.findById(reqObj.get("ownerId").asLong());
				if (parent.isPresent()) {
					user.setOwner(parent.get());
				}
				isFilter = true;
			}

			if (isFilter) {
				list = this.userRepository.findAll(Example.of(user), Sort.by(Direction.ASC, "username"));
			} else {
				list = this.userRepository.findAll(Sort.by(Direction.ASC, "username"));
			}
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	private void createUserFromJson(User user, String type, String organization, String username, String password, String email) {
		if (!StringUtils.isBlank(type)) {
			user.setType(type);
		}
		if (!StringUtils.isBlank(username)) {
			user.setUsername(username);
		}
		if (!StringUtils.isBlank(password)) {
			user.setPassword(password);
		}
//		if (reqObj.get("active") != null) {
			user.setActive(true);
//		}
		if (!StringUtils.isBlank(email)) {
			user.setEmail(email);
		}

//		user.setOwnerId(reqObj.get("ownerId") != null ? reqObj.get("ownerId").asLong() : null);
		if (!StringUtils.isBlank(organization)) {
			Organization org = new Organization();
            org.setName(organization.toUpperCase());
			Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(org));
			if (oOrg.isPresent()) {
				user.setOrganization(oOrg.get());
			}
		}
	}

	@RequestMapping(path = "/updateOrganization", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateOrganization(@RequestBody Map<String, String> reqObj) {
		logger.info("Request to update user's organization. User: " + reqObj.get("userName") + ", Organization: "
				+ reqObj.get("organizationName"));
		User user = new User();
		user.setUsername(reqObj.get("userName"));
		Optional<User> oUser = this.userRepository.findOne(Example.of(user));
		if (oUser.isPresent()) {
			user = oUser.get();
		}

		Date currentDate = new Date();

		Organization organization = new Organization();
		organization.setName(reqObj.get("organizationName").toUpperCase());
		Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
		if (oOrg.isPresent()) {
			logger.info("Organization with the given name found. Assigning existing organization to user");
			user.setOrganization(oOrg.get());
		} else {
			logger.info("Organization no found. Creating new organization");
			organization.setCreatedAt(currentDate);
			organization.setUpdatedAt(currentDate);
			organization.setCreatedBy(reqObj.get("userName"));
			organization.setUpdatedBy(reqObj.get("userName"));
			organization = this.organizationRepository.save(organization);
			user.setOrganization(organization);
		}

		try {
			user.setUpdatedAt(currentDate);
			user.setUpdatedBy(reqObj.get("userName"));
			userRepository.save(user);
			logger.info("User's organization updated: " + user);

		} catch (Throwable th) {
			logger.error("Update organization failed. Exception: ", th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}

	@RequestMapping(path = "/updateAssignedRoleGroups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateAssignedRoleGroups(@RequestBody User user) {
		logger.info("Request to update user's role group");
		try {
			userRepository.save(user);
		} catch (Exception e) {
			logger.error("Assign role groups to user failed. Exception: ", e);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Assign role groups to user failed");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}

	@RequestMapping(path = "/updateUserRoles")
	public ResponseEntity<Object> updateUserRoles(@RequestParam String userName, @RequestParam String roleId) {
		logger.info("Request to update user's role");
		Status st = new Status();
		try {
			User user = this.userRepository.findByUsername(userName);

			List<Role> roleList = new ArrayList<>();
			StringTokenizer token = new StringTokenizer(roleId, ",");
			while (token.hasMoreTokens()) {
				Optional<Role> oRole = roleRepository.findById(Long.parseLong(token.nextToken()));
				if (oRole.isPresent()) {
					roleList.add(oRole.get());
				}
			}
			user.setRoles(roleList);
			userRepository.save(user);

			st.setCode(HttpStatus.OK.value());
			st.setType("SUCCESS");
			st.setMessage("User's role updated successfully");
			st.setObject(user);
			return ResponseEntity.status(HttpStatus.OK).body(st);
		} catch (Exception e) {
			logger.error("Updating user's role failed. Exception: ", e);
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Updating user's role failed");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e);
		}
	}

	@RequestMapping(path = "/inviteUser")
	public ResponseEntity<Object> createUserInvite(@RequestParam String username, @RequestParam String inviteeEmail) {
		logger.info("Request to create a new user invite");
		User user = this.userRepository.findByUsername(inviteeEmail);
		if (user != null) {
			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR",inviteeEmail + " already exists. Please choose a different user id", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}

		try {
			user = new User();
			user.setEmail(inviteeEmail);
			user.setActive(true);
			Optional<User> oUser = this.userRepository.findOne(Example.of(user));
			if (oUser.isPresent()) {
				logger.warn("Another user with email id: " + inviteeEmail + " already exists");
				Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email id: " + inviteeEmail + " already exists", null);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
			user.setActive(false);
			oUser = this.userRepository.findOne(Example.of(user));
			if (oUser.isPresent()) {
				logger.warn("Another user with email id: " + inviteeEmail + " already exists");
				Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email id: " + inviteeEmail + " already exists", null);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
		} catch (Exception e) {
			logger.warn("Email id: " + inviteeEmail + " already exists", e.getMessage());
			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Email already exists", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}

		String invitationCode = RandomGenerator.getRandomValue();
		String activationLink = "http://" + Constants.HOST + ":" + Constants.PORT
				+ "/inviteaccept.html?activation_code=" + invitationCode;
		User invitee = new User();
		try {
			User owner = new User();
			owner.setUsername(username);
			owner.setActive(true);
			Optional<User> oOwner = userRepository.findOne(Example.of(owner));
			if (!oOwner.isPresent()) {
                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Owner not found. Please check owner's email id", null);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
//
			Date currentDate = new Date();

			invitee.setUsername(inviteeEmail);
			invitee.setEmail(inviteeEmail);
			invitee.setOwner(oOwner.get());
			invitee.setInviteStatus(Constants.USER_INVITE_ACCEPTENCE_PENDING);
			invitee.setInviteSentOn(currentDate);
			invitee.setActive(true);
			invitee.setOrganization(oOwner.get().getOrganization());
			invitee.setIsMfaEnable(Constants.NO);
			invitee.setInviteCode(invitationCode);
			invitee.setTempPassword(RandomGenerator.getTemporaryPassword());
			invitee.setCreatedAt(currentDate);
			invitee.setUpdatedAt(currentDate);
			invitee.setCreatedBy(oOwner.get().getUsername());
			invitee.setUpdatedBy(oOwner.get().getUsername());
			invitee.setInviteLink(activationLink);
			invitee = userRepository.save(invitee);
			logger.info("User invite saved in db");

			String templateData = this.templateReader.readTemplate("/userinvite.ftl");
			logger.debug("Injecting dynamic data in user invite template");
			templateData = templateData.replace("${ownerName}", username);
			templateData = templateData.replace("${inviteLink}", activationLink);
			String subject = "User invitation to join Synectiks";
			MimeMessage mimeMessage = this.mailService.createHtmlMailMessage(templateData, inviteeEmail, subject);
			this.mailService.sendEmail(mimeMessage);
			logger.info("User invitation mail send");
            Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Invitation link sent to user's email: " + inviteeEmail, invitee);
            return ResponseEntity.status(HttpStatus.OK).body(st);

		} catch (Exception e) {
			logger.error("User invite failed. Exception: ", e);
			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User invite failed", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
	}

	@RequestMapping(path = "/acceptInvite")
	public ResponseEntity<Object> acceptInvite(@RequestParam String inviteCode) {
		logger.info("Request to accept user invite");
		try {
			User invitee = new User();
			invitee.setInviteCode(inviteCode);
			invitee.setActive(true);
			invitee.setInviteStatus(Constants.USER_INVITE_ACCEPTENCE_PENDING);

			Optional<User> oInvitee = userRepository.findOne(Example.of(invitee));
			if (!oInvitee.isPresent()) {
				Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User authentication failed. Invitation cannot be accepted", null);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
			invitee = oInvitee.get();

			Date currentDate = new Date();

			invitee.setInviteStatus(Constants.USER_INVITE_ACCEPTED);
			invitee.setActive(true);
			invitee.setUpdatedAt(currentDate);
			invitee.setUpdatedBy(invitee.getUsername());
			invitee.setPassword(pswdService.encryptPassword(invitee.getTempPassword()));
			invitee = userRepository.save(invitee);
			logger.info("User invite accepted and saved in db");

			User discardUsers = new User();
			discardUsers.setActive(false);
			discardUsers.setOwner(invitee.getOwner());
			discardUsers.setOrganization(invitee.getOrganization());
			discardUsers.setInviteStatus(Constants.USER_INVITE_ACCEPTENCE_PENDING);
			logger.info("Deleting all the additional invitation requests for the same user");
			List<User> discardUsersList = userRepository.findAll(Example.of(discardUsers));
			for (User us : discardUsersList) {
				userRepository.delete(us);
			}

			String templateData = this.templateReader.readTemplate("/usercredential.ftl");
			templateData = templateData.replace("${loginId}", invitee.getUsername());
			templateData = templateData.replace("${password}", invitee.getTempPassword());
			String subject = "Login credentials of Synectiks cloud monitoring application";
			MimeMessage mimeMessage = this.mailService.createHtmlMailMessage(templateData, invitee.getEmail(), subject);
			this.mailService.sendEmail(mimeMessage);
			logger.info("User credential mail send");
			Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Invitation accepted. Login id and password sent to user's email: " + invitee.getEmail(), invitee);
            return ResponseEntity.status(HttpStatus.OK).body(st);

		} catch (Exception e) {
			logger.error("User invite acceptance failed. Exception: ", e);
			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User invite acceptance failed", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
	}

	@RequestMapping(method = RequestMethod.GET, path = "/getTeam")
	public ResponseEntity<Object> getTeam(@RequestParam Map<String, String> reqObj) {
		logger.info("Request to get list of team members");
		User user = new User();
		try {

			if (reqObj.get("organization") != null) {
				Organization organization = new Organization();
				organization.setName(reqObj.get("organization").toUpperCase());
				Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
				if (oOrg.isPresent()) {
					user.setOrganization(oOrg.get());
				}
			}

			if (reqObj.get("userName") != null) {
				user.setUsername(reqObj.get("userName"));
			}

			user.setActive(true);

			Optional<User> oOwner = userRepository.findOne(Example.of(user));
			if (oOwner.isPresent()) {
				user.setId(oOwner.get().getId());
				user.setEmail(oOwner.get().getEmail());
				user.setPassword(oOwner.get().getPassword());
				user.setActive(oOwner.get().isActive());
				user.setCreatedAt(oOwner.get().getCreatedAt());
				user.setCreatedBy(oOwner.get().getCreatedBy());
				user.setUpdatedAt(oOwner.get().getUpdatedAt());
				user.setUpdatedBy(oOwner.get().getUpdatedBy());
				user.setType(oOwner.get().getType());
				user.setGoogleMfaKey(oOwner.get().getGoogleMfaKey());
				user.setIsMfaEnable(oOwner.get().getIsMfaEnable());
				user.setMfaQrImageFilePath(oOwner.get().getMfaQrImageFilePath());
				user.setInviteCode(oOwner.get().getInviteCode());
				user.setInviteLink(oOwner.get().getInviteLink());
				user.setInviteSentOn(oOwner.get().getInviteSentOn());
				user.setInviteStatus(oOwner.get().getInviteStatus());
				user.setTempPassword(oOwner.get().getTempPassword());

				List<User> allUserList = this.userRepository.findAll(Sort.by(Direction.ASC, "username"));
				List<User> activeUserList = new ArrayList<>();
				List<User> pendingUsersList = new ArrayList<>();
				for (User acUser : allUserList) {
					if (!StringUtils.isBlank(acUser.getIsMfaEnable())
							&& Constants.YES.equalsIgnoreCase(acUser.getIsMfaEnable())) {
						acUser.setIsMfaEnable("Enabled");
					} else {
						acUser.setIsMfaEnable("Disabled");
					}
					if (!StringUtils.isBlank(acUser.getMfaQrImageFilePath())) {
						File qrFile = new File(acUser.getMfaQrImageFilePath());
						if (qrFile.exists() && qrFile.isFile()) {
							acUser.setMfaQrCode(Files.readAllBytes(qrFile.toPath()));
						}
					}

					if (acUser.getOwner() != null && acUser.getOwner().getId().equals(user.getId())
							&& (!StringUtils.isBlank(acUser.getInviteStatus())
									&& Constants.USER_INVITE_ACCEPTED.equals(acUser.getInviteStatus()))) {
						acUser.setInviteStatus("Invite Accepted");
						if (oOwner.get().getRoles() != null && oOwner.get().getRoles().size() > 0) {
							acUser.setRoles(oOwner.get().getRoles());
						}
						activeUserList.add(acUser);
					}
					if (acUser.getOwner() != null && acUser.getOwner().getId().equals(user.getId())
							&& (!StringUtils.isBlank(acUser.getInviteStatus())
									&& Constants.USER_INVITE_ACCEPTENCE_PENDING.equals(acUser.getInviteStatus()))) {
						acUser.setInviteStatus("Acceptance Pending");

						if (oOwner.get().getRoles() != null && oOwner.get().getRoles().size() > 0) {
							acUser.setRoles(oOwner.get().getRoles());
						}

						pendingUsersList.add(acUser);
					}

				}
				user.setTeamList(activeUserList);
				user.setPendingInviteList(pendingUsersList);
			}

		} catch (Throwable th) {
			logger.error("Exception in getTeam: ", th);
			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Due to some error, team list cannot be retrieved", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Team list", user);
        return ResponseEntity.status(HttpStatus.OK).body(st);
	}

    @RequestMapping(path = "/mfaCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMfaCode(@RequestBody ObjectNode reqObj) {
        logger.info("Request to get google mfa for user: {}", reqObj.get("userName").asText());
        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if (StringUtils.isBlank(reqObj.get("password").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Password not provided", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        try {
            UsernamePasswordToken token = new UsernamePasswordToken();
            token.setUsername(reqObj.get("userName").asText());
            token.setPassword(reqObj.get("password").asText().toCharArray());
            AuthenticationInfo info = SecurityUtils.getSecurityManager().authenticate(token);

            String mfaKey = googleMultiFactorAuthenticationService.getGoogleAuthenticationKey(reqObj.get("userName").asText());
            int size = 125;
            String fileType = "png";
            String keyUri = googleMultiFactorAuthenticationService.generateGoogleAuthenticationUri(reqObj.get("userName").asText(),
                "Synectiks ", mfaKey);

            File tempFile = File.createTempFile(reqObj.get("userName").asText(), "."+fileType);
            googleMultiFactorAuthenticationService.createQRImage(tempFile, keyUri, size, fileType);
            Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA QR code created successfully",Files.readAllBytes(tempFile.toPath()));
            st.setMfaKey(mfaKey);
            tempFile.deleteOnExit();
            return ResponseEntity.status(HttpStatus.OK).body(st);
        } catch (Exception e) {
            String errMsg = "Exception in getting mfa";
            logger.error(errMsg+": ", e);
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR",errMsg, null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
    }

    @RequestMapping(value = "/authenticateMfa", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> authenticateGoogleMfa(@RequestBody ObjectNode reqObj) {
        logger.error("Request to authenticate google MFA token");

        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if (StringUtils.isBlank(reqObj.get("token").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","MFA token not provided", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }

        try {
            User user = this.userRepository.findByUsername(reqObj.get("userName").asText());
            if(user == null){
                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User name : "+reqObj.get("userName").asText(), null);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
            }
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean matches = false;
            if(!StringUtils.isBlank(user.getIsMfaEnable()) && Constants.YES.equalsIgnoreCase(user.getIsMfaEnable())){
                matches = gAuth.authorize(user.getGoogleMfaKey(), Integer.parseInt(reqObj.get("token").asText()));
                if(matches) {
                    logger.debug("Google mfa authentication successful");
                }else {
                    logger.warn("Google mfa authentication failed");
                }
            }else{
                matches = gAuth.authorize(reqObj.get("mfaKey").asText(), Integer.parseInt(reqObj.get("token").asText()));
                if(matches){
                    logger.info("Google MFA authentication successful for user {}",reqObj.get("userName").asText());
                    user.setIsMfaEnable(Constants.YES);
                    user.setGoogleMfaKey(reqObj.get("mfaKey").asText());
                    user = userRepository.save(user);
                    logger.info("Google MFA is enabled for user: {}", reqObj.get("userName").asText());
                }else{
                    logger.warn("Google MFA token authentication failed while setting up MFA for user: {}", reqObj.get("userName").asText());
                }

            }
            Status st = setMessage(HttpStatus.OK.value(), "SUCCESS",matches == true ? "Google MFA token authenticated" : "Google MFA token authentication failed", matches);
            return ResponseEntity.status(HttpStatus.OK).body(st);
        }catch(Exception e) {
            logger.error("Exception in MFA authentication: ",e);
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Exception in MFA authentication", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
    }

    @RequestMapping(value = "/disableMfa", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> disableGoogleMfa(@RequestBody ObjectNode reqObj) {
        logger.error("Request to disable google MFA");

        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided",null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if (StringUtils.isBlank(reqObj.get("token").asText())) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","MFA token not provided",null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }

        try {
            User user = this.userRepository.findByUsername(reqObj.get("userName").asText());
            if(user == null){
                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User name : "+reqObj.get("userName").asText(), null);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
            }
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean matches = false;
            Status st = null;
            if(!StringUtils.isBlank(user.getIsMfaEnable()) && Constants.YES.equalsIgnoreCase(user.getIsMfaEnable())){
                matches = gAuth.authorize(user.getGoogleMfaKey(), Integer.parseInt(reqObj.get("token").asText()));
                if(matches) {
                    logger.info("Google MFA authentication successful. Disabling MFA for user {}",reqObj.get("userName").asText());
                    user.setIsMfaEnable(null);
                    user.setGoogleMfaKey(null);
                    user = userRepository.save(user);
                    logger.info("Google MFA is disabled for user: {}", reqObj.get("userName").asText());
                    st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA token disabled", matches);
                }else {
                    logger.warn("Google mfa authentication failed");
                    st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA token authentication failed. MFA could not be disabled", matches);
                }
            }else{
                logger.info("Google MFA is already disabled for user: {}", reqObj.get("userName").asText());
                st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA is already disabled", null);
            }
            return ResponseEntity.status(HttpStatus.OK).body(st);
        }catch(Exception e) {
            logger.error("Exception while disabling MFA token: ",e);
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Exception while disabling MFA token", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
    }


    @RequestMapping(value = "/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ObjectNode reqObje) throws IOException {
        String userName = reqObje.get("userName").asText();
        User user = this.userRepository.findByUsername(userName);
        if (user == null) {
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User Name: "+userName, null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }

        String ourToken = Token.get(userName);
        if(StringUtils.isBlank(ourToken)){
            logger.error("Token not found");
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Invalid token: ", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        String token = reqObje.get("token").asText();
        if(StringUtils.isBlank(token) || !ourToken.equals(token)){
            logger.error("Invalid token");
            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Invalid token: ", null);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }

//        String oldPassword = reqObje.get("oldPassword").asText();
//
//        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken();
//        usernamePasswordToken.setUsername(user.getUsername());
//        usernamePasswordToken.setPassword(oldPassword.toCharArray());
//
//        try{
//            AuthenticationInfo info = SecurityUtils.getSecurityManager().authenticate(usernamePasswordToken);
//        } catch (UnknownAccountException th) {
//            Token.remove(userName);
//            logger.error(th.getMessage(), th);
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","UnknownAccountException: ", th);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        } catch (IncorrectCredentialsException th) {
//            Token.remove(userName);
//            logger.error(th.getMessage(), th);
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","IncorrectCredentialsException: ", th);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        } catch (LockedAccountException th) {
//            Token.remove(userName);
//            logger.error(th.getMessage(), th);
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","LockedAccountException: ", th);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        } catch (AuthenticationException th) {
//            Token.remove(userName);
//            logger.error(th.getMessage(), th);
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","AuthenticationException: ", th);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }

        String newPassword = reqObje.get("newPassword").asText();
        user.setPassword(pswdService.encryptPassword(newPassword));
        userRepository.save(user);
        Token.remove(userName);
        Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "SUCCESS","Password changed successfully: ", null);
        return ResponseEntity.status(HttpStatus.OK).body(st);
    }

//	@RequestMapping(path = "/enableGoogleMfa")
//	public ResponseEntity<Object> enableGoogleMfa(@RequestParam final String userName,
//			@RequestParam final String organizationName) {
//		logger.info("Request to enable google mfa for user: {}", userName);
//		try {
//			User user = userRepository.findByUsername(userName);
//			if (user == null) {
//				logger.error("User not found. User: {}, Organization: {}", userName, organizationName);
//				Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found", null);
//                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//			}
//
//			user.setIsMfaEnable("YES");
//			String mfaKey = googleMultiFactorAuthenticationService.getGoogleAuthenticationKey(userName);
//			user.setGoogleMfaKey(mfaKey);
//
//			String directory = "qrimages/" + organizationName;
//			File dir = new File(directory);
//			if (!dir.exists()) {
//				dir.mkdirs();
//			}
//			String fileName = userName + ".png";
//			String filePath = directory + "/" + fileName;
//			int size = 125;
//			String fileType = "png";
//			File qrFile = new File(filePath);
//			String keyUri = googleMultiFactorAuthenticationService.generateGoogleAuthenticationUri(userName,
//					"Synectiks ", mfaKey);
//			googleMultiFactorAuthenticationService.createQRImage(qrFile, keyUri, size, fileType);
//
//			user.setMfaQrCode(Files.readAllBytes(qrFile.toPath()));
//			user.setMfaQrImageFilePath(qrFile.getAbsolutePath());
//			user = userRepository.save(user);
//
//			logger.info("Google mfa is enabled for user: {}", userName);
//
//			String templateData = this.templateReader.readTemplate("/enablegooglemfa.ftl");
//			logger.debug("Injecting dynamic data in enable google mfa template");
//			templateData = templateData.replace("${userName}", userName);
//			templateData = templateData.replace("${mfaKey}", mfaKey);
//
//			String subject = "Dear " + userName + ". Google multifactor authentication security enabled";
//
//			MimeMessage mimeMessage = this.mailService.getJavaMailSender().createMimeMessage();
//			MimeMessageHelper helper = this.mailService.createHtmlMailMessageWithImage(mimeMessage, templateData,
//					user.getEmail(), subject);
//			helper.addInline("qrImage", qrFile);
//			this.mailService.sendEmail(mimeMessage);
//			logger.info("Google mfa enabled. Access key sent in mail.");
//			Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google mfa is enabled for user: " + userName, user);
//            return ResponseEntity.status(HttpStatus.OK).body(st);
//		} catch (Exception e) {
//			logger.error("Exception in enabling google mfa: ", e);
//			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Exception in enabling google mfa", null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//		}
//	}

//	@RequestMapping(path = "/disableGoogleMfa")
//	public ResponseEntity<Object> disableGoogleMfa(@RequestParam final String userName,
//			@RequestParam final String organizationName) {
//		logger.info("Request to disable google mfa for user: {}", userName);
//		try {
//
//			User user = userRepository.findByUsername(userName);
//			if (user == null) {
//				logger.error("User not found. User: {}, Organization: {}", userName, organizationName);
//				Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found", null);
//                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//			}
//
//			user.setIsMfaEnable("NO");
//			user.setGoogleMfaKey(null);
//			user.setMfaQrImageFilePath(null);
//			user = userRepository.save(user);
//
//			String fileName = userName + ".png";
//			String filePath = "qrimages/" + organizationName + "/" + fileName;
//			File file = new File(filePath);
//
//			if (file.exists()) {
//				file.delete();
//			}
//
//			logger.info("Google mfa is disable for user: {}", userName);
//			Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google mfa is disabled for user: " + userName, user);
//			return ResponseEntity.status(HttpStatus.OK).body(st);
//		} catch (Exception e) {
//			logger.error("Exception in disabling google mfa: ", e);
//			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Exception in disabling google mfa", null);
//			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//		}
//	}

	@Override
	public ResponseEntity<Object> create(ObjectNode entity, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
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
