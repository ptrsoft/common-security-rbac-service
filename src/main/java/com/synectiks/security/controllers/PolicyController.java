/**
 *
 */
package com.synectiks.security.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.Constants;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.entities.Policy;
import com.synectiks.security.entities.PolicyAssignedPermissions;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.PolicyAssignedPermissionsRepository;
import com.synectiks.security.repositories.PolicyRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.util.IUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Manoj
 */
@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_POLICY, method = RequestMethod.POST)
@CrossOrigin
public class PolicyController implements IApiController {

	private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);

	@Autowired
	private PolicyRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyAssignedPermissionsRepository policyAssignedPermissionsRepository;

	@Override
	@RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<Policy> entities = null;
		try {
			entities = (List<Policy>) repository.findAll();
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(entities);
	}

	@Override
	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestBody ObjectNode service,
			HttpServletRequest request) {
		Policy entity = null;
		try {
			String userName = IUtils.getUserFromRequest(request);
			entity = IUtils.createEntity(service, userName, Policy.class);
			logger.info("Policy: " + entity);
            if(StringUtils.isBlank(entity.getStatus())){
                entity.setStatus(Constants.ACTIVE);
            }else {
                entity.setStatus(entity.getStatus().toUpperCase());
            }
            // get assigned permission array in a variable
            List<PolicyAssignedPermissions> assignedPermissions = entity.getPermissions();
            // set assigned permission array null in policy
            entity.setPermissions(null);
            // save policy
            User user = userRepository.findByUsername(entity.getCreatedBy());
            entity.setOrganization(user.getOrganization());
            entity = repository.save(entity);
            // iterate assigned permission array and set policy in each object and save
            for(PolicyAssignedPermissions policyAssignedPermissions : assignedPermissions){
                policyAssignedPermissions.setPolicyId(entity.getId());
                policyAssignedPermissions = policyAssignedPermissionsRepository.save(policyAssignedPermissions);
            }
            // keep saved assigned permission references in an array
            // assign this array in policy
            entity.setPermissions(assignedPermissions);
            // update policy
            entity = repository.save(entity);

		} catch (Throwable th) {
//			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(path = IConsts.API_FIND_ID, method = RequestMethod.GET)
	public ResponseEntity<Object> findById(@PathVariable("id") Long id) {
        Policy entity = null;
		try {
			entity = repository.findById(id).orElse(null);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(entity);
	}

	@Override
	@RequestMapping(IConsts.API_DELETE_ID)
	public ResponseEntity<Object> deleteById(@PathVariable("id") Long id) {
		try {
			repository.deleteById(id);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body("Policy removed Successfully");
	}

	@Override
	@RequestMapping(IConsts.API_UPDATE)
	public ResponseEntity<Object> update(@RequestBody ObjectNode entity,
			HttpServletRequest request) {
        Policy service = null;
        Policy existingPolicy = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			service = IUtils.createEntity(entity, user, Policy.class);
            existingPolicy = repository.findById(service.getId()).orElse(null);
            if(existingPolicy == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("policy not found. policy id: "+service.getId());
            }
            if(!StringUtils.isBlank(service.getName())){
                existingPolicy.setName(service.getName());
            }
            if(!StringUtils.isBlank(service.getDescription())){
                existingPolicy.setDescription(service.getDescription());
            }
            if(!StringUtils.isBlank(service.getStatus())){
                existingPolicy.setStatus(service.getStatus().toUpperCase());
            }
			repository.save(existingPolicy);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(th.getStackTrace());
		}
		return ResponseEntity.status(HttpStatus.OK).body(existingPolicy);
	}

	@Override
	@RequestMapping(IConsts.API_DELETE)
	public ResponseEntity<Object> delete(@RequestBody ObjectNode entity) {
		if (!IUtils.isNull(entity.get(IDBConsts.Col_ID))) {
			return deleteById(entity.get(IDBConsts.Col_ID).asLong());
		}
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Not a valid entity");
	}


}
