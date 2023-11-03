package com.advance.resource;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.advance.domain.HttpResponse;
import com.advance.domain.User;
import com.advance.domain.UserPrincipal;
import com.advance.dto.UserDTO;
import com.advance.dtomapper.UserDTOMapper;
import com.advance.enumeration.EventType;
import com.advance.event.NewUserEvent;
import com.advance.form.LoginForm;
import com.advance.form.PasswordResetForm;
import com.advance.form.SettingsForm;
import com.advance.form.UpdateForm;
import com.advance.provider.TokenProvider;
import com.advance.service.EventService;
import com.advance.service.RoleService;
import com.advance.service.UserService;
import com.advance.utils.ExceptionUtils;
import com.advance.utils.UserUtils;
import com.twilio.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
public class UserResource {

	private static final String TOKEN_PREFIX = "Bearer ";
	private final UserService userService;
	private final RoleService roleService;
	private final AuthenticationManager authenticationManager;
	private final TokenProvider tokenProvider;
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final ApplicationEventPublisher publisher;
	private final EventService eventService; 
	
	@PostMapping("/register")
	public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
		UserDTO userDTO = userService.createUser(user);
		return ResponseEntity.created(getUri())
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString()).data(Map.of("user", userDTO))
						.message("User created").status(HttpStatus.CREATED).statusCode(HttpStatus.CREATED.value())
						.build());
	}

	@PostMapping("/login")
	public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
		UserDTO dto = authenticate(loginForm.getEmail(), loginForm.getPassword());
		return dto.isUsingMfa() ? sendVerificationCode(dto) : sendResponse(dto);
	}

	

	@GetMapping("/verify/code/{email}/{code}")
	public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email,
			@PathVariable("code") String code) {
		UserDTO userDTO = userService.verifyCode(email, code);
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userDTO, "access_token",
								tokenProvider.createAccessToken(getUserPrincipal(userDTO)), "refresh_token",
								tokenProvider.createRefreshToken(getUserPrincipal(userDTO))))
						.message("Login successfull!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/profile")
	public ResponseEntity<HttpResponse> getProfile(Authentication authentication) {
		UserDTO userDTO = userService.getUserByEmail(UserUtils.getAuthenticatedUser(authentication).getEmail());
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userDTO, "roles", roleService.list())).message("User profile retrieved!")
						.status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/resetpassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) {
		userService.resetPassword(email);
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.message("Email sent. Please check your email!").status(HttpStatus.OK)
						.statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/verify/password/{key}")
	public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key) {
		UserDTO dto = userService.verifyPasswordKey(key);
		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", dto)).status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@PostMapping("/resetpassword/{key}/{password}/{confirmPassword}")
	public ResponseEntity<HttpResponse> renewPassword(@PathVariable("key") String key,
			@PathVariable("password") String password, @PathVariable("confirmPassword") String confirmPassword) {
		userService.renewPassword(key, password, confirmPassword);
		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.message("Password has been updated!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/verify/account/{key}")
	public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.message(userService.verifyAccount(key).isEnabled() ? "Account already verified!"
								: "Account verified successfully!")
						.status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping("/refresh/token")
	public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest req) {
		if (isHeaderAndTokenValid(req)) {
			String token = req.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
			UserDTO user = userService.getUserById(tokenProvider.getSubjet(token, req));
			return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
					.data(Map.of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
							"refresh_token", token))
					.message("Token refreshed").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
		} else {
			return ResponseEntity.badRequest()
					.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
							.message("Refresh token not found! Please login").status(HttpStatus.BAD_REQUEST)
							.statusCode(HttpStatus.BAD_REQUEST.value()).build());
		}
	}

	@PatchMapping("/update")
	public ResponseEntity<HttpResponse> updateUserInfo(@RequestBody @Valid UpdateForm user) {
		UserDTO updatedUser = userService.updateUserDetails(user);
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString()).message("User has been updated!")
						.data(Map.of("user", updatedUser)).status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
						.build());
	}

	@PatchMapping("/update/password")
	public ResponseEntity<HttpResponse> updateUserPassword(Authentication authentication,
			@RequestBody @Valid PasswordResetForm resetForm) {
		UserDTO user = UserUtils.getAuthenticatedUser(authentication);
		userService.updateUserPassword(user.getId(), resetForm.getCurrentPassword(), resetForm.getNewPassword(),
				resetForm.getConfirmNewPassword());
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.message("User password has been updated!").status(HttpStatus.OK)
						.statusCode(HttpStatus.OK.value()).build());
	}

	@PatchMapping("/update/role/{roleName}")
	public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication,
			@PathVariable("roleName") String roleName) {
		UserDTO user = UserUtils.getAuthenticatedUser(authentication);
		userService.updateUserRole(user.getId(), roleName);

		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userService.getUserById(user.getId()), "roles",
								roleService.getRoleByUserId(user.getId())))
						.message("User role has been updated!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
						.build());
	}

	@PatchMapping("/update/settings")
	public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication,
			@RequestBody SettingsForm settingForm) {

		UserDTO user = UserUtils.getAuthenticatedUser(authentication);
		userService.updateUserAccountSettings(user.getId(), settingForm.getEnabled(), settingForm.getNotLocked());
		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserById(user.getId()), "roles",
						roleService.getRoleByUserId(user.getId())))
				.message("User account settings have been updated!").status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value()).build());
	}

	@PatchMapping("/togglemfa")
	public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) {

		UserDTO user = userService.toggleMfa(UserUtils.getAuthenticatedUser(authentication).getEmail());

		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", userService.getUserById(user.getId()), "roles",
						roleService.getRoleByUserId(user.getId())))
				.message("User account settings have been updated!").status(HttpStatus.OK)
				.statusCode(HttpStatus.OK.value()).build());
	}

	@PatchMapping("/update/image")
	public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication,
			@RequestParam("image") MultipartFile image) {
		UserDTO user = UserUtils.getAuthenticatedUser(authentication);
		userService.updateImage(user, image);
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.data(Map.of("user", userService.getUserById(user.getId()), "roles",
								roleService.getRoleByUserId(user.getId())))
						.message("User image updated").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	@GetMapping(value = "/image/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception {
		return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName));
	}
	

	@RequestMapping("/error")
	public ResponseEntity<HttpResponse> handleError(HttpServletRequest req) {

		return ResponseEntity.badRequest()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
						.message("There is no matching for " + req.getMethod() + " request on this path!")
						.status(HttpStatus.BAD_REQUEST).statusCode(HttpStatus.BAD_REQUEST.value()).build());
	}
	
	
	

	private boolean isHeaderAndTokenValid(HttpServletRequest req) {
		return req.getHeader(AUTHORIZATION) != null && req.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
				&& tokenProvider.isTokenValid(
						tokenProvider.getSubjet(req.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()), req),
						req.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()));
	}

	private URI getUri() {
		return URI
				.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
	}

	private ResponseEntity<HttpResponse> sendResponse(UserDTO dto) {
		return ResponseEntity.ok().body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString())
				.data(Map.of("user", dto,"events", eventService.getEventByUserId(dto.getId()), "access_token", tokenProvider.createAccessToken(getUserPrincipal(dto)),
						"refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(dto))))
				.message("Login successfull!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value()).build());
	}

	private UserPrincipal getUserPrincipal(UserDTO dto) {
		return new UserPrincipal(UserDTOMapper.fromUserDTO(userService.getUserByEmail(dto.getEmail())),
				roleService.getRoleByUserId(dto.getId()));
	}

	private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO dto) {
		userService.sendVerificationCode(dto);
		return ResponseEntity.ok()
				.body(HttpResponse.builder().timeStamp(LocalDateTime.now().toString()).data(Map.of("user", dto))
						.message("Verification code sent!").status(HttpStatus.OK).statusCode(HttpStatus.OK.value())
						.build());
	}

	private UserDTO authenticate(String email, String password) {
		try {
			if (null != userService.getUserByEmail(email)) {
				System.out.println(EventType.LOGIN_ATTEMPT);
				publisher.publishEvent(new NewUserEvent(email, EventType.LOGIN_ATTEMPT));
			}
			Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
			UserDTO loggedInUser = UserUtils.getUserDto(authentication);
			if (!loggedInUser.isUsingMfa()) {
				publisher.publishEvent(new NewUserEvent(email, EventType.LOGIN_ATTEMPT_SUCCESS));
			}
			return loggedInUser;
		} catch (Exception e) {
			publisher.publishEvent(new NewUserEvent(email, EventType.LOGIN_ATTEMPT_FAILURE));
			ExceptionUtils.processError(request, response, e);
			throw new ApiException(e.getMessage());
		}
	}

}
