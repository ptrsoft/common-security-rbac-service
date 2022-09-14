/**
 * 
 */
package com.synectiks.security.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.util.IUtils;

/**
 * @author Rajesh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityRule {

	/**
	 * Format like: {"url": "/api/v1/auth", "authc": true, "roles": "role1,
	 * role2", "permissions": "permis1, permis2"}
	 */
	public static final String[] secureUrls = {
			"{\"url\": \"" + IApiController.AUTH_API + "/**\", \"authc\": true}",
			"{\"url\": \"" + IApiController.SEC_API + IApiController.URL_USER
					+ "/**\", \"authc\": true, \"roles\": \"ROLE_ADMIN\"}",
			"{\"url\": \"" + IApiController.SEC_API + IApiController.URL_ROLES
					+ "/**\", \"authc\": true, \"roles\": \"ROLE_ADMIN\"}",
			"{\"url\": \"" + IApiController.SEC_API + IApiController.URL_PERMISSION
					+ "/**\", \"authc\": true, \"roles\": \"ROLE_ADMIN\"}",
			"{\"url\": \"" + IApiController.SSM_API + IApiController.URL_SSM
					+ "/**\", \"authc\": true, \"roles\": \"ROLE_ADMIN\"}",
			"{\"url\": \"" + IApiController.SSM_API + IApiController.URL_SUBSCRIPTION
					+ "/**\", \"authc\": true, \"roles\": \"ROLE_ADMIN\"}" };
	/**
	 * Format like: {"url": "/api/v1/auth", "authc": true, "roles": "role1,
	 * role2", "permissions": "permis1, permis2"}
	 */
	public static final String[] publicUtls = {
			"{\"url\": \"" + IApiController.PUB_API + "/**\", \"authc\": false}" };

	private boolean authc;
	private String url;
	private String roles;
	private String permissions;

	public boolean isAuthc() {
		return authc;
	}

	public String getUrl() {
		return url;
	}

	public String getRoles() {
		return roles;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setAuthc(boolean authc) {
		this.authc = authc;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		return "{\"authc\": " + authc + ", "
				+ (url != null ? "\"url\": \"" + url + "\", " : "")
				+ (roles != null ? "\"roles\": \"" + roles + "\", " : "")
				+ (permissions != null ? "\"permissions\": \"" + permissions + "\"" : "")
				+ "}";
	}

	/**
	 * Return security filter key
	 * @return
	 */
	public String getKey() {
		return getUrl();
	}

	/**
	 * Return security filter value
	 * @return
	 */
	public String getValue() {
		StringBuilder sb = new StringBuilder();
		if (isAuthc()) {
			sb.append("authc");
			if (!IUtils.isNullOrEmpty(roles)) {
				sb.append(", roles[" + roles + "]");
			} else if (!IUtils.isNullOrEmpty(permissions)) {
				sb.append(", perms[" + permissions + "]");
			}
		} else {
			sb.append("anon");
		}
		return sb.toString();
	}

}
