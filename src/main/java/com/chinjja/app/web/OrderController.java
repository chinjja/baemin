package com.chinjja.app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.OrderProduct;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	private final BaeminService baeminService;
	
	@PatchMapping("/{id}/cancel")
	@PreAuthorize("isAuthenticated() and #order.account.email == principal.username")
	public Order cancel(@PathVariable("id") Order order) {
		return baeminService.cancel(order);
	}
	
	@PatchMapping("/{id}/complete")
	public Order complete(@PathVariable("id") Order order) {
		return baeminService.complete(order);
	}
	
	@GetMapping("/{id}/products")
	public Iterable<OrderProduct> products(@PathVariable("id") Order order) {
		return baeminService.findOrderProducts(order);
	}
}
