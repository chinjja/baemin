package com.chinjja.app.account.service;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressInfo;
import com.chinjja.app.account.repo.AccountRepository;
import com.chinjja.app.account.repo.AccountRoleRepository;
import com.chinjja.app.account.repo.AddressRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
@Validated
@Transactional(readOnly = true)
public class AccountService {
	private final AccountRepository accountRepository;
	private final AccountRoleRepository accountRoleRepository;
	private final AddressRepository addressRepository;
	private final PasswordEncoder passwordEncoder;
	
	private final ModelMapper mapper = new ModelMapper() {{
		getConfiguration()
		.setSkipNullEnabled(true);
	}};
	
	public Account findByEmail(String email) {
		return accountRepository.findByEmail(email).orElse(null);
	}
	
	@Transactional
	public void init() {
		if(accountRepository.findTopByOrderByIdAsc().isPresent()) return;
		
		val account = create(AccountCreateDto.builder()
				.email("root@user.com")
				.password("12345678")
				.name("root")
				.build());
		addRole(account, "ADMIN");
	}
	
	@Transactional
	public Account create(@Valid AccountCreateDto dto) {
		if(accountRepository.findByEmail(dto.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT);
		}
		val account = accountRepository.save(mapper.map(dto, Account.class));
		account.setPassword(passwordEncoder.encode(dto.getPassword()));
		addRole(account, "USER");
		return account;
	}
	
	@Transactional
	public AccountRole addRole(Account account, String role) {
		return accountRoleRepository.save(AccountRole.builder()
				.account(account)
				.role(role)
				.build());
	}
	
	@Transactional
	public void deleteRole(Account account, String role) {
		accountRoleRepository.deleteByAccountAndRole(account, role);
	}
	
	public Iterable<String> getRoles(Account account) {
		return StreamSupport.stream(accountRoleRepository.findAllByAccount(account).spliterator(), false)
				.map(x -> x.getRole())
				.collect(Collectors.toList());
	}
	
	@Transactional
	public Address addAddress(Account account, @Valid AddressInfo dto) {
		val addr = mapper.map(dto, Address.class);
		addr.setAccount(account);
		return addressRepository.save(addr);
	}

	@Transactional
	public void deleteAddress(Address address) {
		if(address.isMaster()) {
			throw new IllegalArgumentException("cannot delete primary address");
		}
		addressRepository.delete(address);
	}
	
	public Iterable<Address> getAddresses(Account account) {
		return addressRepository.findAllByAccount(account);
	}
	
	@Transactional
	public Address setPrimary(Address address) {
		addressRepository.findByAccountAndMasterIsTrue(address.getAccount())
		.ifPresent(x -> {
			x.setMaster(false);
			addressRepository.save(x);
		});
		
		address.setMaster(true);
		return addressRepository.save(address);
	}
}
