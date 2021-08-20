package com.chinjja.app.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.OrderProduct;
import com.chinjja.app.domain.Product;

public interface OrderProductRepository extends CrudRepository<OrderProduct, Long> {
	Iterable<OrderProduct> findAllByOrder(Order order);
	Optional<OrderProduct> findByOrderAndProduct(Order order, Product product);
}
