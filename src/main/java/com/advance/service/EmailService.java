package com.advance.service;

import com.advance.enumeration.VerificationType;

public interface EmailService {

	void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType type); 
}
