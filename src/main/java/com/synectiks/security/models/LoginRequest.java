/**
 * 
 */
package com.synectiks.security.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Rajesh
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

	private String username;
	private String password;
	private boolean rememberMe;
	private String redirectTo;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public String getRedirectTo() {
		return redirectTo;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}

	@Override
	public String toString() {
		return "{\"" + (username != null ? "username\": \"" + username + "\", " : "")
				+ (password != null ? "password\": \"" + password + "\", " : "")
				+ "rememberMe\": \"" + rememberMe + "\", "
				+ (redirectTo != null ? "redirectTo\": \"" + redirectTo : "") + "}";
	}

}
