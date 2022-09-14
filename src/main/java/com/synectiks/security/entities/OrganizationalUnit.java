package com.synectiks.security.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synectiks.security.domain.PSqlEntity;

/**
 * A OrganizationalUnit.
 */
@Entity
@Table(name = "organizational_unit")
public class OrganizationalUnit extends PSqlEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;

    @Size(max = 5000)
    @Column(name = "description", length = 5000, nullable = true)
    private String description;

    @Column(nullable = true)
    private String status;

    @ManyToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = "organizationalUnits", allowSetters = true)
    private Organization organization;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Override
	public String toString() {
		return "OrganizationalUnit [name=" + name + ", description=" + description + ", status=" + status
				+ ", organization=" + organization + "]";
	}

    
}
