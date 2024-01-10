/**
 *
 */
package com.synectiks.security.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.Constants;
import com.synectiks.security.entities.Config;
import com.synectiks.security.entities.Organization;
import com.synectiks.security.entities.Status;
import com.synectiks.security.service.AppkubeAwsEmailService;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.mfa.GoogleMultiFactorAuthenticationService;
import com.synectiks.security.models.AuthInfo;
import com.synectiks.security.models.LoginRequest;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.service.ConfigService;
import com.synectiks.security.service.OrganizationService;
import com.synectiks.security.util.IUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.PUB_API, method = RequestMethod.POST)
@CrossOrigin
public class SecurityController {

	private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
	private static final String SUC_MSG = "{\"message\": \"SUCCESS\"}";

	private DefaultPasswordService passwordService = new DefaultPasswordService();

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	GoogleMultiFactorAuthenticationService googleMultiFactorAuthenticationService;
//	@Autowired
//	private DocumentService documentService;

    @Autowired
    private DefaultWebSecurityManager defaultWebSecurityManager;

    @Autowired
    private AppkubeAwsEmailService appkubeAwsEmailService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private OrganizationService organizationService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
	public ResponseEntity<Object> login(@RequestParam  String username, @RequestParam String password,
			@RequestParam(required = false) boolean rememberMe) throws IOException {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(username);
		token.setPassword(password.toCharArray());
		token.setRememberMe(rememberMe);

		return authenticate(token);
	}
//	private void getDocumentList(User usr) {
//		Map<String, String> requestObj = new HashMap<>();
//		requestObj.put("sourceId", String.valueOf(usr.getId()));
//		List<Document> docList = documentService.searchDocument(requestObj);
//		List<Document> finalDocList = new ArrayList<>();
//		for (Document doc : docList) {
//			if (!doc.getIdentifier().equalsIgnoreCase(Constants.IDENTIFIER_PROFILE_IMAGE)) {
//				finalDocList.add(doc);
//			}
//		}
//		usr.setDocumentList(finalDocList);
//
//	}
//	private void setProfileImage(User usr) throws IOException {
//		Map<String, String> requestObj = new HashMap<>();
//		requestObj.put("sourceId", String.valueOf(usr.getId()));
//		List<Document> docList = documentService.searchDocument(requestObj);
//		for (Document doc : docList) {
//			if (doc.getIdentifier().equalsIgnoreCase(Constants.IDENTIFIER_PROFILE_IMAGE)) {
//				if (doc.getLocalFilePath() != null) {
//					byte[] bytes = Files.readAllBytes(Paths.get(doc.getLocalFilePath()));
//					usr.setProfileImage(bytes);
//				}
//				break;
//			}
//		}
//
//
//	}


	@RequestMapping(value = "/signup")
	public String signup(@RequestBody LoginRequest request) throws IOException {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(request.getUsername());
		token.setPassword(request.getPassword().toCharArray());
		token.setRememberMe(request.isRememberMe());
		ResponseEntity<Object> res = authenticate(token);
		if (!IUtils.isNullOrEmpty(request.getRedirectTo())) {
			return request.getRedirectTo();
		}
		return res.toString();
	}

	@RequestMapping(value = "/signin")
	public ResponseEntity<Object> signin(@RequestBody User user) throws IOException {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(user.getUsername());
		token.setPassword(user.getPassword().toCharArray());
		return authenticate(token);
	}

