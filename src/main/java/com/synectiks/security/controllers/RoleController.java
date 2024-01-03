/**
 *
 */
package com.synectiks.security.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synectiks.security.config.Constants;
import com.synectiks.security.config.IConsts;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.entities.*;
import com.synectiks.security.interfaces.IApiController;
import com.synectiks.security.repositories.OrganizationRepository;
import com.synectiks.security.repositories.PermissionRepository;
import com.synectiks.security.repositories.RoleRepository;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.util.IUtils;
import com.synectiks.security.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = IApiController.SEC_API + IApiController.URL_ROLES, method = RequestMethod.POST)
@CrossOrigin
public class RoleController implements IApiController {

	private static final Logger logger = LoggerFactory
			.getLogger(RoleController.class);

	@Autowired
	private RoleRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;


    @Override
	@RequestMapping(path = IConsts.API_FIND_ALL, method = RequestMethod.GET)
	public ResponseEntity<Object> findAll(HttpServletRequest request) {
		List<Role> entities = null;
		try {
			entities = (List<Role>) repository.findAll();
		} catch (Throwable th) {
			th.printStackTrace();
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entities);
	}

    @RequestMapping(path = IConsts.API_FIND_BY_OWNER, method = RequestMethod.GET)
    public ResponseEntity<Object> findByOwnerAndGroup(@RequestParam(name = "createdBy", required = true) String createdBy,
                                                      @RequestParam(name = "isGroup", required = true) boolean isGroup,
                                                      HttpServletRequest request) {
        List<Role> entities = null;
        try {
            entities = (List<Role>) repository.findByCreatedByAndGrp(createdBy, isGroup);
        } catch (Throwable th) {
            logger.error(th.getMessage(), th);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th.getStackTrace());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(entities);
    }

