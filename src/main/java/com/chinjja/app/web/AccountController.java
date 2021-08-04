package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressCreateDto;
import com.chinjja.app.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
	private final AccountService accountService;
	
	@GetMapping("/{id}")
	public Account one(@PathVariable("id") Account account) {
		return account;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Account create(@RequestBody AccountCreateDto dto) {
		return accountService.create(dto);
	}
	
	@PostMapping("/{id}/roles/{role}")
	@ResponseStatus(HttpStatus.CREATED)
	public AccountRole addRole(
			@PathVariable("id") Account account,
			@PathVariable String role) {
		return accountService.addRole(account, role);
	}
	
	@DeleteMapping("/{id}/roles/{role}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRole(
			@PathVariable("id") Account account,
			@PathVariable String role
			) {
		accountService.deleteRole(account, role);
	}
	
	@GetMapping("/{id}/roles")
	public Iterable<String> getRoles(@PathVariable("id") Account account) {
		return accountService.getRoles(account);
	}
	
	@PostMapping("/{id}/addresses")
	@ResponseStatus(HttpStatus.CREATED)
	public Address createAddress(
			@PathVariable("id") Account account,
			@RequestBody AddressCreateDto dto) {
		return accountService.addAddress(account, dto);
	}
	
	@GetMapping("/{id}/addresses")
	public Iterable<Address> getAddresses(@PathVariable("id") Account account) {
		return accountService.getAddresses(account);
	}
}
