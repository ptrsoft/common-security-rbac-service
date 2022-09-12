/**
 * 
 */
package com.synectiks.commons.entities.oak;

import java.io.Serializable;
import java.util.Date;

import javax.jcr.PropertyType;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;

/**
 * @author Rajesh
 */
public abstract class OakEntity implements Serializable {

	private static final long serialVersionUID = -6336613981911434785L;

	private String entityClass;
	private String jcrPath;
	private Date createdAt;
	private Date updatedAt;

	public OakEntity() {
		this.entityClass = this.getClass().getName();
	}

	public String getEntityClass() {
		return entityClass;
	}

	@Field(path = true)
	public String getJcrPath() {
		return jcrPath;
	}

	@Field(jcrType = PropertyType.TYPENAME_DATE)
	public Date getCreatedAt() {
		return createdAt;
	}

	@Field(jcrType = PropertyType.TYPENAME_DATE)
	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setEntityClass(String cls) {
		this.entityClass = cls;
	}

	public void setJcrPath(String path) {
		this.jcrPath = path;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}
