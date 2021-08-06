package com.chinjja.app.repo;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Product;

public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
	Iterable<Product> findAllBySeller(Account account);
	Optional<Product> findBySellerAndCode(Account seller, String code);
}
