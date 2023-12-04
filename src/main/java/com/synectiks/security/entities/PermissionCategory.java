package com.synectiks.security.entities;

import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.List;

/**
 * @author Manoj
 */
@Entity
@Table(name = IDBConsts.Tbl_PERMISSION_CATEGORY)
public class PermissionCategory extends PSqlEntity {

	private static final long serialVersionUID = 2619620405443093733L;

    private String name;

    @Column(nullable = true)
	private String description;

    @Column(nullable = true)
    private Long version;

    @Column(nullable = true)
//    @ColumnDefault(value = "ACTIVE")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ManyToMany(targetEntity = Permission.class, fetch = FetchType.LAZY)
	private List<Permission> permissions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		return "{" + (version != null ? "\"version\": \"" + version + "\", " : "")
				+ (name != null ? "\"name\": \"" + name + "\", " : "")
				+ (description != null ? "\"description\": \"" + description + "\", " : "")
                + (status != null ? "\"status\": \"" + status + "\", " : "")
//				+ (permissions != null ? "\"permissions\": " + permissions + ", " : "")
				+ ((id!=null &&id > 0) ? "\"id\": " + id + ", " : "")
				+ (createdAt != null ? "\"createdAt\": \"" + createdAt + "\", " : "")
				+ (updatedAt != null ? "\"updatedAt\": \"" + updatedAt + "\", " : "")
				+ (createdBy != null ? "\"createdBy\": \"" + createdBy + "\", " : "")
				+ (updatedBy != null ? "\"updatedBy\": \"" + updatedBy + "\"" : "")
				+ "}";
	}

}
