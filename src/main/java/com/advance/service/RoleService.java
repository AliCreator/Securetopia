package com.advance.service;

import java.util.Collection;

import com.advance.domain.Role;

public interface RoleService {

	Role getRoleByUserId(Long id);
	Collection<Role> list(); 

}
