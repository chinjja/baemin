package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Cart;
import com.chinjja.app.domain.Order;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
	private final BaeminService baeminService;
	
	@PostMapping("/{id}/orders")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated() and #cart.account.email == principal.username")
	public Order buy(@PathVariable("id") Cart cart) {
		return baeminService.buy(cart);
	}
	
	@GetMapping("/{id}")
	public Cart one(@PathVariable("id") Cart cart) {
		return cart;
	}
}
