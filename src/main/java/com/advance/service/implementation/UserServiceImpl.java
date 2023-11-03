package com.advance.service.implementation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.advance.domain.Role;
import com.advance.domain.User;
import com.advance.dto.UserDTO;
import com.advance.dtomapper.UserDTOMapper;
import com.advance.form.UpdateForm;
import com.advance.repository.RoleRepository;
import com.advance.repository.UserRepository;
import com.advance.service.UserService;import com.advance.utils.UserUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository<User> userRepository;
	private final RoleRepository<Role> roleRepo; 

	@Override
	public UserDTO createUser(User user) {
		return mapToUser(userRepository.create(user));
	}

	@Override
	public UserDTO getUserByEmail(String email) {
		return mapToUser(userRepository.getUserByEmail(email));
	}

	@Override
	public void sendVerificationCode(UserDTO dto) {
		userRepository.sendVerificationCode(dto);
	}

	@Override
	public UserDTO verifyCode(String email, String code) {
		return mapToUser(userRepository.verifyCode(email, code));
	}

	private UserDTO mapToUser(User user) {
		return UserDTOMapper.fromUser(user, roleRepo.getRoleByUserId(user.getId())); 
	}

	@Override
	public void resetPassword(String email) {
		userRepository.resetPassword(email); 
		
	}

	@Override
	public UserDTO verifyPasswordKey(String key) {
		return UserDTOMapper.fromUser(userRepository.verifyPasswordKey(key));
	}

	@Override
	public void renewPassword(String key, String password, String confirmPassword) {
		userRepository.renewPassword(key, password, confirmPassword); 
		
	}

	@Override
	public UserDTO verifyAccount(String key) {
		return UserDTOMapper.fromUser(userRepository.verifyAccount(key));
	}

	@Override
	public UserDTO updateUserDetails(@Valid UpdateForm user) {
		return UserDTOMapper.fromUser(userRepository.updateUserDetails(user));
	}

	@Override
	public UserDTO getUserById(Long id) {
		return UserDTOMapper.fromUser(userRepository.get(id));
	}

	@Override
	public void updateUserPassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
		userRepository.updateUserPassword(id, currentPassword, newPassword, confirmNewPassword); 
	}

	@Override
	public void updateUserRole(Long id, String roleName) {
		userRepository.updateUserRole(id, roleName); 
	}

	@Override
	public void updateUserAccountSettings(Long id, Boolean enabled, Boolean notLocked) {
		userRepository.updateUserAccountSettings(id, enabled, notLocked); 
	}

	@Override
	public UserDTO toggleMfa(String email) {
		return UserDTOMapper.fromUser(userRepository.toggleMfa(email));
	}

	@Override
	public void updateImage(UserDTO user, MultipartFile image) {
		userRepository.updateImage(user, image); 
		
	}
}
