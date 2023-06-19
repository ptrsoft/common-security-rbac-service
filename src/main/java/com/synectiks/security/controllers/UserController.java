/**
 *
 */
package com.synectiks.security.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.synectiks.security.config.Constants;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.email.MailService;
import com.synectiks.security.entities.Document;
import com.synectiks.security.entities.Organization;
import com.synectiks.security.entities.Role;
import com.synectiks.security.entities.Status;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.mfa.GoogleMultiFactorAuthenticationService;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.RoleRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.service.DocumentService;
import com.synectiks.security.util.IUtils;
import com.synectiks.security.util.RandomGenerator;
import com.synectiks.security.util.TemplateReader;

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
					byte[] bytes = Files.readAllBytes(Paths.get(doc.getLocalFilePath()));
					by.setProfileImage(bytes);
				}
				break;
			}
		}

		
	}

	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestParam("type") String type,@RequestParam("organization") String organization, @RequestParam("username") String username,@RequestParam("password") String password,@RequestParam("email") String email,
			@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException {
//		ObjectMapper mapper = new ObjectMapper();
//		ObjectNode json = (ObjectNode) mapper.readTree(obj);
		JsonObject obj=new JsonObject();
		obj.addProperty("username", username);
		obj.addProperty("type", type);
		obj.addProperty("password", password);
		obj.addProperty("email", email);
		obj.addProperty("organization",organization);
		User user = this.userRepository.findByUsername(obj.get("username").toString());
		if (user != null) {
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Login id already exists");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		// check for duplicate email
		user = new User();
		user.setEmail(obj.get("email").toString());
		Optional<User> oUser = this.userRepository.findOne(Example.of(user));
		if (oUser.isPresent()) {
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Email already exists");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		user = new User();
		try {
			String signedInUser = IUtils.getUserFromRequest(request);
//			user = IUtils.createEntity(service, signedInUser, User.class);
			createUserFromJson(user, obj);
			// Encrypt the password
			if (!IUtils.isNullOrEmpty(user.getPassword()) && !user.getPassword().startsWith("$shiro1$")) {
				user.setPassword(pswdService.encryptPassword(user.getPassword()));
			}
			Date currentDate = new Date();

			saveOrUpdateOrganization(obj, user, currentDate);

			user.setCreatedAt(currentDate);
			user.setUpdatedAt(currentDate);

			if (obj.get("username") != null) {
				user.setCreatedBy(obj.get("username").toString());
				user.setUpdatedBy(obj.get("username").toString());
			} else {
				user.setCreatedBy(Constants.SYSTEM_ACCOUNT);
				user.setUpdatedBy(Constants.SYSTEM_ACCOUNT);
			}

			logger.info("Saving user: " + user);
			user = userRepository.save(user);
			addProfileImage(file, user, obj, currentDate);
			getDocumentList(user);
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Service issues. User data cannot be saved.");
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

	private void addProfileImage(MultipartFile file, User user, JsonObject json, Date currentDate) throws IOException {
		if (file != null) {
			byte[] bytes = file.getBytes();
			String orgFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
			String ext = "";
			if (orgFileName.lastIndexOf(".") != -1) {
				ext = orgFileName.substring(orgFileName.lastIndexOf(".") + 1);
			}
			String filename = "";
			if (json.get("name") != null) {
				filename = json.get("name").toString();
			}
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
						Status st = new Status();
						st.setCode(HttpStatus.EXPECTATION_FAILED.value());
						st.setType("ERROR");
						st.setMessage("Email id already exists");
						return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
					}
				}
			} catch (IncorrectResultSizeDataAccessException e) {
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("Email id already exists");
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
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}

	private void saveOrUpdateOrganization(JsonObject  reqObje, User user, Date currentDate) throws URISyntaxException {
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

	private void createUserFromJson(User user, JsonObject reqObj) {
		if (reqObj.get("type") != null) {
			user.setType(reqObj.get("type").toString());
		}
		if (reqObj.get("username") != null) {
			user.setUsername(reqObj.get("username").toString());
		}
		if (reqObj.get("password") != null) {
			user.setPassword(reqObj.get("password").toString());
		}
		if (reqObj.get("active") != null) {
			user.setActive(reqObj.get("active").getAsBoolean());
		}
		if (reqObj.get("email") != null) {
			user.setEmail(reqObj.get("email").toString());
		}

//		user.setOwnerId(reqObj.get("ownerId") != null ? reqObj.get("ownerId").asLong() : null);
		if (reqObj.get("organization") != null) {
			Organization organization = new Organization();
			organization.setName(reqObj.get("organization").toString().toUpperCase());
			Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
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
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage(inviteeEmail + " already exists. Please choose a different user id");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}

		try {
			user = new User();
			user.setEmail(inviteeEmail);
			user.setActive(true);
			Optional<User> oUser = this.userRepository.findOne(Example.of(user));
			if (oUser.isPresent()) {
				logger.warn("Another user with email id: " + inviteeEmail + " already exists");
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("Email id: " + inviteeEmail + " already exists");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
			user.setActive(false);
			oUser = this.userRepository.findOne(Example.of(user));
			if (oUser.isPresent()) {
				logger.warn("Another user with email id: " + inviteeEmail + " already exists");
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("Email id: " + inviteeEmail + " already exists");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}
		} catch (Exception e) {
			logger.warn("Email id: " + inviteeEmail + " already exists", e.getMessage());
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Email already exists");
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
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("Owner not found. Please check owner's email id");
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

			Status st = new Status();
			st.setCode(HttpStatus.OK.value());
			st.setType("SUCCESS");
			st.setMessage("Invitation link sent to user's email: " + inviteeEmail);
			st.setObject(invitee);
			return ResponseEntity.status(HttpStatus.OK).body(st);

		} catch (Exception e) {
			logger.error("User invite failed. Exception: ", e);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("User invite failed");
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
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("User authentication failed. Invitation cannot be accepted");
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

			Status st = new Status();
			st.setCode(HttpStatus.OK.value());
			st.setType("SUCCESS");
			st.setMessage("Invitation accepted. Login id and password sent to user's email: " + invitee.getEmail());
			st.setObject(invitee);

			return ResponseEntity.status(HttpStatus.OK).body(st);

		} catch (Exception e) {
			logger.error("User invite acceptance failed. Exception: ", e);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("User invite acceptance failed");
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
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Due to some error, team list cannot be retrieved");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
		Status st = new Status();
		st.setCode(HttpStatus.OK.value());
		st.setType("SUCCESS");
		st.setMessage("Team list");
		st.setObject(user);
		return ResponseEntity.status(HttpStatus.OK).body(st);
	}

	@RequestMapping(path = "/enableGoogleMfa")
	public ResponseEntity<Object> enableGoogleMfa(@RequestParam final String userName,
			@RequestParam final String organizationName) {
		logger.info("Request to enable google mfa for user: {}", userName);
		try {
			User user = userRepository.findByUsername(userName);
			if (user == null) {
				logger.error("User not found. User: {}, Organization: {}", userName, organizationName);
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("User not found");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}

			user.setIsMfaEnable("YES");
			String mfaKey = googleMultiFactorAuthenticationService.getGoogleAuthenticationKey(userName);
			user.setGoogleMfaKey(mfaKey);

			String directory = "qrimages/" + organizationName;
			File dir = new File(directory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String fileName = userName + ".png";
			String filePath = directory + "/" + fileName;
			int size = 125;
			String fileType = "png";
			File qrFile = new File(filePath);
			String keyUri = googleMultiFactorAuthenticationService.generateGoogleAuthenticationUri(userName,
					"Synectiks ", mfaKey);
			googleMultiFactorAuthenticationService.createQRImage(qrFile, keyUri, size, fileType);

			user.setMfaQrCode(Files.readAllBytes(qrFile.toPath()));
			user.setMfaQrImageFilePath(qrFile.getAbsolutePath());
			user = userRepository.save(user);

			logger.info("Google mfa is enabled for user: {}", userName);

			String templateData = this.templateReader.readTemplate("/enablegooglemfa.ftl");
			logger.debug("Injecting dynamic data in enable google mfa template");
			templateData = templateData.replace("${userName}", userName);
			templateData = templateData.replace("${mfaKey}", mfaKey);

			String subject = "Dear " + userName + ". Google multifactor authentication security enabled";

			MimeMessage mimeMessage = this.mailService.getJavaMailSender().createMimeMessage();
			MimeMessageHelper helper = this.mailService.createHtmlMailMessageWithImage(mimeMessage, templateData,
					user.getEmail(), subject);
			helper.addInline("qrImage", qrFile);
			this.mailService.sendEmail(mimeMessage);
			logger.info("Google mfa enabled. Access key sent in mail.");

			Status st = new Status();
			st.setCode(HttpStatus.OK.value());
			st.setType("SUCCESS");
			st.setMessage("Google mfa is enabled for user: " + userName);
			st.setObject(user);
			return ResponseEntity.status(HttpStatus.OK).body(st);
		} catch (Exception e) {
			logger.error("Exception in enabling google mfa: ", e);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Exception in enabling google mfa");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
	}

	@RequestMapping(path = "/disableGoogleMfa")
	public ResponseEntity<Object> disableGoogleMfa(@RequestParam final String userName,
			@RequestParam final String organizationName) {
		logger.info("Request to disable google mfa for user: {}", userName);
		try {

			User user = userRepository.findByUsername(userName);
			if (user == null) {
				logger.error("User not found. User: {}, Organization: {}", userName, organizationName);
				Status st = new Status();
				st.setCode(HttpStatus.EXPECTATION_FAILED.value());
				st.setType("ERROR");
				st.setMessage("User not found");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
			}

			user.setIsMfaEnable("NO");
			user.setGoogleMfaKey(null);
			user.setMfaQrImageFilePath(null);
			user = userRepository.save(user);

			String fileName = userName + ".png";
			String filePath = "qrimages/" + organizationName + "/" + fileName;
			File file = new File(filePath);

			if (file.exists()) {
				file.delete();
			}

			logger.info("Google mfa is disable for user: {}", userName);
			Status st = new Status();
			st.setCode(HttpStatus.OK.value());
			st.setType("SUCCESS");
			st.setMessage("Google mfa is disable for user: " + userName);
			st.setObject(user);
			return ResponseEntity.status(HttpStatus.OK).body(st);
		} catch (Exception e) {
			logger.error("Exception in disabling google mfa: ", e);
			Status st = new Status();
			st.setCode(HttpStatus.EXPECTATION_FAILED.value());
			st.setType("ERROR");
			st.setMessage("Exception in disabling google mfa");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
		}
	}

	@Override
	public ResponseEntity<Object> create(ObjectNode entity, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
