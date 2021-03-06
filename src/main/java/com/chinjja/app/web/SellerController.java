package com.chinjja.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.SellerInfo;
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
			@RequestBody ProductInfo dto) {
		return baeminService.createProduct(seller, dto);
	}
	
	@GetMapping("/{id}/products")
	public Iterable<Product> getProducts(
			@PathVariable("id") Seller seller) {
		return baeminService.findProductsBySeller(seller);
	}
	
	@GetMapping
	public Iterable<Seller> all() {
		return baeminService.findAllSeller();
	}
	
	@GetMapping("/{id}")
	public Seller one(@PathVariable("id") Seller seller) {
		return seller;
	}
	
	@PatchMapping("/{id}")
	public Seller update(
			@PathVariable("id") Seller seller,
			@RequestBody SellerInfo dto) {
		return baeminService.update(seller, dto);
	}
}