	@RequestMapping(value = "/authenticate")
	@ResponseBody
    public ResponseEntity<Object> authenticate(@RequestBody final UsernamePasswordToken token) throws IOException {
        logger.info("Authenticating {}", token.getUsername());
        Map<Object, Object> resourceMap = ThreadContext.getResources();
        deleteExistingUserSession(token.getUsername(), resourceMap);
        Subject subject = (Subject)ThreadContext.get(token.getUsername());
        if (subject == null) {
            subject = (new Subject.Builder()).buildSubject();
            ThreadContext.put(token.getUsername(), subject);
        }

        String res = null;
        AuthInfo authInfo;
		try {
            AuthenticationInfo info = SecurityUtils.getSecurityManager().authenticate(token);
            subject.login(token);

            Session session = subject.getSession();
            logger.info("Users session id: {}",session.getId());
            subject.getSession().setAttribute(token.getUsername(), session.getId());

            User usr = userRepository.findByUsername(token.getUsername());
            User updateUser = (User)BeanUtils.cloneBean(usr);
            if(StringUtils.isBlank(usr.getIsMfaEnable())) {
            	usr.setIsMfaEnable(Constants.NO);
            }
//            getDocumentList(usr);
//			setProfileImage(usr);
            authInfo = AuthInfo.create(info, usr, passwordService.encryptPassword(session.getId().toString()));
            res = IUtils.getStringFromValue(authInfo);
            logger.info(res);
            //if no exception, that's it, we're done!
            logger.debug("Updating users login date-time");
            updateUser.setLastLoginAt(new Date());
            updateUser.setLoginCount(updateUser.getLoginCount() == null ? 1 : updateUser.getLoginCount()+1);
            userRepository.save(updateUser);
        } catch (UnknownAccountException th) {
            //username wasn't in the system, show them an error message?
			logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (IncorrectCredentialsException th) {
            //password didn't match, try again?
			logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (LockedAccountException th) {
            //account for that username is locked - can't login.  Show them a message?
			logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (AuthenticationException th) {
        	// General exception thrown due to an error during the Authentication process
			logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (InvocationTargetException|IllegalAccessException|InstantiationException|NoSuchMethodException th) {
            logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        }

        return ResponseEntity.status(HttpStatus.OK).body(authInfo);
    }

	@RequestMapping(value = "/authenticateUser")
	@ResponseBody
    public ResponseEntity<Object> authenticateUser(@RequestParam final String userName) {
        logger.info("Authenticating user: {}", userName);
        User usr = null;
        try {
            usr = userRepository.findByUsername(userName);
            if(usr == null) {
            	logger.error("User not found");
    			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("User not found");
            }
            usr.setPassword(null);
            usr.setRoles(null);
            if(StringUtils.isBlank(usr.getIsMfaEnable())) {
            	usr.setIsMfaEnable(Constants.NO);
            }
        } catch (UnknownAccountException th) {
            //username wasn't in the system, show them an error message?
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (LockedAccountException th) {
            //account for that username is locked - can't login.  Show them a message?
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        } catch (AuthenticationException th) {
        	// General exception thrown due to an error during the Authentication process
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
        }
        return ResponseEntity.status(HttpStatus.OK).body(usr);
    }

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ResponseEntity<Object> logout(@RequestParam final String userName) {
        Map<Object, Object> resourceMap = ThreadContext.getResources();
        Session userSession = deleteExistingUserSession(userName, resourceMap);
        ThreadContext.setResources(resourceMap);
        if(userSession != null){
            Status st = new Status(HttpStatus.OK.value(), "User: "+userName+ " logged out successfully", true);
            return ResponseEntity.status(HttpStatus.OK).body(st);
        }
        Status st = new Status(601, "Session already expired", false);
        return ResponseEntity.status(HttpStatus.OK).body(st);
	}

    private Session deleteExistingUserSession(String userName, Map<Object, Object> resourceMap) {
        Session userSession = null;
        for(Map.Entry<Object,Object> entry : resourceMap.entrySet()){
            if(!StringUtils.isBlank((String)entry.getKey())
                && ((String)entry.getKey()).contains("_SECURITY_MANAGER_KEY")){
                DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) entry.getValue();
                DefaultWebSessionManager sessionManager = (DefaultWebSessionManager)securityManager.getSessionManager();
                MemorySessionDAO sessionDAO = (MemorySessionDAO)sessionManager.getSessionDAO();
                for(Session session: sessionDAO.getActiveSessions()){
                    if(session.getAttribute(userName) != null
                        && session.getId().equals((Serializable) session.getAttribute(userName))){
                        session.removeAttribute(userName);
                        sessionDAO.delete(session);
                        sessionManager.setSessionDAO(sessionDAO);
                        securityManager.setSessionManager(sessionManager);
                        resourceMap.put(entry.getKey(),securityManager);
                        userSession = session;
                        break;
                    }
                }
            }
        }
        return userSession;
    }


    @RequestMapping(value = "/importUser")
	public ResponseEntity<Object> importUser(@RequestBody List<String> list) {

		try {
			List<User> existingUsers = (List<User>) userRepository.findAll();
			for(User user: existingUsers) {
				if(list.contains(user.getEmail())) {
					list.remove(user.getEmail());
				}
			}
			List<User> newUsers = new ArrayList<User>();

			for(String userId: list) {
				User entity = new User();
				entity.setEmail(userId);
				entity.setUsername(userId);
				entity.setActive(true);
				entity.setPassword(passwordService.encryptPassword("welcome"));
//				entity.setCreatedAt(new Date(System.currentTimeMillis()));
				entity.setCreatedBy("APPLICATION");
				newUsers.add(entity);

			}
			userRepository.saveAll(newUsers);
			logger.info("All users successfully saved in security db" );

		} catch (Throwable th) {
//			th.printStackTrace();
			logger.error("Exception in importUser: ", th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("SUCCESS");
	}

    @RequestMapping(value = "/authenticateSession", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> authenticateUserSession(@RequestBody ObjectNode reqObj) {
        logger.error("Request to authenticate user session");

        if (reqObj.get("userName") == null || (reqObj.get("userName") != null && StringUtils.isBlank(reqObj.get("userName").asText()))) {
            Status st = new Status(HttpStatus.PRECONDITION_REQUIRED.value(),"User name required", false);
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(st);
        }
        Map<Object, Object> resourceMap = ThreadContext.getResources();
        boolean isAuthenticated = false;
        for(Map.Entry<Object,Object> entry : resourceMap.entrySet()){
            if(!StringUtils.isBlank((String)entry.getKey())
            && ((String)entry.getKey()).contains("_SECURITY_MANAGER_KEY")){
                DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) entry.getValue();
                Collection<Session> activeSession = ((DefaultWebSessionManager)securityManager.getSessionManager()).getSessionDAO().getActiveSessions();
                for(Session session: activeSession){
                    if(session.getAttribute(reqObj.get("userName").asText()) != null
                        && session.getId().equals((Serializable) session.getAttribute(reqObj.get("userName").asText()))){
                        isAuthenticated = true;
                        break;
                    }
                }
            }
        }
        if(isAuthenticated){
            Status st = new Status(HttpStatus.OK.value(), "Session found", true);
            return ResponseEntity.status(HttpStatus.OK).body(st);
        }
        Status st = new Status(600, "Session not found", false);
        return ResponseEntity.status(HttpStatus.OK).body(st);

    }

    @RequestMapping(path = "/forgot-password", method = RequestMethod.GET)
    public ResponseEntity<Object> sendForgotPasswordMail(@RequestParam String userName) {
        logger.info("Request to send forgot password mail");
        Organization organization = organizationService.getOrganizationByName(Constants.DEFAULT_ORGANIZATION);
        Config configEmailFrom = configService.findByKeyAndOrganizationId(Constants.GLOBAL_APPKUBE_EMAIL_SENDER, organization.getId());
        User user = this.userRepository.findByUsername(userName);
        if (user == null) {
            Status st = new Status(HttpStatus.EXPECTATION_FAILED.value(),"User not found. User name : "+userName, false);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
        }
        if (StringUtils.isBlank(user.getEmail())) {
            Status st = new Status(HttpStatus.valueOf(418).value(),"User's email not found. User Name :"+userName, false);
            return ResponseEntity.status(HttpStatus.valueOf(418)).body(st);
        }
        SendEmailResponse status = appkubeAwsEmailService.sendForgotPasswordMail(user, configEmailFrom);
        Status st = new Status(HttpStatus.OK.value(), "Mail sent", true);
        return ResponseEntity.status(HttpStatus.OK).body(st);
    }

    private class Status{
        private int code;
        private String message;
        private boolean isAuthenticated;

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

        public boolean isAuthenticated() {
            return isAuthenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            isAuthenticated = authenticated;
        }

        public Status(int code, String message, boolean isAuthenticated){
            this.code = code;
            this.message = message;
            this.isAuthenticated = isAuthenticated;
        }
    }
}
