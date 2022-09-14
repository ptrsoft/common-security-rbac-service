package com.synectiks.security.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.security.entities.Organization;
import com.synectiks.security.entities.OrganizationalUnit;
import com.synectiks.security.entities.User;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.OrganizationalUnitRepository;
import com.synectiks.security.repositories.UserRepository;

@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_ORGANIZATION, method = RequestMethod.POST)
@CrossOrigin
public class OrganizationController {

	private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private OrganizationalUnitRepository organizationalUnitRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/getAllOrganizations")
	private List<Organization> getAllOrganization() {
		List<Organization> list = organizationRepository.findAll(Sort.by(Direction.DESC, "id"));
		for(Organization org: list) {
			OrganizationalUnit ou = new OrganizationalUnit();
			ou.setOrganization(org);
			List<OrganizationalUnit> ouList = organizationalUnitRepository.findAll(Sort.by(Direction.DESC, "name"));
			org.setOrganizationalUnitList(ouList);
		}
		return list;
	}
	
	@GetMapping("/getOrganization/{id}")
    public ResponseEntity<Organization> getOrganization(@PathVariable Long id) {
		logger.debug("Request to get Organization : {}", id);
		Optional<Organization> oo = organizationRepository.findById(id);
		if(oo.isPresent()) {
			Organization org = oo.get();
			OrganizationalUnit ou = new OrganizationalUnit();
			ou.setOrganization(org);
			List<OrganizationalUnit> ouList = organizationalUnitRepository.findAll(Sort.by(Direction.DESC, "name"));
			org.setOrganizationalUnitList(ouList);
			return ResponseEntity.status(HttpStatus.OK).body(org);
		}
		return ResponseEntity.status(HttpStatus.OK).body(null);
    }
	
	@RequestMapping(path = "/getOrganizationByUserName", method = RequestMethod.GET)
	public Organization getOrganizationByUserName(@RequestParam String userName) {
		try {
			User user = this.userRepository.findByUsername(userName);
			if(user != null) {
				logger.info("Tenant found: "+  user.getOrganization().toString());
				return user.getOrganization();
			}
		} catch (Throwable th) {
			logger.error("Tenant not found: "+ th);
			return null;
		}
		return null;
	}
}
