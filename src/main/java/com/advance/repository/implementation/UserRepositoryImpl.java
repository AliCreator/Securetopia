package com.advance.repository.implementation;

import static com.advance.enumeration.RoleType.ROLE_USER;
import static com.advance.enumeration.VerificationType.ACCOUNT;
import static com.advance.enumeration.VerificationType.PASSWORD;
import static com.advance.query.RoleQuery.SELECT_ROLE_BY_NAME_QUERY;
import static com.advance.query.RoleQuery.UPDATE_USER_ROLE_QUERY;
import static com.advance.query.UserQuery.COUNT_USER_EMAIL_QUERY;
import static com.advance.query.UserQuery.DELETE_CODE;
import static com.advance.query.UserQuery.DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY;
import static com.advance.query.UserQuery.DELETE_VERIFICATION_CODE_BY_URL_QUERY;
import static com.advance.query.UserQuery.DELETE_VERIFICATION_CODE_BY_USER_ID;
import static com.advance.query.UserQuery.GET_USER_BY_ID_QUERY;
import static com.advance.query.UserQuery.INSERT_ACCOUNT_VERIFICATION_URL_QUERY;
import static com.advance.query.UserQuery.INSERT_PASSWORD_VERIFICATION_QUERY;
import static com.advance.query.UserQuery.INSERT_USER_QUERY;
import static com.advance.query.UserQuery.INSERT_VERIFICATION_CODE_QUERY;
import static com.advance.query.UserQuery.SELECT_CODE_EXPIRATION_QUERY;
import static com.advance.query.UserQuery.SELECT_EXPIRATION_BY_URL;
import static com.advance.query.UserQuery.SELECT_USER_BY_ACCOUNT_URL_QUERY;
import static com.advance.query.UserQuery.SELECT_USER_BY_CODE_QUERY;
import static com.advance.query.UserQuery.SELECT_USER_BY_EMAIL_QUERY;
import static com.advance.query.UserQuery.SELECT_USER_BY_PASSWORD_URL_QUERY;
import static com.advance.query.UserQuery.TOGGLE_USER_MFA_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_ACCOUNT_SETTINGS_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_DETAILS_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_ENABLED_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_IMAGE_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_PASSWORD_BY_ID_QUERY;
import static com.advance.query.UserQuery.UPDATE_USER_PASSWORD_BY_URL_QUERY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.advance.domain.Role;
import com.advance.domain.User;
import com.advance.domain.UserPrincipal;
import com.advance.dto.UserDTO;
import com.advance.enumeration.VerificationType;
import com.advance.exception.ApiException;
import com.advance.form.UpdateForm;
import com.advance.repository.RoleRepository;
import com.advance.repository.UserRepository;
import com.advance.rowmapper.RoleRowMapper;
import com.advance.rowmapper.UserRowMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {

	private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	private final NamedParameterJdbcTemplate jdbc;
	private final RoleRepository<Role> roleRepository;
	private final BCryptPasswordEncoder encorder;

	@Override
	public User create(User user) {

		if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
			throw new ApiException("Email is already in use. Please use another email!");

		try {

			// We need the id of the user, but since we are using JDBC, we can't get it
			// until its saved
			KeyHolder holder = new GeneratedKeyHolder();
			SqlParameterSource paramaters = getSqlParameterSource(user);
			jdbc.update(INSERT_USER_QUERY, paramaters, holder);
			user.setId(holder.getKey().longValue());
			roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

			// Let's send the verification url to the email address
			String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

			jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));

			// Now let's send an email to the user
			// emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(),
			// verificationUrl, ACCOUNT);

			// Now, let's do the last config of the user and return it
			user.setEnabled(true);
			user.setNotLocked(true);

			return user;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ApiException("An error occured. Please try again!");
		}

	}

	@Override
	public Collection<User> list(int page, int pageSize) {
		return null;
	}

	@Override
	public User get(Long id) {
		try {
			return jdbc.queryForObject(GET_USER_BY_ID_QUERY, Map.of("id", id), new UserRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new ApiException("No user was found with this id!");
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public User update(User user) {
		return null;
	}

	@Override
	public Boolean delete(Long id) {
		return null;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = getUserByEmail(email);
		if (user == null) {
			log.error("User was not found in the database!");
			throw new UsernameNotFoundException("User was not found in the database!");
		} else {
			log.info("User found in the database: {}", email);
			return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
		}
	}

	@Override
	public User getUserByEmail(String email) {
		try {
			User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
			return user;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public void sendVerificationCode(UserDTO dto) {

		String expirateDate = format(addDays(new Date(), 1), DATE_FORMAT);
		String verificationCode = randomAlphabetic(8).toUpperCase();

		try {
			jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, Map.of("id", dto.getId()));
			jdbc.update(INSERT_VERIFICATION_CODE_QUERY,
					Map.of("userId", dto.getId(), "code", verificationCode, "expirationDate", expirateDate));

			log.info(verificationCode);
//			SmsUtils.sendSMS(dto.getPhone(), "From: Jan\nVerification code:\n" + verificationCode);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ApiException("An error occured. Please try again!");
		}

	}

	@Override
	public User verifyCode(String email, String code) {
		if (isVerificationCodeExpired(code))
			throw new ApiException("The code has expired! Please login again");
		try {
			User userByCode = jdbc.queryForObject(SELECT_USER_BY_CODE_QUERY, Map.of("code", code), new UserRowMapper());
			User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, Map.of("email", email),
					new UserRowMapper());
			if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
				jdbc.update(DELETE_CODE, Map.of("code", code));
				return userByCode;
			} else {
				throw new ApiException("The code is not found!");
			}
		} catch (EmptyResultDataAccessException e) {
			throw new ApiException("The code was not found!");
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public void resetPassword(String email) {
		if (getEmailCount(email.trim().toLowerCase()) < 0)
			throw new ApiException("Email was not found!");
		try {
			String expirateDate = format(addDays(new Date(), 1), DATE_FORMAT);
			String verificationCode = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
			User user = getUserByEmail(email);
			jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("userId", user.getId()));
			jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY,
					Map.of("userId", user.getId(), "url", verificationCode, "expirationDate", expirateDate));

			// Send Email
			log.info("The verification code {}:", verificationCode);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ApiException("An error occured. Please try again!");

		}
	}

	@Override
	public User verifyPasswordKey(String key) {
		if (isPasswordKeyExpired(key, PASSWORD))
			throw new ApiException("The key is invalid. Please reset your password again!");
		try {

			User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY,
					Map.of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
			// jdbc.update(DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY, Map.of("userId",
			// user.getId()));
			return user;
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public void renewPassword(String key, String password, String confirmPassword) {
		if (!password.equals(confirmPassword))
			throw new ApiException("Passwords don't match. Please try again!");
		try {
			jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, Map.of("password", encorder.encode(password), "url", getVerificationUrl(key, PASSWORD.getType())));
			jdbc.update(DELETE_VERIFICATION_CODE_BY_URL_QUERY,
					Map.of("url", getVerificationUrl(key, PASSWORD.getType())));
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}

	}

	@Override
	public User verifyAccount(String key) {
		try {
			User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY,
					Map.of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
			jdbc.update(UPDATE_USER_ENABLED_QUERY, Map.of("enabled", true, "userId", user.getId()));
			return user;

		} catch (EmptyResultDataAccessException e) {
			throw new ApiException("The link is not valid!");
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public User updateUserDetails(@Valid UpdateForm user) {
		try {
			jdbc.update(UPDATE_USER_DETAILS_QUERY, updateUserSqlParameterSource(user));
			User updateUser = get(user.getId());
			return updateUser;

		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	@Override
	public void updateUserPassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {

		if (!newPassword.equals(confirmNewPassword)) {
			throw new ApiException("New password and confirmed passwords are not the same. Please try again!");
		}
		User user = get(id);
		if (encorder.matches(currentPassword, user.getPassword())) {
			try {
				jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY,
						Map.of("userId", id, "password", encorder.encode(newPassword)));
			} catch (Exception e) {
				throw new ApiException("Something went wrong!");
			}
		} else {
			throw new ApiException("The password is incorrect!");
		}

	}

	@Override
	public void updateUserRole(Long id, String roleName) {
		try {
			User user = get(id);
			Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("name", roleName), new RoleRowMapper());
			jdbc.update(UPDATE_USER_ROLE_QUERY, Map.of("userId", user.getId(), "roleId", role.getId()));
		} catch (Exception e) {
			throw new ApiException("Something went wrong!");
		}
	}

	@Override
	public void updateUserAccountSettings(Long id, Boolean enabled, Boolean notLocked) {

		try {
			jdbc.update(UPDATE_USER_ACCOUNT_SETTINGS_QUERY,
					Map.of("userId", id, "enabled", enabled, "notLocked", notLocked));

		} catch (Exception e) {
			throw new ApiException("Something went wrong!");
		}
	}

	@Override
	public User toggleMfa(String email) {
		User user = getUserByEmail(email);
		if (isBlank(user.getPhone())) {
			throw new ApiException("You need a phone number to enable Multifactor Authentication");
		}
		user.setUsingMfa(!user.isUsingMfa());
		try {
			jdbc.update(TOGGLE_USER_MFA_QUERY, Map.of("email", email, "isUsingMfa", user.isUsingMfa()));
			return user;
		} catch (Exception e) {
			throw new ApiException("Something we wrong!");
		}
	}

	@Override
	public void updateImage(UserDTO user, MultipartFile image) {
		String imageUrl = setUserImageUrl(user.getEmail());
		user.setImageUrl(imageUrl);
		saveImage(user.getEmail(), image);
		jdbc.update(UPDATE_USER_IMAGE_QUERY, Map.of("image", imageUrl, "userId", user.getId()));
	}

	private String setUserImageUrl(String email) {
		// The below line will create a string that will contain the url of the server
		// (8080 OR www.google.com) + the path that we want to add
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("user/image/" + email + ".png").toUriString();
	}

	private void saveImage(String email, MultipartFile image) {
		Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/images/").toAbsolutePath()
				.normalize();
		if (!Files.exists(fileStorageLocation)) {
			try {
				Files.createDirectories(fileStorageLocation);
			} catch (Exception e) {
				log.error(e.getMessage());
				throw new ApiException("Unable to create directory!");
			}
			log.info("Created file directory: {}", fileStorageLocation);
		}

		try {
			Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
		} catch (Exception e) {
			log.error(e.getMessage()); 
			throw new ApiException("Unable to save image!");
		}
		log.info("File saved in {} folder", fileStorageLocation); 
	}

	private Boolean isPasswordKeyExpired(String key, VerificationType password) {
		try {
			return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL,
					Map.of("url", getVerificationUrl(key, PASSWORD.getType())), Boolean.class);

		} catch (EmptyResultDataAccessException e) {
			log.error(e.getMessage());
			throw new ApiException(key);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ApiException("An error occured. Please try again!");
		}
	}

	private Boolean isVerificationCodeExpired(String code) {
		try {
			return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, Map.of("code", code), Boolean.class);
		} catch (EmptyResultDataAccessException e) {
			throw new ApiException("The code is not valid. Please login again.");
		} catch (Exception e) {
			throw new ApiException("An error occured. Please try again!");
		}
	}

	private Integer getEmailCount(String email) {
		return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
	}

	private SqlParameterSource getSqlParameterSource(User user) {
		return new MapSqlParameterSource().addValue("firstName", user.getFirstName())
				.addValue("lastName", user.getLastName()).addValue("email", user.getEmail().toLowerCase())
				.addValue("password", encorder.encode(user.getPassword()));
	}

	private SqlParameterSource updateUserSqlParameterSource(UpdateForm user) {
		return new MapSqlParameterSource().addValue("firstName", user.getFirstName())
				.addValue("lastName", user.getLastName()).addValue("email", user.getEmail().toLowerCase())
				.addValue("id", user.getId()).addValue("address", user.getAddress()).addValue("bio", user.getBio())
				.addValue("phone", user.getPhone()).addValue("title", user.getTitle());
	}

	private String getVerificationUrl(String key, String type) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key)
				.toUriString();
	}

}
