package com.advance.dtomapper;

import org.springframework.beans.BeanUtils;

import com.advance.domain.Role;
import com.advance.domain.User;
import com.advance.dto.UserDTO;

public class UserDTOMapper {

	public static UserDTO fromUser(User user) {
		UserDTO dto = new UserDTO();
		BeanUtils.copyProperties(user, dto);
		return dto;
	}

	public static UserDTO fromUser(User user, Role role) {
		UserDTO dto = new UserDTO();
		BeanUtils.copyProperties(user, dto);
		dto.setRoleName(role.getName());
		dto.setPermissions(role.getPermission());
		return dto;
	}

	public static User fromUserDTO(UserDTO dto) {
		User user = new User();
		BeanUtils.copyProperties(dto, user);
		return user;
	}
}
