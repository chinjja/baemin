package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.account.Address;
import com.chinjja.app.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {
	private final AccountService accountService;
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("isAuthenticated() and #address.account.email == principal.username")
	public void deleteAddress(
			@PathVariable("id") Address address) {
		accountService.deleteAddress(address);
	}
	
	@GetMapping("/{id}")
	public Address getAddress(
			@PathVariable("id") Address address) {
		return address;
	}
}
