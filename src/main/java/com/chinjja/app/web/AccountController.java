package com.chinjja.app.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AccountInfo;
import com.chinjja.app.account.dto.AddressInfo;
import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.SellerInfo;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
	private final AccountService accountService;
	private final BaeminService baeminService;
	
	@GetMapping("/{id}")
	public ResponseEntity<Account> one(@PathVariable(name = "id", required = false) Account account) {
		if(account == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().eTag(account.etag()).body(account);
	}
	
	@PostMapping
	public ResponseEntity<Account> create(@RequestBody AccountCreateDto dto) {
		val account = accountService.create(dto);
		return ResponseEntity.created(null).eTag(account.etag()).body(account);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Account> patch(
			WebRequest request,
			@PathVariable(name = "id", required = false) Account account,
			@RequestBody AccountInfo dto) {
		if(account == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(account.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = accountService.update(account, dto);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
	
	@PostMapping("/{id}/roles/{role}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AccountRole> addRole(
			@PathVariable("id") Account account,
			@PathVariable String role) {
		val new_role = accountService.addRole(account, role);
		return ResponseEntity.created(null).eTag(new_role.etag()).body(new_role);
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
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public ResponseEntity<Address> createAddress(
			@PathVariable("id") Account account,
			@RequestBody AddressInfo dto) {
		val address = accountService.createAddress(account, dto);
		return ResponseEntity.created(null).eTag(address.etag()).body(address);
	}
	
	@GetMapping("/{id}/addresses")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Iterable<Address> getAddresses(@PathVariable("id") Account account) {
		return accountService.getAddresses(account);
	}
	
	@GetMapping("/{id}/addresses/master")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public ResponseEntity<Address> getMasterAddress(@PathVariable("id") Account account) {
		val address = accountService.getMasterAddress(account);
		if(address == null) {
			return ResponseEntity.noContent().build();
		}
		else {
			return ResponseEntity.ok().eTag(address.etag()).body(address);
		}
	}
	
	@GetMapping("/{id}/orders")
	public Iterable<Order> getOrders(
			@PathVariable("id") Account account,
			@RequestParam(required = false) Status status) {
		return baeminService.findOrders(account, status);
	}
	
	@PutMapping("/{id}/products/{product_id}")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public ResponseEntity<AccountProduct> addToCart(
			@PathVariable("id") Account account,
			@PathVariable("product_id") Product product,
			@RequestParam(defaultValue = "1") Integer quantity) {
		val entity = baeminService.addToCart(account, product, quantity);
		return ResponseEntity.ok().eTag(entity.etag()).body(entity);
	}
	
	@PostMapping("/{id}/sellers")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public ResponseEntity<Seller> createSeller(
			@PathVariable("id") Account account,
			@RequestBody SellerInfo dto) {
		val seller = baeminService.createSeller(account, dto);
		return ResponseEntity.created(null).eTag(seller.etag()).body(seller);
	}
	
	@GetMapping("/{id}/sellers")
	public Iterable<Seller> sellers(@PathVariable("id") Account account) {
		return baeminService.findAllSellerByAccount(account);
	}
	
	@PostMapping("/{id}/orders")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public ResponseEntity<Order> buy(@PathVariable("id") Account account) {
		val order = baeminService.buy(account);
		return ResponseEntity.created(null).eTag(order.etag()).body(order);
	}
	
	@GetMapping("/{id}/products")
	@PreAuthorize("isAuthenticated() and #account.email == principal.username")
	public Iterable<AccountProduct> products(@PathVariable("id") Account account) {
		return baeminService.findCartProducts(account);
	}
}
