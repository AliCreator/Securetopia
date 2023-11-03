package com.advance.service.implementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.advance.enumeration.VerificationType;
import com.advance.exception.ApiException;
import com.advance.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;

	@Override
	public void sendVerificationEmail(String firstName, String email, String verificationUrl, VerificationType type) {

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("Jan.AZahedi@gmail.com");
			message.setTo(email);
			message.setSubject(String.format("Full Stack App - %s Verification Email", type));
			message.setText(getBodyText(firstName, verificationUrl, type));
			mailSender.send(message);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private String getBodyText(String firstName, String verificationUrl, VerificationType type) {
		switch (type) {
		case PASSWORD: {

			return "Hello " + firstName
					+ ",\n\nReset password request. Please click the link below to reset your password.\n\n"
					+ verificationUrl + "\n\nFullStack App Support Team";
		}

		case ACCOUNT: {
			return "Hello " + firstName
					+ ",\n\nAccount verification request. Please click the link below to verify your account.\n\n"
					+ verificationUrl + "\n\nFullStack App Support Team";
		}
		default:
			throw new ApiException("Unable to send email. Email type unknown!");
		}
	}

}
