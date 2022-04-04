package com.synectiks.security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synectiks.security.entities.OrganizationalUnit;

/**
 * Spring Data  repository for the OrganizationalUnit entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrganizationalUnitRepository extends JpaRepository<OrganizationalUnit, Long> {
}
