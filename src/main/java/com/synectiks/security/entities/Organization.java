package com.synectiks.security.entities;


import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synectiks.security.domain.PSqlEntity;

/**
 * A Organization.
 */
@Entity
@Table(name = "organization")
public class Organization extends PSqlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    @Size(max = 5000)
    @Column(name = "description", length = 5000, nullable = true)
    private String description;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String fax;

    @Column(nullable = true)
    private Instant dateOfEstablishment;

    @Column(nullable = true)
    private String status;

    @Transient
    @JsonProperty
    private List<OrganizationalUnit> organizationalUnitList;

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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public Instant getDateOfEstablishment() {
		return dateOfEstablishment;
	}

	public void setDateOfEstablishment(Instant dateOfEstablishment) {
		this.dateOfEstablishment = dateOfEstablishment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<OrganizationalUnit> getOrganizationalUnitList() {
		return organizationalUnitList;
	}

	public void setOrganizationalUnitList(List<OrganizationalUnit> organizationalUnitList) {
		this.organizationalUnitList = organizationalUnitList;
	}

	@Override
	public String toString() {
		return "Organization [name=" + name + ", description=" + description + ", phone=" + phone + ", email=" + email
				+ ", address=" + address + ", fax=" + fax + ", dateOfEstablishment=" + dateOfEstablishment + ", status="
				+ status + "]";
	}
    
    
}
