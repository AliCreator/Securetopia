package com.advance.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_DEFAULT)
public class User {

	private Long id; 
	@NotEmpty(message = "First name cannot be empty!")
	private String firstName; 
	
	@NotEmpty(message = "Last name cannot be empty!")
	private String lastName; 
	
	@NotEmpty(message = "Email cannot be empty!")
	@Email(message = "Invalid email! Please enter a valid email.")
	private String email; 
	
	@NotEmpty(message = "Password cannot be empty!")
	private String password; 
	private String address; 
	private String phone; 
	private String title; 
	private String bio; 
	private String imageUrl; 
	private boolean enabled; 
	private boolean isNotLocked; 
	private boolean isUsingMfa; 
	private LocalDateTime createdAt; 
}
