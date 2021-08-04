package com.chinjja.app.security;

import java.util.stream.StreamSupport;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chinjja.app.account.repo.AccountRepository;
import com.chinjja.app.account.repo.AccountRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final AccountRepository accountRepository;
	private final AccountRoleRepository accountRoleRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return accountRepository.findByEmail(username)
				.map(x -> User.withUsername(x.getEmail())
						.password(x.getPassword())
						.roles(StreamSupport.stream(accountRoleRepository.findAllByAccount(x).spliterator(), false)
								.map(role -> role.getRole())
								.toArray(String[]::new))
						.build())
				.orElseThrow(() -> new UsernameNotFoundException(username));
	}

}
