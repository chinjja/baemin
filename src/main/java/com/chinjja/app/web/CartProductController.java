package com.chinjja.app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.CartProduct;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart-products")
public class CartProductController {
	private final BaeminService baeminService;
	
	@PatchMapping("/{id}/quantity")
	@PreAuthorize("isAuthenticated() and #cartProduct.cart.account.email == principal.username")
	public CartProduct plusQuantity(
			@PathVariable("id") CartProduct cartProduct,
			@RequestParam Integer quantity) {
		return baeminService.plusQuantity(cartProduct, quantity);
	}
}
