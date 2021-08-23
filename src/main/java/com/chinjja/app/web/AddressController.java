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

import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AddressInfo;
import com.chinjja.app.account.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {
	private final AccountService accountService;
	
	@DeleteMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #address.account.email == principal.username")
	public ResponseEntity<?> deleteAddress(
			@PathVariable(name = "id", required = false) Address address) {
		if(address == null) {
			return ResponseEntity.notFound().build();
		}
		accountService.deleteAddress(address);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #address.account.email == principal.username")
	public ResponseEntity<Address> getAddress(
			@PathVariable(name = "id", required = false) Address address) {
		if(address == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().eTag(address.etag()).body(address);
	}
	
	@PatchMapping("/{id}")
	@PreAuthorize("isAuthenticated() and #address.account.email == principal.username")
	public ResponseEntity<Address> patch(
			WebRequest request,
			@PathVariable(name = "id", required = false) Address address,
			@RequestBody AddressInfo dto) {
		if(address == null) {
			return ResponseEntity.notFound().build();
		}
		val etag = request.getHeader(HttpHeaders.IF_MATCH);
		if(!StringUtils.hasText(etag)) {
			return ResponseEntity.badRequest().build();
		}
		if(!etag.equals(address.etag())) {
			return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
		}
		val updated = accountService.updateAddress(address, dto);
		return ResponseEntity.ok().eTag(updated.etag()).body(updated);
	}
}
