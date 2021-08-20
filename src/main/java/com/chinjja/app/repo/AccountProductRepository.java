package com.chinjja.app.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Product;

public interface AccountProductRepository extends CrudRepository<AccountProduct, Long> {
	List<AccountProduct> findAllByAccount(Account account);
	Optional<AccountProduct> findByAccountAndProduct(Account account, Product product);
}
