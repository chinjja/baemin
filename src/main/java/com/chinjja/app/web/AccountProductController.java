package com.chinjja.app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account-products")
public class AccountProductController {
	private final BaeminService baeminService;
	
	@PatchMapping("/{id}/quantity")
	@PreAuthorize("isAuthenticated() and #product.account.email == principal.username")
	public AccountProduct plusQuantity(
			@PathVariable(name = "id", required = false) AccountProduct product,
			@RequestParam Integer quantity) {
		if(product == null) {
			throw new IllegalArgumentException("id not found");
		}
		return baeminService.plusQuantity(product, quantity);
	}
}
