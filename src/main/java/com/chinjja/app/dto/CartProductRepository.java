package com.chinjja.app.dto;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.domain.Cart;
import com.chinjja.app.domain.CartProduct;
import com.chinjja.app.domain.Product;

public interface CartProductRepository extends CrudRepository<CartProduct, Long> {
	Iterable<CartProduct> findAllByCart(Cart cart);
	Optional<CartProduct> findByCartAndProduct(Cart cart, Product product);
}
