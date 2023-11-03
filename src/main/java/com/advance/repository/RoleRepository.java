package com.advance.repository;

import java.util.Collection;

import com.advance.domain.Role;

public interface RoleRepository<T extends Role> {

	T create(Role role);

	Collection<T> list();

	T udpate(Role role);

	boolean delete(Long id);

	void addRoleToUser(Long userId, String roleName);

	Role getRoleByUserId(Long userId);

	Role getRoleByUserEmail(String email);

	void updateUserRole(Long userId, String roleName);
}
