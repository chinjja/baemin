package com.chinjja.app.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.SellerInfo;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;
import lombok.val;

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
	public ResponseEntity<Seller> one(@PathVariable(name = "id", required = false) Seller seller) {
		if(seller == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().eTag(seller.etag()).body(seller);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Seller> update(
			WebRequest request,
			@PathVariable(name = "id", required = false) Seller seller,
			@RequestBody SellerInfo dto) {
		if(seller == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(seller.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = baeminService.update(seller, dto);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
}
