package com.advance.utils;

import org.springframework.security.core.Authentication;

import com.advance.domain.UserPrincipal;
import com.advance.dto.UserDTO;

public class UserUtils {
	public static UserDTO getAuthenticatedUser(Authentication authentication) {
		return ((UserDTO) authentication.getPrincipal());
	}
	
	public static UserDTO getUserDto(Authentication authentication) {
		return ((UserPrincipal) authentication.getPrincipal()).getUser();
	}

}
