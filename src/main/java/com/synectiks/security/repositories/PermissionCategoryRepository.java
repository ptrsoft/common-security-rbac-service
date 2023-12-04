package com.synectiks.security.repositories;

import com.synectiks.security.entities.PermissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Manoj
 */
@Repository
public interface PermissionCategoryRepository extends JpaRepository<PermissionCategory, Long>{

	public String findIdByName(String name);

}
