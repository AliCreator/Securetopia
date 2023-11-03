package com.advance.repository;

import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

import com.advance.domain.User;
import com.advance.dto.UserDTO;
import com.advance.form.UpdateForm;

import jakarta.validation.Valid;

public interface UserRepository<T extends User> {

	T create(T user); 
	Collection<T> list(int page, int pageSize); 
	T get(Long id); 
	T update(T user); 
	Boolean delete(Long id); 
	
	User getUserByEmail(String email);
	void sendVerificationCode(UserDTO dto);
	User verifyCode(String email, String code);
	void resetPassword(String email);
	User verifyPasswordKey(String key);
	void renewPassword(String key, String password, String confirmPassword);
	User verifyAccount(String key);
	User updateUserDetails(@Valid UpdateForm user);
	void updateUserPassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);
	void updateUserRole(Long id, String roleName);
	void updateUserAccountSettings(Long id, Boolean enabled, Boolean notLocked);
	User toggleMfa(String email);
	void updateImage(UserDTO user, MultipartFile image); 
}
