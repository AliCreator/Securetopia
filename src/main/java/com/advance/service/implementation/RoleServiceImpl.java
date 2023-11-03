package com.advance.service.implementation;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.advance.domain.Role;
import com.advance.repository.RoleRepository;
import com.advance.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
	
	private final RoleRepository<Role> roleRepository; 
	
	@Override
	public Role getRoleByUserId(Long id) {
		return roleRepository.getRoleByUserId(id);
	}

	@Override
	public Collection<Role> list() {
		
		return roleRepository.list();
	}

}
