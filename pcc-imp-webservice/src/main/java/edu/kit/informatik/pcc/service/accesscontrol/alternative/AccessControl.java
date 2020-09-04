package edu.kit.informatik.pcc.service.accesscontrol.alternative;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccessControl {
	// TODO: This is for Demonstrator-Purposes only. If project should be further used or
	// developed, use third-party access control or implement generic approach.
	public enum AccessPermissions {
		ACCESS_ANONYMIZED, 
		ACCESS_UNANONYMIZED, 
		ACCESS_VIDEO_METADATA, 
		ACCESS_ALL_VIDEOINFORMATION,
		ACCESS_THIRD_PARTY_VIDEO, 
		ACCESS_THIRD_PARTY_METADATA
	}

	private Set<Role> roles;
	public Map<Integer, Set<Role>> userToRoles;

	public AccessControl() {
		roles = new HashSet<Role>();
		userToRoles = new HashMap<Integer, Set<Role>>();

		generateDemoRoles();
	}

	private void grantRole(int userId, String roleName) {
		Role role = getRole(roleName);
		Set<Role> userRoles;
		if (role != null) {
			if (userToRoles.containsKey(userId)) {
				userRoles = userToRoles.get(userId);
			} else {
				userRoles = new HashSet<Role>();
				userToRoles.put(userId, userRoles);
			}
			userRoles.add(role);
		}
	}

	public void requestRoleGrant(AccessPermissions permission) {

	}

	public boolean canAccessUnanonymized(int userId) {
		return hasPermission(userId, AccessPermissions.ACCESS_UNANONYMIZED);
	}

	public boolean canAccessAnonymized(int userId) {
		return hasPermission(userId, AccessPermissions.ACCESS_ANONYMIZED);
	}

	public boolean canAccessAllVideoInformation(int userId) {
		return hasPermission(userId, AccessPermissions.ACCESS_ALL_VIDEOINFORMATION);
	}

	public boolean canAccessMetaData(int userId) {
		return hasPermission(userId, AccessPermissions.ACCESS_VIDEO_METADATA)
				|| hasPermission(userId, AccessPermissions.ACCESS_ALL_VIDEOINFORMATION);
	}

	public boolean hasPermission(int id, AccessPermissions permission) {
		RoleToken token = generateToken(id);

		for (Role role : token.getCurrentRoles()) {
			if (role.hasPermission(permission)) {
				return true;
			}
		}
		return false;
	}

	private Role getRole(String roleName) {
		for (Role role : roles) {
			if (role.getRoleName().equals(roleName)) {
				return role;
			}
		}

		return null;
	}

	public RoleToken generateToken(int id) {
		if (userToRoles.containsKey(id)) {
			Collection<Role> roles = Collections.unmodifiableCollection(userToRoles.get(id));
			return new RoleToken(roles);
		} else {
			return null;
		}
	}

	public void generateDemoRoles() {
		Set<AccessPermissions> judgePermissions = new HashSet<AccessPermissions>();
		judgePermissions.add(AccessPermissions.ACCESS_ANONYMIZED);
		judgePermissions.add(AccessPermissions.ACCESS_UNANONYMIZED);
		judgePermissions.add(AccessPermissions.ACCESS_ALL_VIDEOINFORMATION);
		judgePermissions.add(AccessPermissions.ACCESS_THIRD_PARTY_VIDEO);
		Role judgeRole = new Role("judge", judgePermissions);
		roles.add(judgeRole);

		Set<AccessPermissions> userPermissions = new HashSet<AccessControl.AccessPermissions>();
		userPermissions.add(AccessPermissions.ACCESS_UNANONYMIZED);
		userPermissions.add(AccessPermissions.ACCESS_VIDEO_METADATA);
		Role userRole = new Role("standardUser", userPermissions);
		roles.add(userRole);

		Set<AccessPermissions> lawEnforcementPermissions = new HashSet<AccessControl.AccessPermissions>();
		lawEnforcementPermissions.add(AccessPermissions.ACCESS_ALL_VIDEOINFORMATION);
		lawEnforcementPermissions.add(AccessPermissions.ACCESS_THIRD_PARTY_METADATA);
		Role lawEnforcementRole = new Role("lawEnforcement", lawEnforcementPermissions);
		roles.add(lawEnforcementRole);

		for (int i = 0; i < 10; i++) {
			Set<Role> s = new HashSet<Role>();
			s.add(judgeRole);
			s.add(lawEnforcementRole);
			userToRoles.put(i, s);
		}
	}

}
