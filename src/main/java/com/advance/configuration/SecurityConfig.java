package com.advance.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.advance.filter.CustomAuhtorizationFilter;
import com.advance.handler.CustomAccessDeniedHandler;
import com.advance.handler.CustomAuthenticationEntryPoint;

import static org.springframework.security.config.Customizer.withDefaults;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsService userDetailsService;
	private final BCryptPasswordEncoder encoder;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAuhtorizationFilter customAuthorizationFilter;

	private static final String[] PUBLIC_URLS = { "/user/verify/password/**", "/user/login/**", "/user/verify/code/**",
			"/user/register/**", "/user/resetpassword/**", "/user/verify/account/**", "/user/refresh/token/**",
			"/user/image/**", "/user/new/password/**" };

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(c -> c.disable()).cors(withDefaults());
		http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_URLS).permitAll());
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers(HttpMethod.DELETE, "/user/delete/**").hasAuthority("DELETE:USER"));
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers(HttpMethod.DELETE, "/customer/delete/**").hasAuthority("DELETE:CUSTOMER"));
		http.exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler)
				.authenticationEntryPoint(customAuthenticationEntryPoint));
		http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
		http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(encoder);
		return new ProviderManager(authProvider);
	}
}
