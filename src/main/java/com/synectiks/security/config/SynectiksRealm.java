package com.synectiks.security.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.synectiks.security.entities.Permission;
import com.synectiks.security.entities.Role;
import com.synectiks.security.entities.User;
import com.synectiks.security.repositories.UserRepository;
import com.synectiks.security.util.IUtils;

public class SynectiksRealm extends JdbcRealm {

	private static final Logger logger = LoggerFactory.getLogger(SynectiksRealm.class);

	@Autowired
	private UserRepository userRepository;

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException {

		UsernamePasswordToken uToken = (UsernamePasswordToken) token;
		User user = null;
		if (IUtils.isNullOrEmpty(uToken.getUsername())) {
			throw new UnknownAccountException("username is null or empty!");
		}
		//user = userRepository.findById("7afb71b8-2db0-4fc0-b66f-47f34d10a5a3");
		user = userRepository.findByUsername(uToken.getUsername());
		if (IUtils.isNull(user) || (!IUtils.isNull(user) && !user.isActive())) {
			throw new UnknownAccountException("username not found!");
		}

		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(uToken.getUsername(),
				user.getPassword(), getName());
		info.setCredentialsSalt(null);

		return info;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Set<String> roleNames = new HashSet<>();
		Set<String> permissions = new HashSet<>();

		principals.forEach(p -> {
			try {
				User user = userRepository.findByUsername((String) p);
				if (!IUtils.isNull(user) && user.isActive()) {
					for (Role role : user.getRoles()) {
						roleNames.add(role.getName());
						if (!IUtils.isNull(role.getPermissions())) {
							for (Permission permisn : role.getPermissions()) {
								permissions.add(permisn.getPermission());
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}
		});

		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
		info.setStringPermissions(permissions);
		return info;
	}

	@Override
	protected Set<String> getRoleNamesForUser(Connection conn, String username)
			throws SQLException {
		Set<String> roles = new HashSet<>();
		User user = userRepository.findByUsername(username);
		if (!IUtils.isNull(user) && user.isActive()) {
			for (Role role : user.getRoles()) {
				roles.add(role.getName());
			}
		}
		return roles;
	}

	@Override
	protected Set<String> getPermissions(Connection conn, String username,
			Collection<String> roleNames) throws SQLException {

		Set<String> permisns = new HashSet<>();
		User user = userRepository.findByUsername(username);
		if (!IUtils.isNull(user) && user.isActive()) {
			for (Role role : user.getRoles()) {
				if ((!IUtils.isNull(roleNames) && roleNames.contains(role.getName()))
						&& !IUtils.isNull(role.getPermissions())) {
					for (Permission permisn : role.getPermissions()) {
						permisns.add(permisn.getPermission());
					}
				}
			}
		}

		return permisns;
	}

}
