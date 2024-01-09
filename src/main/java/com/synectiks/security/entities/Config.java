package com.synectiks.security.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

/**
 * @author Manoj
 */
@Entity
@Table(name = IDBConsts.Tbl_Config)
public class Config extends PSqlEntity {
	private static final long serialVersionUID = 8158287362423611830L;

    @Column(name = "key")
    private String key;

    @Column(name = "value")
    private String value;

    @Column(nullable = true)
    private String status;

    @Column(name = "is_encrypted")
    private boolean isEncrypted;

    @OneToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = "organizations", allowSetters = true)
    private Organization organization;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

}
