/**
 * 
 */
package com.synectiks.security.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.entities.Permission;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.PermissionRepository;
import com.synectiks.security.util.IUtils;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_PERMISSION, method = RequestMethod.POST)
@CrossOrigin
public class PermissionController implements IApiController {

	private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

	@Autowired
	private PermissionRepository repository;

	@Override
	@RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<Permission> entities = null;
		try {
			entities = (List<Permission>) repository.findAll();
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entities);
	}

	@Override
	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestBody ObjectNode service, HttpServletRequest request) {
		Permission entity = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			entity = IUtils.createEntity(service, user, Permission.class);
			entity = repository.save(entity);
		} catch (Throwable th) {
			th.printStackTrace();
			// logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(IConsts.API_FIND_ID)
	public ResponseEntity<Object> findById(@PathVariable("id") Long id) {
		Permission entity = null;
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
		return ResponseEntity.status(HttpStatus.OK).body("Permission removed Successfully");
	}

	@Override
	@RequestMapping(IConsts.API_UPDATE)
	public ResponseEntity<Object> update(@RequestBody ObjectNode entity, HttpServletRequest request) {
		Permission service = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			service = IUtils.createEntity(entity, user, Permission.class);
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
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Not a valid entity");
	}

	@RequestMapping("/createPermissionInBatch")
	public ResponseEntity<Object> createPermissionsInBatch(@RequestBody ObjectNode entity) {
		Iterator<Map.Entry<String, JsonNode>> iter = entity.fields();
		while (iter.hasNext()) {
			Map.Entry<String, JsonNode> entry = iter.next();
//			System.out.println("key=" + entry.getKey() + " Value=" + entry.getValue());
			String parent = entry.getKey();
			JsonNode childObjectList = entry.getValue();
			if (childObjectList.hasNonNull(0)) {
				for (JsonNode jsonNode : childObjectList) {

					Permission permissionObj = new Permission();
					permissionObj.setName(parent);
					String permission = jsonNode.get("permission").asText();
					String description = jsonNode.get("description").asText();
					permissionObj.setPermission(permission);
					permissionObj.setDescription(description);
					System.out.println("Permision object going to persist:::" + permissionObj);
					repository.save(permissionObj);

				}
			}
		}
//		for (JsonNode jsonNode : entity) {
//			System.out.println(jsonNode);
//		}
		List<Permission> entities = null;
		try {
			entities = (List<Permission>) repository.findAll();
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(entities);
	}

	@PostMapping(value = "/createPermissionsByFile")
	public ResponseEntity<Object> createPermissionsByFile(@RequestParam(required = false) File inputFile,
			@RequestParam String str, HttpServletRequest request) throws IOException {
		System.out.println(request.getAttributeNames());
		System.out.println(request.getParameter("str"));
		System.out.println(request.getParameter("inputFile"));
		System.out.println(request.getInputStream().read());
		// if (!inputFile.isEmpty()) {
//			try {
//	            byte[] bytes = inputFile.getBytes();
//	            String completeData = new String(bytes);
//	            ObjectNode json = (ObjectNode) new ObjectMapper().readTree(completeData);
//	            createPermissionsInBatch(json);
//			}catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//		}
		return null;
	}

	
}
