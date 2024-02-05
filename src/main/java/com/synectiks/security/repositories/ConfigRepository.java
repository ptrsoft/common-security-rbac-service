package com.synectiks.security.repositories;


import com.synectiks.security.entities.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Spring Data SQL repository for the Config entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {
    Config findByKey(String key);
    Config findByKeyAndOrganizationId(String key, Long organizationId);
    List<Config> findByOrganizationId(Long organizationId);
    List<Config> findByCreatedBy(String createdBy);
    List<Config> findByUpdatedBy(String updatedBy);
    List<Config> findByStatus(String status);
    List<Config> findByCreatedAt(Date date);
    List<Config> findByUpdatedAt(Date date);
}
