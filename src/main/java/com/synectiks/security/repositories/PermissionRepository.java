/**
 * 
 */
package com.synectiks.security.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.synectiks.security.entities.Permission;

/**
 * @author Rajesh
 */
@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {

}
