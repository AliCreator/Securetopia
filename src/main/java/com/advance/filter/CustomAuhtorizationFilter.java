package com.advance.filter;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.advance.provider.TokenProvider;
import com.advance.utils.ExceptionUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuhtorizationFilter extends OncePerRequestFilter {

	private static final String OPTIONS = "OPTIONS";
	private static final String[] PUBLIC_ROUTES = {"/user/verify/password", "/user/login", "/user/verify/code",
			"/user/register", "/user/resetpassword", "/user/verify/account", "/user/refresh/token",
			"/user/image", "/user/new/password"};
	private static final String TOKEN_PREFIX = "Bearer ";
	private final TokenProvider tokenProvider;
	protected static final String TOKEN_KEY = "token";
	protected static final String EMAIL_KEY = "email";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String token = getToken(request);
			Long id = getUserId(request);
			if (tokenProvider.isTokenValid(id, token)) {
				List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
				Authentication authentication = tokenProvider.getAuthentication(id, authorities,
						request);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				SecurityContextHolder.clearContext();
			}

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.error(e.getMessage());
			ExceptionUtils.processError(request, response, e);
		}

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getHeader(AUTHORIZATION) == null || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
				|| request.getMethod().equalsIgnoreCase(OPTIONS)
				|| asList(PUBLIC_ROUTES).contains(request.getRequestURI());
	}

	private Long getUserId(HttpServletRequest request) {
		return tokenProvider.getSubjet(getToken(request), request);
	}

	private String getToken(HttpServletRequest request) {
		return ofNullable(request.getHeader(AUTHORIZATION)).filter(header -> header.startsWith(TOKEN_PREFIX))
				.map(token -> token.replace(TOKEN_PREFIX, EMPTY)).get();
	}
}
