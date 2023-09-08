package com.synectiks.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.synectiks.security.entities.Organization;

import java.util.List;

/**
 * Spring Data  repository for the Organization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    String ORG_QUERY ="select o.* from organization o where upper(o.name) = upper(:orgName)";
    @Query(value = ORG_QUERY, nativeQuery = true)
    Organization getOrganizationByName(@Param("orgName") String orgName);
}
