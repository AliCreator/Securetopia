package com.advance.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsForm {

	@NotNull(message = "Enabled cannot be empty or null")
	private Boolean enabled; 
	
	@NotNull(message = "NotLocked cannot be empty or null")
	private Boolean notLocked; 
}
