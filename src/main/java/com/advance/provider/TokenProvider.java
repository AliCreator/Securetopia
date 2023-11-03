package com.advance.provider;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.advance.domain.UserPrincipal;
import com.advance.dto.UserDTO;
import com.advance.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenProvider {

	private final UserService userService;
	public static final String AUTHORITIES = "authorities";
	public static final String ISSUER = "Jan Ali Zahedi";
	public static final String USER = "Full Stack App User";
//	public static final long ACCESS_TOKEN_EXPIREATION_TIME = 2_800_000l;
	public static final long ACCESS_TOKEN_EXPIREATION_TIME = 432_000_000l;
	public static final long REFRESH_TOKEN_EXPIREATION_TIME = 432_000_000l;

	@Value("${jwt.secret}")
	private String secret;

	public String createAccessToken(UserPrincipal userPrincipal) {

		return JWT.create().withIssuer(ISSUER).withAudience(USER).withIssuedAt(new Date())
				.withSubject(String.valueOf(userPrincipal.getUser().getId()))
				.withArrayClaim(AUTHORITIES, getClaimsFromUser(userPrincipal))
				.withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIREATION_TIME))
				.sign(Algorithm.HMAC512(secret));
	}

	public String createRefreshToken(UserPrincipal userPrincipal) {
		return JWT.create().withIssuer(ISSUER).withAudience(USER).withIssuedAt(new Date())
				.withSubject(String.valueOf(userPrincipal.getUser().getId()))
				.withExpiresAt(new Date(currentTimeMillis() + REFRESH_TOKEN_EXPIREATION_TIME))
				.sign(Algorithm.HMAC512(secret));
	}

	public List<GrantedAuthority> getAuthorities(String token) {
		String[] claims = getClaimsFromToken(token);
		return stream(claims).map(SimpleGrantedAuthority::new).collect(toList());
	}

	public Authentication getAuthentication(Long id, List<GrantedAuthority> authorities,
			HttpServletRequest request) {
		
		UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(
				userService.getUserById(id), null, authorities);
		userPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		return userPassAuthToken;
	}

	public boolean isTokenValid(Long id, String token) {
		JWTVerifier verifier = getJWTVerifier();
		return !Objects.isNull(id) && !isTokenExpired(verifier, token);
	}

	public Long getSubjet(String token, HttpServletRequest requet) {
		try {
			return Long.valueOf(getJWTVerifier().verify(token).getSubject());
		} catch (TokenExpiredException e) {
			requet.setAttribute("expiredMessage", e.getMessage());
			throw e;
		} catch (InvalidClaimException e) {
			requet.setAttribute("invalidClaim", e.getMessage());
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean isTokenExpired(JWTVerifier verifier, String token) {
		Date expiration = verifier.verify(token).getExpiresAt();
		return expiration.before(new Date());
	}

	private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
		return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
	}

	private String[] getClaimsFromToken(String token) {
		JWTVerifier verifier = getJWTVerifier();

		return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
	}

	private JWTVerifier getJWTVerifier() {
		JWTVerifier verifier;
		try {
			Algorithm algorithm = Algorithm.HMAC512(secret);
			verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
		} catch (JWTVerificationException e) {
			throw new JWTVerificationException("Token cannot be verified!");
		}
		return verifier;
	}

}
