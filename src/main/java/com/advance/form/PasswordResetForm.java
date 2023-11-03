package com.advance.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetForm {

	@NotNull(message = "Current password cannot be null!")
	private String currentPassword; 
	
	@NotNull(message = "New password cannot be null!")
	private String newPassword; 
	
	@NotNull(message = "Confirm new password cannot be null!")
	private String confirmNewPassword; 
}
