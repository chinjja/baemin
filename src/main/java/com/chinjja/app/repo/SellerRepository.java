package com.chinjja.app.repo;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Seller;

public interface SellerRepository extends CrudRepository<Seller, Long> {
	Iterable<Seller> findAllByAccount(Account account);
}
