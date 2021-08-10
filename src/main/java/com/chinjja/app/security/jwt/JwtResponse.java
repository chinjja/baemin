package com.chinjja.app.security.jwt;

import lombok.Data;

@Data
public class JwtResponse {
	private final Long id;
	private final String token;
}
