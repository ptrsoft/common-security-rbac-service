package com.synectiks.security.repositories;

import com.synectiks.security.entities.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Manoj
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long>{

	public String findIdByName(String name);

}
