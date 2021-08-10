package com.chinjja.app.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.security.jwt.JwtRequestFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final JwtRequestFilter jwtRequestFilter;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
		.anyRequest().permitAll()
		.and()
		.httpBasic()
		.and()
		.formLogin()
		.and()
		.csrf().ignoringAntMatchers("/api/**")
		.and()
		.exceptionHandling().authenticationEntryPoint((req, res, ex) -> res.sendError(HttpStatus.UNAUTHORIZED.value()))
		.and()
		.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http
		.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public CommandLineRunner initAccount(AccountService accountService) {
		return args -> accountService.init();
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}
