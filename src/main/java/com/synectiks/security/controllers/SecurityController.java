/**
 * 
 */
package com.synectiks.security.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.security.config.Constants;
import com.synectiks.security.entities.Organization;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.mfa.GoogleMultiFactorAuthenticationService;
import com.synectiks.security.models.AuthInfo;
import com.synectiks.security.models.LoginRequest;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.util.IUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.PUB_API, method = RequestMethod.POST)
@CrossOrigin
public class SecurityController {

	private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
	private static final String SUC_MSG = "{\"message\": \"SUCCESS\"}";

	private DefaultPasswordService pswdService = new DefaultPasswordService();
	
	@Autowired
	private UserRepository users;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	GoogleMultiFactorAuthenticationService googleMultiFactorAuthenticationService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ResponseEntity<Object> login(@RequestParam  String username, @RequestParam String password,
			@RequestParam(required = false) boolean rememberMe) {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(username);
		token.setPassword(password.toCharArray());
		token.setRememberMe(rememberMe);
		return authenticate(token);
	}

	@RequestMapping(value = "/signup")
	public String login(@RequestBody LoginRequest request) {
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

	@RequestMapping(value = "/singin")
	public ResponseEntity<Object> login(@RequestBody User user) {
		UsernamePasswordToken token = new UsernamePasswordToken();
		token.setUsername(user.getUsername());
		token.setPassword(user.getPassword().toCharArray());
		return authenticate(token);
	}

	@RequestMapping(value = "/authenticate")
	@ResponseBody
    public ResponseEntity<Object> authenticate(@RequestBody final UsernamePasswordToken token) {
        logger.info("Authenticating {}", token.getUsername());
        final Subject subject = SecurityUtils.getSubject();
        String res = null;
        AuthInfo authInfo;
		try {
            subject.login(token);
            AuthenticationInfo info = SecurityUtils.getSecurityManager().authenticate(token);
            User usr = users.findByUsername(token.getUsername());
            if(StringUtils.isBlank(usr.getIsMfaEnable())) {
            	usr.setIsMfaEnable(Constants.NO);
            }
            authInfo = AuthInfo.create(info, usr);
            res = IUtils.getStringFromValue(authInfo);
            logger.info(res);
            //if no exception, that's it, we're done!
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
        }
        return ResponseEntity.status(HttpStatus.OK).body(authInfo);
    }

	@RequestMapping(value = "/authenticateUser")
	@ResponseBody
    public ResponseEntity<Object> authenticateUser(@RequestParam final String userName) {
        logger.info("Authenticating user: {}", userName);
        User usr = null;
        try {
            usr = users.findByUsername(userName);
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
	public String logout() {
		SecurityUtils.getSubject().logout();
		return SUC_MSG;
	}

	
	@RequestMapping(value = "/importUser")
	public ResponseEntity<Object> importUser(@RequestBody List<String> list) {
		
		try {
			List<User> existingUsers = (List<User>) users.findAll();
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
				entity.setPassword(pswdService.encryptPassword("welcome"));
//				entity.setCreatedAt(new Date(System.currentTimeMillis()));
				entity.setCreatedBy("APPLICATION");
				newUsers.add(entity);
				
			}
			users.saveAll(newUsers);
			logger.info("All users successfully saved in security db" );
			
		} catch (Throwable th) {
//			th.printStackTrace();
			logger.error("Exception in importUser: ", th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("SUCCESS");
	}
	
	
	@RequestMapping(value = "/authenticateGoogleMfa")
	@ResponseBody
    public ResponseEntity<Object> authenticateGoogleMfa(@RequestParam final String userName,
    		@RequestParam final String organizationName, @RequestParam final String mfaCode) {
		logger.error("Request to authenticate google mfa token");
		try {
			User user = new User();
			user.setUsername(userName);
			user.setActive(true);
			
			Organization organization = new Organization();
			organization.setName(organizationName);
			Optional<Organization> oOrg = this.organizationRepository.findOne(Example.of(organization));
			if(!oOrg.isPresent()) {
				logger.error("Organization not found. Organization: {}", organizationName);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
			}
			user.setOrganization(oOrg.get());
			
			Optional<User> oUser = users.findOne(Example.of(user));
			if(!oUser.isPresent()) {
				logger.error("User not found. User: {}, Organization: {}", userName, organizationName);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
			}
			user = oUser.get();
			
			
			GoogleAuthenticator gAuth = new GoogleAuthenticator(); 
			boolean matches = gAuth.authorize(user.getGoogleMfaKey(), Integer.parseInt(mfaCode));
			if(matches) {
				logger.info("Google mfa token authentication success");
			}else {
				logger.info("Google mfa token authentication failed");
			}
				
			return ResponseEntity.status(HttpStatus.OK).body(matches);
		}catch(Exception e) {
			logger.error("Exception in authenticateGoogleMfa: ",e);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
		}
		
		
    }
	
}
