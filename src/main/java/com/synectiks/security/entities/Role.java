package com.synectiks.security.entities;

import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Rajesh
 */
@Entity
@Table(name = IDBConsts.Tbl_ROLES)
public class Role extends PSqlEntity {

	private static final long serialVersionUID = 2619620405443093727L;
	public static final Role ROLE_ADMIN = create(IConsts.ADMIN);

	private String name;
	@Column(nullable = true)
	private Long version;
	private boolean grp;

    @ColumnDefault(value = "false")
    private boolean isDefault;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @OneToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = "organizations", allowSetters = true)
    private Organization organization;

    @Column(nullable = true)
	private String description;
//	@ManyToMany(targetEntity = Permission.class, fetch = FetchType.EAGER)
//	private List<Permission> permissions;
	@ManyToMany(targetEntity = Role.class, fetch = FetchType.EAGER)
	private Set<Role> roles;

    @ManyToMany(targetEntity = Policy.class, fetch = FetchType.LAZY)
    private List<Policy> policies;

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

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

//	public List<Permission> getPermissions() {
//		return permissions;
//	}
//
//	public void setPermissions(List<Permission> permissions) {
//		this.permissions = permissions;
//	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	/**
	 * Method to check if role is a group of roles
	 * @return
	 */
	public boolean isGrp() {
		return grp;
	}

	public void setGrp(boolean grp) {
		this.grp = grp;
	}

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

	@Override
	public String toString() {
		return "{" + (version != null ? "\"version\": \"" + version + "\", " : "")
				+ (name != null ? "\"name\": \"" + name + "\", " : "")
				+ (description != null ? "\"description\": \"" + description + "\", " : "")
//				+ (permissions != null ? "\"permissions\": " + permissions + ", " : "")
				+ (roles != null ? "\"roles\": " + roles + ", " : "")
				+ ((id!=null &&id > 0) ? "\"id\": " + id + ", " : "")
				+ ("\"grp\": " + grp + ", ")
                + ("\"isDefault\": " + isDefault + ", ")
				+ (createdAt != null ? "\"createdAt\": \"" + createdAt + "\", " : "")
				+ (updatedAt != null ? "\"updatedAt\": \"" + updatedAt + "\", " : "")
				+ (createdBy != null ? "\"createdBy\": \"" + createdBy + "\", " : "")
				+ (updatedBy != null ? "\"updatedBy\": \"" + updatedBy + "\"" : "")
				+ "}";
	}

	private static Role create(String roleName) {
		Role role = new Role();
		role.setName("ROLE_" + roleName);
		return role;
	}

}
