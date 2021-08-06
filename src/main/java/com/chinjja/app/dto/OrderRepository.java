package com.chinjja.app.dto;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;

public interface OrderRepository extends CrudRepository<Order, Long> {
	Iterable<Order> findAllByAccount(Account account);
	Iterable<Order> findAllByAccountAndStatus(Account account, Status status);
}
