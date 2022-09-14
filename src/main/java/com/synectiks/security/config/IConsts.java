/**
 * 
 */
package com.synectiks.security.config;

/**
 * @author Rajesh
 */
public interface IConsts {

	

	int SEC_IN_MILLIS = 1000;
	int MIN_IN_MILLIS = 60 * SEC_IN_MILLIS;
	
	

	
	/** 30 Sec Timeout for elastic search scroll query */

	String DELIM_COMMA = ",";

	String ADMIN = "ADMIN";
	String CAUSE = "Cause";
	String FAILED = "Failed";
	
	String SPACE = " ";
	String STATUS = "Status";


	// Index names of entities NOTE: index names must be in lower case
	String INDX_SERVICE = "indx_services";
	String INDX_SOURCE = "indx_source";
	String INDX_SUBSCRIBE = "indx_subscribe";
	

	String INDX_TYPE_SERVICE = "indx_type_service";
	String INDX_TYPE_SOURCE = "indx_type_source";
	String INDX_TYPE_SUBSCRIBE = "indx_type_subscribe";
	

	
	String DEF_ENCODING = "UTF-8";
	String JSON_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
	String DEF_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	

	

	
	
	

	String CONT_TYPE = "Content-Type";
	String CONT_TYPE_JSON = "application/json";

	String API_CREATE = "/create";
	String API_UPDATE = "/update";
	String API_DELETE = "/delete";
	String API_DELETE_ID = "/delete/{id}";
	String API_FIND_ALL = "/listAll";
	String API_FIND_ID = "/{id}";

	
	
	
	
	

	String PRM_QUERY = "query";


	
	
	//String URL_INDX_EVENT_FIRE = "http://localhost:8081/api/v1/auth/seach/fireEvent";
	

	String NESTED = "nested";
	String PATH = "path";
	String SURVEYJS = "surveyjs";
}
