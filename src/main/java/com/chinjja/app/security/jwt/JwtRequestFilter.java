package com.chinjja.app.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
	private final UserDetailsService userDetailsService;
	private final JwtToken jwtToken;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		val requestTokenHeader = request.getHeader("Authorization");
		String username = null;
		String token = null;
		
		if(requestTokenHeader != null) {
			if(requestTokenHeader.startsWith("Bearer ")) {
				token = requestTokenHeader.substring(7);
				try {
					username = jwtToken.getUsernameFromToken(token);
				} catch(Exception e) {
					log.warn(e.getMessage());
				}
			}
			
			if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				try {
					val userDetails = userDetailsService.loadUserByUsername(username);
					if(jwtToken.validateToken(token, userDetails)) {
						val usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					}
				} catch(Exception e) {
					log.warn(e.getMessage());
				}
			}
		}
		filterChain.doFilter(request, response);
	}
	
	
}
