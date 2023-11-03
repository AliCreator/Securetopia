package com.advance.service;

import org.springframework.web.multipart.MultipartFile;

import com.advance.domain.User;
import com.advance.dto.UserDTO;
import com.advance.form.UpdateForm;

import jakarta.validation.Valid;

public interface UserService {

	UserDTO createUser(User user);

	UserDTO getUserByEmail(String email);

	void sendVerificationCode(UserDTO dto);

	UserDTO verifyCode(String email, String code);

	void resetPassword(String email);

	UserDTO verifyPasswordKey(String key);

	void renewPassword(String key, String password, String confirmPassword);

	UserDTO verifyAccount(String key);

	UserDTO updateUserDetails(@Valid UpdateForm user);

	UserDTO getUserById(Long id);

	void updateUserPassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

	void updateUserRole(Long id, String roleName);

	void updateUserAccountSettings(Long id, Boolean enabled, Boolean notLocked);

	UserDTO toggleMfa(String email);

	void updateImage(UserDTO user, MultipartFile image); 
}
