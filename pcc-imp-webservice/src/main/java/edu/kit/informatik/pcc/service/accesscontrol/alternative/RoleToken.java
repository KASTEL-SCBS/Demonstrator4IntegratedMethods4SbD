package edu.kit.informatik.pcc.service.accesscontrol.alternative;

import java.util.Collection;

public class RoleToken {
	private Collection<Role> currentRoles;
	
	public RoleToken(Collection<Role> roles) {
		currentRoles = roles;
	}
	
	public Collection<Role> getCurrentRoles(){
		return currentRoles;
	}
}
