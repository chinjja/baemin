package com.chinjja.app.repo;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;

public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
	Iterable<Product> findAllBySeller(Seller seller);
	Optional<Product> findBySellerAndInfoCode(Seller seller, String code);
}
