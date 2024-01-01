/**
 *
 */
package com.synectiks.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.synectiks.security.entities.Permission;

import java.util.List;

/**
 * @author Rajesh
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByOrganizationId(Long organizationId);
}
