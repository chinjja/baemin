package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.ProductCreateDto;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers")
public class SellerController {
	private final BaeminService baeminService;
	
	@PostMapping("/{id}/products")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("isAuthenticated() and #seller.account.email == principal.username")
	public Product createProduct(
			@PathVariable("id") Seller seller,
			@RequestBody ProductCreateDto dto) {
		return baeminService.createProduct(seller, dto);
	}
	
	@GetMapping("/{id}/products")
	public Iterable<Product> getProducts(
			@PathVariable("id") Seller seller) {
		return baeminService.findProductsBySeller(seller);
	}
}
