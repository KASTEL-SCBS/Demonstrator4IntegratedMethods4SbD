package edu.kit.informatik.pcc.service.accesscontrol.alternative;

import java.util.Collection;

import edu.kit.informatik.pcc.service.accesscontrol.alternative.AccessControl.AccessPermissions;

public class Role {

	private String roleName;
	private Collection<AccessPermissions> permissions;

	public Role(String roleName, Collection<AccessPermissions> permissions) {
		this.roleName = roleName;
		this.permissions = permissions;
	}
	
	public String getRoleName() {
		return roleName;
	}
	
	public boolean hasPermission(AccessPermissions permission) {
		for (AccessPermissions accessPermissions : permissions) {
			if(accessPermissions.toString().equals(permission.toString())) {
				return true;
			}
		} 
		
		return false;
	}
}
