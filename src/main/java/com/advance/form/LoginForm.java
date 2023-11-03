package com.advance.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginForm {

	@NotEmpty(message = "Email field cannot be empty!")
	private String email; 
	
	@NotEmpty(message = "Password field cannot be empty!")
	private String password; 
}
