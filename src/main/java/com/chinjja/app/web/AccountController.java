package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressCreateDto;
import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.SellerInfo;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
	private final AccountService accountService;
	private final BaeminService baeminService;
	
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
	@PreAuthorize("hasRole('ADMIN')")
	public AccountRole addRole(
			@PathVariable("id") Account account,
			@PathVariable String role) {
		return accountService.addRole(account, role);
	}
	
	@DeleteMapping("/{id}/roles/{role}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
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
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Address createAddress(
			@PathVariable("id") Account account,
			@RequestBody AddressCreateDto dto) {
		return accountService.addAddress(account, dto);
	}
	
	@GetMapping("/{id}/addresses")
	public Iterable<Address> getAddresses(@PathVariable("id") Account account) {
		return accountService.getAddresses(account);
	}
	
	@GetMapping("/{id}/orders")
	public Iterable<Order> getOrders(
			@PathVariable("id") Account account,
			@RequestParam(required = false) Status status) {
		return baeminService.findOrders(account, status);
	}
	
	@PutMapping("/{id}/products/{product_id}")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public AccountProduct addToCart(
			@PathVariable("id") Account account,
			@PathVariable("product_id") Product product,
			@RequestParam(defaultValue = "1") Integer quantity) {
		return baeminService.addToCart(account, product, quantity);
	}
	
	@PostMapping("/{id}/sellers")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Seller createSeller(
			@PathVariable("id") Account account,
			@RequestBody SellerInfo dto) {
		return baeminService.createSeller(account, dto);
	}
	
	@PostMapping("/{id}/orders")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Order buy(@PathVariable("id") Account account) {
		return baeminService.buy(account);
	}
	
	@GetMapping("/{id}/products")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Iterable<AccountProduct> products(@PathVariable("id") Account account) {
		return baeminService.findCartProducts(account);
	}
}
