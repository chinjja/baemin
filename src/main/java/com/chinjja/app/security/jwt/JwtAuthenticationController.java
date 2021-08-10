package com.chinjja.app.security.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.account.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class JwtAuthenticationController {
	private final AuthenticationManager authenticationManager;
	private final JwtToken jwtToken;
	private final AccountService accountService;
	
	@PostMapping("/api/signin")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest jwtRequest) throws Exception {
		val email = jwtRequest.getEmail();
		val password = jwtRequest.getPassword();
		val auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		val token = jwtToken.generateToken((UserDetails)auth.getPrincipal());
		val id = accountService.findByEmail(email).getId();
		return ResponseEntity.ok(new JwtResponse(id, token));
	}
}
