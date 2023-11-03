package com.advance.repository.implementation;

import static com.advance.enumeration.RoleType.ROLE_USER;

import java.util.Collection;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.advance.domain.Role;
import com.advance.exception.ApiException;
import com.advance.repository.RoleRepository;
import com.advance.rowmapper.RoleRowMapper;
import static com.advance.query.RoleQuery.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {

	private final NamedParameterJdbcTemplate jdbc;

	@Override
	public Role create(Role role) {
		return null;
	}

	@Override
	public Collection<Role> list(){
		try {
			return jdbc.query(GET_ALL_ROLES_QUERY, new RoleRowMapper()); 
		} catch (Exception e) {
			throw new ApiException("Something went wrong!"); 
		}
	}

	@Override
	public Role udpate(Role role) {
		return null;
	}

	@Override
	public boolean delete(Long id) {
		return false;
	}

	@Override
	public void addRoleToUser(Long userId, String roleName) {
		log.info("Adding role {} to user with id {}", roleName, userId);

		try {
			Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("name", roleName), new RoleRowMapper());
			jdbc.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", role.getId()));

		} catch (EmptyResultDataAccessException ex) {
			throw new ApiException("No role found by name: " + ROLE_USER.name());
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}

	}

	@Override
	public Role getRoleByUserId(Long userId) {

		log.info("Fetch role for the user with id: {}", userId);

		try {
			return jdbc.queryForObject(SELECT_ROLE_BY_ID_QUERY, Map.of("id", userId), new RoleRowMapper());
		} catch (EmptyResultDataAccessException ex) {
			throw new ApiException("No role found by name: " + ROLE_USER.name());
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public Role getRoleByUserEmail(String email) {
		return null;
	}

	@Override
	public void updateUserRole(Long userId, String roleName) {

	}

}
