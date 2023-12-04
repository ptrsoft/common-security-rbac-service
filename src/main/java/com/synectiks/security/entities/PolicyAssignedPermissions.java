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

    @Column(name = "permission_category_id")
    private Long permissionCategoryId;

    @Column(name = "permission_id")
    private Long permissionId;

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
