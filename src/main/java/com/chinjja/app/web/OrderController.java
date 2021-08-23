package com.chinjja.app.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.OrderProduct;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	private final BaeminService baeminService;
	
	@GetMapping("/{id}")
	public ResponseEntity<Order> one(@PathVariable(name = "id", required = false) Order order) {
		if(order == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().eTag(order.etag()).body(order);
	}
	
	@PatchMapping("/{id}/cancel")
	@PreAuthorize("isAuthenticated() and #order.account.email == principal.username")
	public ResponseEntity<Order> cancel(
			WebRequest request,
			@PathVariable(name = "id", required = false) Order order) {
		if(order == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(order.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = baeminService.cancel(order);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
	
	@PatchMapping("/{id}/complete")
	public ResponseEntity<Order> complete(
			WebRequest request,
			@PathVariable(name = "id", required = false) Order order) {
		if(order == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(order.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = baeminService.complete(order);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
	
	@GetMapping("/{id}/products")
	public Iterable<OrderProduct> products(@PathVariable(name = "id", required = false) Order order) {
		return baeminService.findOrderProducts(order);
	}
}
