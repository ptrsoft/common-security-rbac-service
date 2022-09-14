/**
 * 
 */
package com.synectiks.security.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.entities.Role;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.RoleRepository;
import com.synectiks.security.util.IUtils;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.SEC_API
		+ IApiController.URL_ROLES, method = RequestMethod.POST)
@CrossOrigin
public class RoleController implements IApiController {

	private static final Logger logger = LoggerFactory
			.getLogger(RoleController.class);

	@Autowired
	private RoleRepository repository;

	@Override
	@RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<Role> entities = null;
		try {
			entities = (List<Role>) repository.findAll();
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entities);
	}

	@Override
	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestBody ObjectNode service,
			HttpServletRequest request) {
		Role entity = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			entity = IUtils.createEntity(service, user, Role.class);
			logger.info("Role: " + entity);
			entity = repository.save(entity);
		} catch (Throwable th) {
			th.printStackTrace();
			//logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(IConsts.API_FIND_ID)
	public ResponseEntity<Object> findById(@PathVariable("id") Long id) {
		Role entity = null;
		try {
			entity = repository.findById(id).orElse(null);
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
			repository.deleteById(id);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body("Role removed Successfully");
	}

	@Override
	@RequestMapping(IConsts.API_UPDATE)
	public ResponseEntity<Object> update(@RequestBody ObjectNode entity,
			HttpServletRequest request) {
		Role service = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			service = IUtils.createEntity(entity, user, Role.class);
			repository.save(service);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(service);
	}

	@Override
	@RequestMapping(IConsts.API_DELETE)
	public ResponseEntity<Object> delete(@RequestBody ObjectNode entity) {
		if (!IUtils.isNull(entity.get(IDBConsts.Col_ID))) {
			return deleteById(entity.get(IDBConsts.Col_ID).asLong());
		}
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
				.body("Not a valid entity");
	}

	
}
