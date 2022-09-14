package com.synectiks.security.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;

/**
 * @author Rajesh
 */
@Entity
@Table(name = IDBConsts.Tbl_PERMISSION)
public class Permission extends PSqlEntity {

	private static final long serialVersionUID = 8069169541347906220L;

	@Column(nullable = true)
    private Long version;
    private String name;
    private String permission;
    @Column(nullable = true)
    private String description;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permis) {
        this.permission = permis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	@Override
	public String toString() {
		return "{\"" + (version != null ? "version\": \"" + version + "\", " : "")
				+ (name != null ? "name\": \"" + name + "\", " : "")
				+ (description != null ? "description\": \"" + description + "\", " : "")
				+ ( (id!=null && id > 0) ? "id\": \"" + id + "\", " : "")
				+ (createdAt != null ? "createdAt\": \"" + createdAt + "\", " : "")
				+ (updatedAt != null ? "updatedAt\": \"" + updatedAt + "\", " : "")
				+ (createdBy != null ? "createdBy\": \"" + createdBy + "\", " : "")
				+ (updatedBy != null ? "updatedBy\": \"" + updatedBy : "") + "}";
	}

}
