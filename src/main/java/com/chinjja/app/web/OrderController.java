package com.chinjja.app.web;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Order;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	private final BaeminService baeminService;
	
	@PatchMapping("/{id}/cancel")
	public Order cancel(@PathVariable("id") Order order) {
		return baeminService.cancel(order);
	}
	
	@PatchMapping("/{id}/complete")
	public Order complete(@PathVariable("id") Order order) {
		return baeminService.complete(order);
	}
}
