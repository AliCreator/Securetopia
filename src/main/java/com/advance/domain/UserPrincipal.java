package com.advance.domain;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.advance.dto.UserDTO;
import com.advance.dtomapper.UserDTOMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class UserPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final User user;
	private final Role roles;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return stream(this.roles.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).collect(toList());
	}

	@Override
	public String getPassword() {
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isNotLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.user.isEnabled();
	}

	public UserDTO getUser() {
		return UserDTOMapper.fromUser(this.user, this.roles);
	}

}
