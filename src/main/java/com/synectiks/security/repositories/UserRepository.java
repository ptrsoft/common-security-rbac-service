/**
 *
 */
package com.synectiks.security.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synectiks.security.entities.User;

/**
 * @author Rajesh
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>{//,	IUserRepository {

	Optional<User> findById(long id);
	User findByUsername(String username);
    User findByEmail(String email);

    List<User> findByOwnerId(long id);

    List<User> findByOrganizationId(Long organizationId);

}
