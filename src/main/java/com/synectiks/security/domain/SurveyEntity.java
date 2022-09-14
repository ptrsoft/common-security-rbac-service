/**
 * 
 */
package com.synectiks.security.domain;

import java.io.Serializable;

/**
 * @author Rajesh Upadhyay
 */
public class SurveyEntity implements Serializable {

	private static final long serialVersionUID = -6322188214155966223L;

	public SurveyEntity() {
		// Empty constructor
	}

	public SurveyEntity(String id, String name) {
		this.value = id;
		this.text = name;
	}

	/** Represent and used like id */
	String value;
	/** Represent and used like name */
	String text;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
