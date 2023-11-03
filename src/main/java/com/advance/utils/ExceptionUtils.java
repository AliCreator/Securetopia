package com.advance.utils;

import java.io.OutputStream;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.advance.domain.HttpResponse;
import com.advance.exception.ApiException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ExceptionUtils {

	public static void processError(HttpServletRequest request, HttpServletResponse response, Exception e) {

		if (e instanceof ApiException || e instanceof DisabledException || e instanceof LockedException
				|| e instanceof InvalidClaimException || e instanceof BadCredentialsException) {
			HttpResponse httpResponse = getHttpResponse(response, e.getMessage(), HttpStatus.BAD_REQUEST);
			writeResponse(response, httpResponse);
		} else if (e instanceof TokenExpiredException) {
			HttpResponse res = getHttpResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED);
			writeResponse(response, res);
		}

		else {
			HttpResponse httpResponse = getHttpResponse(response, "An error occured. Please try again later!",
					HttpStatus.INTERNAL_SERVER_ERROR);
			writeResponse(response, httpResponse);
		}
		log.error(e.getMessage());
	}

	private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
		OutputStream out;
		try {
			out = response.getOutputStream();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, httpResponse);
			out.flush();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private static HttpResponse getHttpResponse(HttpServletResponse response, String message, HttpStatus badRequest) {
		HttpResponse httpResponse = HttpResponse.builder().message(message).timeStamp(LocalDate.now().toString())
				.status(badRequest).statusCode(badRequest.value()).build();
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(badRequest.value());
		return httpResponse;
	}
}
