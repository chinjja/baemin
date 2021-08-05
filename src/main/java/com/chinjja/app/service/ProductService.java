package com.chinjja.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.ProductCreateDto;
import com.chinjja.app.repo.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
	private final ProductRepository productRepository;
	
	private final ModelMapper mapper = new ModelMapper() {{
		getConfiguration()
		.setSkipNullEnabled(true);
	}};
	
	@Transactional
	@PreAuthorize("isAuthenticated() and #seller.email == principal.username")
	public Product create(Account seller, ProductCreateDto dto) {
		val product = mapper.map(dto, Product.class);
		product.setSeller(seller);
		return productRepository.save(product);
	}
	
	public Iterable<Product> findBySeller(Account seller) {
		return productRepository.findAllBySeller(seller);
	}
}
