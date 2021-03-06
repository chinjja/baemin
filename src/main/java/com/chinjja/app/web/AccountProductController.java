package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.dto.AccountProductInfo;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account-products")
public class AccountProductController {
	private final BaeminService baeminService;
	
	@PatchMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #product.account.email == principal.username")
	public AccountProduct patch(
			@PathVariable(name = "id", required = false) AccountProduct product,
			@RequestBody AccountProductInfo dto) {
		if(product == null) {
			throw new IllegalArgumentException("id not found");
		}
		return baeminService.update(product, dto);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #product.account.email == principal.username")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@PathVariable(name = "id", required = false) AccountProduct product) {
		if(product == null) {
			throw new IllegalArgumentException("id not found");
		}
		baeminService.delete(product);
	}
	
}
