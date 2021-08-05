package com.chinjja.app.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Product;

public interface ProductRepository extends PagingAndSortingRepository<Product, Long> {
	Iterable<Product> findAllBySeller(Account account);
}