	@Override
	@RequestMapping(IConsts.API_CREATE)
	public ResponseEntity<Object> create(@RequestBody ObjectNode service,
			HttpServletRequest request) {
		Role entity = null;
		try {
			String userName = IUtils.getUserFromRequest(request);
			entity = IUtils.createEntity(service, userName, Role.class);
            if(StringUtils.isBlank(entity.getCreatedBy())){
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("createdBy not provided");
            }
            User user = userRepository.findByUsername(entity.getCreatedBy());
            entity.setOrganization(user.getOrganization());
            entity.setCreatedAt(new Date());
			logger.info("Role: " + entity);
            entity = repository.save(entity);

            if(entity.isGrp() && service.get("users") != null){
                JsonNode jsonNode = service.get("users");
                if(jsonNode != null && jsonNode.isArray()){
                    for (final JsonNode objNode : jsonNode) {
                        Optional<User> oUser = userRepository.findById(objNode.get("id").asLong());
                        if(oUser.isPresent()){
                            User tempUser = oUser.get();
                            tempUser.getRoles().add(entity);
                            userRepository.save(tempUser);
                        }
                    }
                }
            }

		} catch (Throwable th) {
			logger.error(th.getMessage(), th.getStackTrace());
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th.getMessage());
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

	@Override
	@RequestMapping(path = IConsts.API_FIND_ID, method = RequestMethod.GET)
	public ResponseEntity<Object> findById(@PathVariable("id") Long id) {
		Role entity = null;
		try {
			entity = repository.findById(id).orElse(null);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(entity);
	}

    @RequestMapping(path = "get-role-group-details", method = RequestMethod.GET)
    public ResponseEntity<Object> getRoleGroupDetails(@RequestParam String userName, @RequestParam Long roleId) {
        Optional<Role> oRole = repository.findById(roleId);
        if(!oRole.isPresent()){
            logger.error("Role not found. Given role id: {}",roleId);
            return ResponseEntity.status(HttpStatus.valueOf(426)).body("role not found");
        }
        if(!oRole.get().isGrp()){
            logger.error("Provided role is not a role group. Given role id: {}",roleId);
            return ResponseEntity.status(HttpStatus.valueOf(427)).body("Provided role is not a role group");
        }
        User user = this.userRepository.findByUsername(userName);
        if(user == null){
            logger.error("User not found. Given user name: {}",userName);
            return ResponseEntity.status(HttpStatus.valueOf(428)).body("user not found");
        }
        boolean isAdmin = false;
        for(Role role: user.getRoles()){
            if(Constants.USER_TYPE_SUPER_ADMIN.equalsIgnoreCase(role.getName()) && role.isDefault() && role.isGrp()){
                isAdmin = true;
                break;
            }
        }
        Role roleGrp = oRole.get();
        List<User> userList = new ArrayList<>();
        logger.info("Getting details for super admin role");
        Map<Long, User> userMap = new HashMap<>();
        if(isAdmin){
            logger.info("user : {},  is super admin", user.getUsername());
            userList = userRepository.findByOrganizationId(user.getOrganization().getId());
            for(User user1: userList){
                for(Role roleG: user1.getRoles()){
                    if(roleG.getId().compareTo(roleId) == 0){
                        userMap.put(user1.getId(), user1);
                    }
                }
            }
            List<User> grpRoleUserList = new ArrayList<>();
            for(Long key: userMap.keySet()){
                grpRoleUserList.add(userMap.get(key));
            }
            // find all the users of this group
            addUsersToRoleGroup(roleGrp, grpRoleUserList);
        }else{
            logger.info("user : {},  is not super admin", user.getUsername());
            List<User> ownerList = userRepository.findByOwnerId(user.getId());
            addUsersToRoleGroup(roleGrp, ownerList);
        }

        addAllowedAndDisAllowedPermissions(roleGrp);

        return ResponseEntity.status(HttpStatus.OK).body(roleGrp);
    }

    private void addAllowedAndDisAllowedPermissions(Role roleGroup) {
        Map<Long, Permission> allPermissions = new HashMap<>();

        Permission permission = new Permission();
        permission.setStatus(Constants.ACTIVE);
        List<Permission> permissionList = permissionRepository.findAll(Example.of(permission));
        for(Permission obj: permissionList){
            allPermissions.put(obj.getId(), obj);
        }

        // get role-group->role->policy-permission
//        for(Role roleGrp: roleGrpList){
            Map<Long, Permission> allowedPermissions = new HashMap<>();
            // union all the permission from each role
            for(Role role: roleGroup.getRoles()){
                for(Policy policy: role.getPolicies()){
                    for(PolicyAssignedPermissions pap: policy.getPermissions()){
                        Optional<Permission> op = permissionRepository.findById(pap.getPermissionId());
                        if(op.isPresent()){
                            allowedPermissions.put(pap.getPermissionId(), op.get());
                        }
                    }
                }
            }
            roleGroup.setAllowedPermissions(allowedPermissions.keySet().stream().map(key -> allowedPermissions.get(key)).collect(Collectors.toList()));
            for(Long key: allowedPermissions.keySet()){
                if(allPermissions.containsKey(key)){
                    allPermissions.remove(key);
                }
            }
            roleGroup.setDisAllowedPermissions(allPermissions.keySet().stream().map(key -> allPermissions.get(key)).collect(Collectors.toList()));
            for(Permission obj: permissionList){
                allPermissions.put(obj.getId(), obj);
            }
//        }
    }

    private void addUsersToRoleGroup(Role roleGroup, List<User> userList) {
        Map<Long, List<Role>> userRolesMap = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for(User user1: userList){
            for(Role roleGrp: user1.getRoles()){
                if(!userRolesMap.containsKey(user1.getId())){
                    List<Role> temp = new ArrayList<>();
                    temp.add(roleGrp);
                    userRolesMap.put(user1.getId(), temp);
                }else {
                    userRolesMap.get(user1.getId()).add(roleGrp);
                }
            }
            userMap.put(user1.getId(), user1);
        }

        for(Long key: userRolesMap.keySet()){
            List<Role> values = userRolesMap.get(key);
            for(Role tempRole: values){
                ObjectNode node = IUtils.OBJECT_MAPPER.createObjectNode();
                node.put("id", userMap.get(key).getId());
                node.put("userName", userMap.get(key).getUsername());
                node.put("eMail", userMap.get(key).getEmail());
                node.put("numberOfGroups", values.size());
                if(tempRole.getUsers() == null){
                    List<ObjectNode> tempList = new ArrayList<>();
                    tempList.add(node);
                    tempRole.setUsers(tempList);
                }else {
                    tempRole.getUsers().add(node);
                }
            }
            for(Role tempRole: values){
//                for(Role grp: roleGrpList){
                    if(tempRole.getId().compareTo(roleGroup.getId()) == 0){
                        roleGroup.setUsers(tempRole.getUsers());
                    }
//                }
            }
        }
    }

	@Override
	@RequestMapping(IConsts.API_DELETE_ID)
	public ResponseEntity<Object> deleteById(@PathVariable("id") Long id) {
		try {
			repository.deleteById(id);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body("Role removed Successfully");
	}

	@Override
	@RequestMapping(IConsts.API_UPDATE)
	public ResponseEntity<Object> update(@RequestBody ObjectNode entity,
			HttpServletRequest request) {
        logger.info("Request to update role");
		Role temp = null;
		try {
			String user = IUtils.getUserFromRequest(request);
			Role service = IUtils.createEntity(entity, user, Role.class);
            if(service.getId() == null){
                logger.error("Unique identifier id not provided");
                throw new BadRequestAlertException(String.format("Null id", "role"), "role", "idnull");
            }
            if (!repository.existsById(service.getId())) {
                logger.error("Role with the given id not found. Given role id: {} ", service.getId());
                throw new BadRequestAlertException("Entity not found", "role", "idnotfound");
            }
            Role existingRole = repository.findById(service.getId()).get();

            existingRole.setUpdatedAt(new Date());
            if(!StringUtils.isBlank(service.getUpdatedBy())){
                existingRole.setUpdatedBy(service.getUpdatedBy());
            }
            if(!StringUtils.isBlank(service.getDescription())){
                existingRole.setDescription(service.getDescription());
            }
            if(service.isGrp()){
                existingRole.setGrp(service.isGrp());
            }
            if(service.isDefault()){
                existingRole.setDefault(service.isDefault());
            }
            if(!StringUtils.isBlank(service.getName())){
                existingRole.setName(service.getName());
            }
            if(service.getVersion() != null){
                existingRole.setVersion(service.getVersion());
            }
            if(service.getOrganization() != null){
                if(service.getOrganization().getId() == null){
                    logger.error("Unique identifier id not provided in organization");
                    throw new BadRequestAlertException(String.format("Null id", "organization"), "organization", "idnull");
                }
                Organization organization = organizationRepository.findById(service.getOrganization().getId()).get();
                existingRole.setOrganization(organization);
            }
            if(service.getRoles() != null){
                existingRole.setRoles(service.getRoles());
            }
            if(service.getPolicies() != null){
                existingRole.setPolicies(service.getPolicies());
            }
            temp = repository.save(existingRole);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(th);
		}
		return ResponseEntity.status(HttpStatus.OK).body(temp);
	}

	@Override
	@RequestMapping(IConsts.API_DELETE)
	public ResponseEntity<Object> delete(@RequestBody ObjectNode entity) {
		if (!IUtils.isNull(entity.get(IDBConsts.Col_ID))) {
			return deleteById(entity.get(IDBConsts.Col_ID).asLong());
		}
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
				.body("Not a valid entity");
	}


}
