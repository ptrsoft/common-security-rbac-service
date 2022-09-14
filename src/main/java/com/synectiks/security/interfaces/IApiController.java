/**
 * 
 */
package com.synectiks.security.interfaces;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.domain.Entity;
import com.synectiks.security.domain.SurveyEntity;
import com.synectiks.security.util.IUtils;

/**
 * @author Rajesh
 */
public interface IApiController {

	String API_PATH = "";
	String AUTH_API = API_PATH + "/auth";
	String SEC_API = API_PATH + "/security";
	String PUB_API = SEC_API + "/public";
	String SSM_API = API_PATH + "/ssm";
	String PLC_API = API_PATH + "/policy";
	String RULE_API = API_PATH + "/rule";
	String QRY_API = API_PATH + "/query";
	String CLOUD_ENTITY = API_PATH + "/cloudEntity";
	String CLOUD_API = API_PATH + "/cloudAcc";
	String CUSTOMER_API = API_PATH + "/customer";
	
	String URL_ACTION = "/action";
	String URL_ACTIVITY = "/activity";
	String URL_MIGRATION = "/migration";
	String URL_OAKREPO = "/oakRepo";
	String URL_PAYMENT = "/payment";
	String URL_PERMISSION = "/permissions";
	String URL_RATING = "/rating";
	String URL_RECOM = "/recom";
	String URL_ROLES = "/roles";
	String URL_RESOURCE = "/resource";
	String URL_SEARCH = "/search";
	String URL_SERVICE = "/service";
	String URL_SSM = "/states";
	String URL_SUBSCRIPTION = "/subscription";
	String URL_USER = "/users";
	String URL_ORGANIZATION = "/organization";
	// String URL_ = "";

	/**
	 * List all the entities from repository
	 * @param request
	 * @return
	 */
	ResponseEntity<Object> findAll(HttpServletRequest request);

	/**
	 * API to load entity by id
	 * @param id
	 * @return
	 */
	ResponseEntity<Object> findById(Long id);

	/**
	 * API to load entity by id
	 * @param id
	 * @return
	 */
	ResponseEntity<Object> deleteById(Long id);

	/**
	 * Api to create a new entity
	 * @param entity
	 * @param request
	 * @return
	 */
	ResponseEntity<Object> create(ObjectNode entity, HttpServletRequest request);

	/**
	 * API to update an entity
	 * @param entity
	 * @param request
	 * @return
	 */
	ResponseEntity<Object> update(ObjectNode entity, HttpServletRequest request);

	/**
	 * API to delete and entity
	 * @param entity
	 * @return
	 */
	ResponseEntity<Object> delete(ObjectNode entity);
	
	/**
	 * Method to check query parameter if its 'surveyjs' returns
	 * {@link SurveyEntity} object.
	 * @param request
	 * @param list
	 * @return
	 */
	default List<?> getSurveyEntityResult(HttpServletRequest request,
			List<? extends Entity> list) {
		List<?> entities = list;
		String qry = request.getParameter(IConsts.PRM_QUERY);
		if (!IUtils.isNullOrEmpty(qry) &&
				IConsts.SURVEYJS.equals(qry.toLowerCase())) {
			entities = IUtils.getValueTextForSurveyjs(list);
		}
		return entities;
	}
	
}
