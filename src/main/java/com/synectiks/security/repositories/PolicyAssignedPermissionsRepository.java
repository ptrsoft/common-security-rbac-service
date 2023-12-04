/**
 *
 */
package com.synectiks.security.repositories;

import com.synectiks.security.entities.PolicyAssignedPermissions;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rajesh
 */
@Repository
public interface PolicyAssignedPermissionsRepository extends CrudRepository<PolicyAssignedPermissions, Long> {

}
