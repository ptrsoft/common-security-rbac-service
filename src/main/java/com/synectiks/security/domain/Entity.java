/**
 * 
 */
package com.synectiks.security.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author Rajesh
 */
public abstract class Entity implements Serializable {

	private static final long serialVersionUID = -6637412247017015975L;

	@Field(type = FieldType.Text, index = false)
	protected String id;

	protected Date createdAt;
	protected Date updatedAt;
	protected String createdBy;
	protected String updatedBy;

	public Entity() {
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}

	public abstract String getId();

	public abstract String getName();

	public void setId(String id) {
		this.id = id;
	}

	public void setId(long id) {
		this.id = Long.toString(id);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * @author Rajesh
	 */
	public static class Category {

		/**
		 * Category types
		 * @author Rajesh
		 */
		public static enum TYPE {
			SERVICE, RESOURCE;
		}

		private String name;
		private TYPE type;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public TYPE getType() {
			return type;
		}

		public void setType(TYPE type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return String.format("{\"name\": \"%s\", \"type\":\"%s\"}",	name, type);
		}
	}

	/**
	 * Method to load entity properties from DTO object
	 * @param src
	 */
	public void fillFromDto(Object src) {
		BeanUtils.copyProperties(src, this);
	}
}
