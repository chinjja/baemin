package com.chinjja.app.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Cart;
import com.chinjja.app.domain.Order;

public interface CartRepository extends CrudRepository<Cart, Long> {
	Optional<Cart> findByAccountAndOrderIsNull(Account account);
	Iterable<Cart> findAllByAccountAndOrderIsNotNull(Account account);
	Optional<Cart> findByOrder(Order order);
}
