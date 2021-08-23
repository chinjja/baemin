package com.chinjja.app.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.dto.AccountProductInfo;
import com.chinjja.app.service.BaeminService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account-products")
public class AccountProductController {
	private final BaeminService baeminService;
	
	@GetMapping("/{id}")
	public ResponseEntity<AccountProduct> one(
			@PathVariable(name = "id", required = false) AccountProduct product) {
		if(product == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().eTag(product.etag()).body(product);
	}
	
	@PatchMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #product.account.email == principal.username")
	public ResponseEntity<AccountProduct> patch(
			WebRequest request,
			@PathVariable(name = "id", required = false) AccountProduct product,
			@RequestBody AccountProductInfo dto) {
		if(product == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(product.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = baeminService.update(product, dto);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #product.account.email == principal.username")
	public ResponseEntity<?> delete(
			@PathVariable(name = "id", required = false) AccountProduct product) {
		if(product == null) {
			return ResponseEntity.notFound().build();
		}
		baeminService.delete(product);
		return ResponseEntity.noContent().build();
	}
}
