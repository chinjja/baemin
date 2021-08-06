package com.chinjja.app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.ProductUpdateDto;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
	private final BaeminService baeminService;
	
	@GetMapping("/{id}")
	public Product one(@PathVariable("id") Product product) {
		return product;
	}
	
	@PatchMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #product.seller.email == principal.username")
	public Product patch(
			@PathVariable("id") Product product,
			@RequestBody ProductUpdateDto dto) {
		return baeminService.updateProduct(product, dto);
	}
	
	@PatchMapping("/{id}/quantity")
	@PreAuthorize("isAuthenticated() and #product.seller.email == principal.username")
	public Product quantity(
			@PathVariable("id") Product product,
			@RequestParam Integer quantity) {
		return baeminService.plusQuantity(product, quantity);
	}
}
