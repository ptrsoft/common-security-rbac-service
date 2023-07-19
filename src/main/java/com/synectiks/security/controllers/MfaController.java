///**
// *
// */
//package com.synectiks.security.controllers;
//
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.synectiks.security.config.Constants;
//import com.synectiks.security.entities.User;
//import com.synectiks.security.interfaces.IApiController;
//import com.synectiks.security.mfa.GoogleMultiFactorAuthenticationService;
//import com.synectiks.security.repositories.UserRepository;
//import com.warrenstrange.googleauth.GoogleAuthenticator;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.authc.AuthenticationInfo;
//import org.apache.shiro.authc.UsernamePasswordToken;
//import org.apache.shiro.authc.credential.DefaultPasswordService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.File;
//import java.nio.file.Files;
//
///**
// * @author Manoj
// */
//@RestController
//@RequestMapping(path = IApiController.SEC_API + IApiController.URL_USER, method = RequestMethod.POST)
//@CrossOrigin
public class MfaController  {
//
//	private static final Logger logger = LoggerFactory.getLogger(MfaController.class);
//
//	private DefaultPasswordService pswdService = new DefaultPasswordService();
//
//	@Autowired
//	private UserRepository userRepository;
//
//	@Autowired
//	private GoogleMultiFactorAuthenticationService googleMultiFactorAuthenticationService;
//
//
//	@RequestMapping(path = "/mfa-code")
//	public ResponseEntity<Object> getMfaCode(@RequestBody ObjectNode reqObj) {
//		logger.info("Request to get google mfa for user: {}", reqObj.get("userName").asText());
//        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//        if (StringUtils.isBlank(reqObj.get("password").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","Password not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//		try {
//            UsernamePasswordToken token = new UsernamePasswordToken();
//            token.setUsername(reqObj.get("userName").asText());
//            token.setPassword(reqObj.get("password").asText().toCharArray());
//            AuthenticationInfo info = SecurityUtils.getSecurityManager().authenticate(token);
//
//			String mfaKey = googleMultiFactorAuthenticationService.getGoogleAuthenticationKey(reqObj.get("userName").asText());
//			int size = 125;
//			String fileType = "png";
//			String keyUri = googleMultiFactorAuthenticationService.generateGoogleAuthenticationUri(reqObj.get("userName").asText(),
//					"Synectiks ", mfaKey);
//
//            File tempFile = File.createTempFile(reqObj.get("userName").asText(), "."+fileType);
//			googleMultiFactorAuthenticationService.createQRImage(tempFile, keyUri, size, fileType);
//			Status st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA QR code created successfully",mfaKey, Files.readAllBytes(tempFile.toPath()));
//            tempFile.deleteOnExit();
//            return ResponseEntity.status(HttpStatus.OK).body(st);
//		} catch (Exception e) {
//            String errMsg = "Exception in getting mfa";
//			logger.error(errMsg+": ", e);
//			Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR",errMsg, null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//		}
//	}
//
//    @RequestMapping(value = "/authenticate-mfa")
//    @ResponseBody
//    public ResponseEntity<Object> authenticateGoogleMfa(@RequestBody ObjectNode reqObj) {
//        logger.error("Request to authenticate google MFA token");
//
//        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//        if (StringUtils.isBlank(reqObj.get("token").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","MFA token not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//
//        try {
//            User user = this.userRepository.findByUsername(reqObj.get("userName").asText());
//            if(user == null){
//                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User name : "+reqObj.get("userName").asText(), null, null);
//                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//            }
//            GoogleAuthenticator gAuth = new GoogleAuthenticator();
//            boolean matches = false;
//            if(!StringUtils.isBlank(user.getIsMfaEnable()) && Constants.YES.equalsIgnoreCase(user.getIsMfaEnable())){
//                matches = gAuth.authorize(user.getGoogleMfaKey(), Integer.parseInt(reqObj.get("token").asText()));
//                if(matches) {
//                    logger.debug("Google mfa authentication successful");
//                }else {
//                    logger.warn("Google mfa authentication failed");
//                }
//            }else{
//                matches = gAuth.authorize(reqObj.get("mfaKey").asText(), Integer.parseInt(reqObj.get("token").asText()));
//                if(matches){
//                    logger.info("Google MFA authentication successful for user {}",reqObj.get("userName").asText());
//                    user.setIsMfaEnable(Constants.YES);
//                    user.setGoogleMfaKey(reqObj.get("mfaKey").asText());
//                    user = userRepository.save(user);
//                    logger.info("Google MFA is enabled for user: {}", reqObj.get("userName").asText());
//                }else{
//                    logger.warn("Google MFA token authentication failed while setting up MFA for user: {}", reqObj.get("userName").asText());
//                }
//
//            }
//            Status st = setMessage(HttpStatus.OK.value(), "SUCCESS",matches == true ? "Google MFA token authenticated" : "Google MFA token authentication failed",null, matches);
//            return ResponseEntity.status(HttpStatus.OK).body(st);
//        }catch(Exception e) {
//            logger.error("Exception in MFA authentication: ",e);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
//        }
//    }
//
//    @RequestMapping(value = "/disable-mfa")
//    @ResponseBody
//    public ResponseEntity<Object> disableGoogleMfa(@RequestBody ObjectNode reqObj) {
//        logger.error("Request to disable google MFA");
//
//        if (StringUtils.isBlank(reqObj.get("userName").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User name not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//        if (StringUtils.isBlank(reqObj.get("token").asText())) {
//            Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","MFA token not provided", null, null);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//        }
//
//        try {
//            User user = this.userRepository.findByUsername(reqObj.get("userName").asText());
//            if(user == null){
//                Status st = setMessage(HttpStatus.EXPECTATION_FAILED.value(), "ERROR","User not found. User name : "+reqObj.get("userName").asText(), null, null);
//                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(st);
//            }
//            GoogleAuthenticator gAuth = new GoogleAuthenticator();
//            boolean matches = false;
//            Status st = null;
//            if(!StringUtils.isBlank(user.getIsMfaEnable()) && Constants.YES.equalsIgnoreCase(user.getIsMfaEnable())){
//                matches = gAuth.authorize(user.getGoogleMfaKey(), Integer.parseInt(reqObj.get("token").asText()));
//                if(matches) {
//                    logger.info("Google MFA authentication successful. Disabling MFA for user {}",reqObj.get("userName").asText());
//                    user.setIsMfaEnable(null);
//                    user.setGoogleMfaKey(null);
//                    user = userRepository.save(user);
//                    logger.info("Google MFA is disabled for user: {}", reqObj.get("userName").asText());
//                    st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA token disabled",null, matches);
//                }else {
//                    logger.warn("Google mfa authentication failed");
//                    st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA token authentication failed. MFA could not be disabled",null, matches);
//                }
//            }else{
//                logger.info("Google MFA is already disabled for user: {}", reqObj.get("userName").asText());
//                st = setMessage(HttpStatus.OK.value(), "SUCCESS","Google MFA is already disabled",null, null);
//            }
//            return ResponseEntity.status(HttpStatus.OK).body(st);
//        }catch(Exception e) {
//            logger.error("Exception while disabling MFA token: ",e);
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
//        }
//    }
//
//    private Status setMessage(int statusCode, String statusType, String msg, String mfaKey, Object obj){
//        Status st = new Status();
//        st.setCode(statusCode);
//        st.setType(statusType);
//        st.setMessage(msg);
//        st.setMfaKey(mfaKey);
//        st.setObject(obj);
//        return st;
//    }
//
//    class Status {
//        public Status (){}
//        private int code;
//        private String type;
//        private String message;
//        private String mfaKey;
//        private Object object;
//
//        public int getCode() {
//            return code;
//        }
//        public void setCode(int code) {
//            this.code = code;
//        }
//        public String getType() {
//            return type;
//        }
//        public void setType(String type) {
//            this.type = type;
//        }
//        public String getMessage() {
//            return message;
//        }
//        public void setMessage(String message) {
//            this.message = message;
//        }
//
//        public String getMfaKey() {
//            return mfaKey;
//        }
//        public void setMfaKey(String mfaKey) {
//            this.mfaKey = mfaKey;
//        }
//
//        public Object getObject() {
//            return object;
//        }
//        public void setObject(Object object) {
//            this.object = object;
//        }
//
//
//    }
}
