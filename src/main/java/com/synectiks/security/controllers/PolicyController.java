/**
 *
 */
package com.synectiks.security.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.entities.Policy;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.PolicyRepository;
import com.synectiks.security.util.IUtils;
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
			String user = IUtils.getUserFromRequest(request);
			entity = IUtils.createEntity(service, user, Policy.class);
			logger.info("Policy: " + entity);
			entity = repository.save(entity);
		} catch (Throwable th) {
//			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(IConsts.API_FIND_ID)
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
		try {
			String user = IUtils.getUserFromRequest(request);
			service = IUtils.createEntity(entity, user, Policy.class);
			repository.save(service);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(service);
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
