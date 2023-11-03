package com.advance.exception;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.advance.domain.HttpResponse;
import com.auth0.jwt.exceptions.JWTDecodeException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jan Ali Zahedi
 *
 */

@RestControllerAdvice
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		return new ResponseEntity<>(HttpResponse.builder().timeStamp(LocalDate.now().toString()).reason(ex.getMessage())
				.developerMessage(ex.getMessage()).status(HttpStatus.resolve(statusCode.value()))
				.statusCode(statusCode.value()).build(), statusCode);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		List<FieldError> fieldError = ex.getBindingResult().getFieldErrors();
		String fieldMessages = fieldError.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
		return new ResponseEntity<>(HttpResponse.builder().timeStamp(LocalDate.now().toString()).reason(fieldMessages)
				.developerMessage(ex.getMessage()).status(HttpStatus.resolve(status.value())).statusCode(status.value())
				.build(), status);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<HttpResponse> accessDenEntity(AccessDeniedException ex) {
		return new ResponseEntity<HttpResponse>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.reason("Access denied. You don't have access.").status(HttpStatus.FORBIDDEN)
				.statusCode(HttpStatus.FORBIDDEN.value()).build(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<HttpResponse> sqlIntegrityConstraintViolationException(
			SQLIntegrityConstraintViolationException e) {
		return new ResponseEntity<HttpResponse>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.reason(e.getMessage().contains("Duplicate entry") ? "Information is already available!"
						: e.getMessage())
				.status(HttpStatus.BAD_REQUEST).statusCode(HttpStatus.BAD_REQUEST.value()).build(),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<HttpResponse> badCredentialsException(BadCredentialsException e) {
		return new ResponseEntity<HttpResponse>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.status(HttpStatus.BAD_REQUEST).statusCode(HttpStatus.BAD_REQUEST.value())
				.reason(e.getMessage() + ", Invalid credentials!").developerMessage(e.getMessage()).build(),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<HttpResponse> apiException(ApiException e) {
		return new ResponseEntity<HttpResponse>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.reason(e.getMessage()).developerMessage(e.getMessage()).status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(JWTDecodeException.class)
	public ResponseEntity<HttpResponse> jwtDecodedException(JWTDecodeException e) {
		return new ResponseEntity<HttpResponse>(
				HttpResponse.builder().timeStamp(LocalDateTime.now().toString()).reason(e.getMessage())
						.developerMessage(e.getMessage()).status(HttpStatus.INTERNAL_SERVER_ERROR)
						.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<HttpResponse> emptyResultDataAccessException(EmptyResultDataAccessException e) {
		return new ResponseEntity<HttpResponse>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.reason(e.getMessage().contains("expected 1, actual 0") ? "Record not found" : e.getMessage())
				.developerMessage(e.getMessage()).status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<HttpResponse> disabledException(DisabledException exception) {
		log.error(exception.getMessage());
		return new ResponseEntity<>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.developerMessage(exception.getMessage())
				// .reason(exception.getMessage() + ". Please check your email and verify your
				// account.")
				.reason("User account is currently disabled").status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<HttpResponse> lockedException(LockedException exception) {
		log.error(exception.getMessage());
		return new ResponseEntity<>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.developerMessage(exception.getMessage())
				// .reason(exception.getMessage() + ", too many failed attempts.")
				.reason("User account is currently locked").status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<HttpResponse> dataAccessException(DataAccessException exception) {
		log.error(exception.getMessage());
		return new ResponseEntity<>(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.reason(processErrorMessage(exception.getMessage()))
				.developerMessage(processErrorMessage(exception.getMessage())).status(HttpStatus.BAD_REQUEST)
				.statusCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpResponse> exception(Exception exception) {
		log.error(exception.getMessage());
		System.out.println(exception);
		return new ResponseEntity<>(
				HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.reason(exception.getMessage() != null
								? (exception.getMessage().contains("expected 1, actual 0") ? "Record not found"
										: exception.getMessage())
								: "Some error occurred")
						.developerMessage(exception.getMessage()).status(HttpStatus.INTERNAL_SERVER_ERROR)
						.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private String processErrorMessage(String errorMessage) {
		if (errorMessage != null) {
			if (errorMessage.contains("Duplicate entry") && errorMessage.contains("AccountVerifications")) {
				return "You already verified your account.";
			}
			if (errorMessage.contains("Duplicate entry") && errorMessage.contains("ResetPasswordVerifications")) {
				return "We already sent you an email to reset your password.";
			}
			if (errorMessage.contains("Duplicate entry")) {
				return "Duplicate entry. Please try again.";
			}
		}
		return "Some error occurred";
	}

}
