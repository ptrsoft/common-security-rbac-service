package com.synectiks.security.entities;

import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;

import javax.persistence.*;

/**
 * @author Manoj
 */
@Entity
@Table(name = IDBConsts.Tbl_POLICY_ASSIGNED_PERMISSIONS)
public class PolicyAssignedPermissions extends PSqlEntity {

	private static final long serialVersionUID = 2619620405443093653L;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name")
    private String policyName;

    @Column(name = "permission_category_id")
    private Long permissionCategoryId;

    @Column(name = "permission_category_name")
    private String permissionCategoryName;

    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_name")
    private String permissionName;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPermissionCategoryName() {
        return permissionCategoryName;
    }

    public void setPermissionCategoryName(String permissionCategoryName) {
        this.permissionCategoryName = permissionCategoryName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }


    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Long getPermissionCategoryId() {
        return permissionCategoryId;
    }

    public void setPermissionCategoryId(Long permissionCategoryId) {
        this.permissionCategoryId = permissionCategoryId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
}
