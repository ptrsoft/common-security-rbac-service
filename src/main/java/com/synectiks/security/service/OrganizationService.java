package com.synectiks.security.service;


import com.synectiks.security.entities.Organization;
import com.synectiks.security.repositories.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

	public Organization save(Organization organization) {
    	logger.debug("Save organization : {}", organization);
        return organizationRepository.save(organization);
    }

//    @Transactional(readOnly = true)
    public List<Organization> findAll(){
    	logger.debug("Get all organizations");
        return organizationRepository.findAll();
    }

//    @Transactional(readOnly = true)
    public Optional<Organization> findOne(Long id) {
    	logger.debug("Get an organization : {}", id);
        return organizationRepository.findById(id);
    }

    public void delete(Long id) {
    	logger.debug("Request to delete an organization : {}", id);
        organizationRepository.deleteById(id);
    }

//    @Transactional(readOnly = true)
    public List<Organization> search(Organization organization) {
    	logger.debug("Search organizations on given filters");
        return organizationRepository.findAll(Example.of(organization), Sort.by(Sort.Direction.DESC, "name"));
    }

//    @Transactional(readOnly = true)
    public Organization getOrganizationByName(String orgName) {
        logger.debug("Get an organization by name: {}", orgName);
        return organizationRepository.getOrganizationByName(orgName);
    }
}
