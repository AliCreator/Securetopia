package com.advance.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateForm {

	@NotNull(message = "ID cannot be empty or null!")	
	private Long id; 
	
	@NotEmpty(message = "First name cannot be empty!")
	private String firstName; 
	
	@NotEmpty(message = "Last name cannot be empty!")
	private String lastName; 
	
	@NotEmpty(message = "Email cannot be empty!")
	@Email(message = "Invalid email! Please enter a valid email.")
	private String email; 
	
	@Pattern(regexp = "^\\d{11}$", message = "Please enter 11 digit phone number!")
	private String phone; 
	private String address; 
	
	private String title; 
	private String bio; 

}
