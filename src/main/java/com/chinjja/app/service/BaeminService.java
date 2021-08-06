package com.chinjja.app.service;

import java.util.Date;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.chinjja.app.account.Account;
import com.chinjja.app.domain.Cart;
import com.chinjja.app.domain.CartProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.CartProductRepository;
import com.chinjja.app.dto.CartRepository;
import com.chinjja.app.dto.OrderRepository;
import com.chinjja.app.dto.ProductCreateDto;
import com.chinjja.app.dto.ProductUpdateDto;
import com.chinjja.app.repo.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class BaeminService {
	private final ProductRepository productRepository;
	private final CartRepository cartRepository;
	private final CartProductRepository cartProductRepository;
	private final OrderRepository orderRepository;
	
	private final ModelMapper mapper = new ModelMapper() {{
		getConfiguration()
		.setSkipNullEnabled(true);
	}};
	
	@Transactional
	public Product createProduct(Account seller, @Valid ProductCreateDto dto) {
		if(productRepository.findBySellerAndCode(seller, dto.getCode()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "seller or code is conflict");
		}
		val product = mapper.map(dto, Product.class);
		product.setSeller(seller);
		return productRepository.save(product);
	}
	
	public Iterable<Product> findProductsBySeller(Account seller) {
		return productRepository.findAllBySeller(seller);
	}
	
	@Transactional
	public Product updateProduct(Product product, @Valid ProductUpdateDto dto) {
		mapper.map(dto, product);
		return productRepository.save(product);
	}
	
	@Transactional
	public Product plusQuantity(Product product, int quantity) {
		if(quantity == 0) {
			return product;
		}
		val new_quantity = product.getQuantity() + quantity;
		if(new_quantity < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot be less than zero");
		}
		product.setQuantity(new_quantity);
		return productRepository.save(product);
	}
	
	@Transactional
	public Product minusQuantity(Product product, int quantity) {
		return plusQuantity(product, -quantity);
	}
	
	@Transactional
	public CartProduct addToCart(Cart cart, Product product, int quantity) {
		val cartProduct = cartProductRepository.findByCartAndProduct(cart, product)
				.orElseGet(() -> CartProduct.builder()
						.cart(cart)
						.product(product)
						.quantity(0)
						.build());
		val new_quantity = cartProduct.getQuantity() + quantity;
		if(new_quantity < 0) {
			throw new IllegalArgumentException("cannot be zero");
		}
		cartProduct.setQuantity(quantity);
		return cartProductRepository.save(cartProduct);
	}
	
	@Transactional
	public CartProduct addToCart(Account account, Product product, int quantity) {
		val cart = cartRepository.findByAccountAndOrderIsNull(account)
				.orElseGet(() -> cartRepository.save(Cart.builder()
						.account(account)
						.build()));
		return addToCart(cart, product, quantity);
	}
	
	public Cart findCart(Account account) {
		return cartRepository.findByAccountAndOrderIsNull(account).orElse(null);
	}
	
	public Iterable<Order> findOrders(Account account, Status status) {
		if(status == null) {
			return orderRepository.findAllByAccount(account);
		}
		else {
			return orderRepository.findAllByAccountAndStatus(account, status);
		}
	}
	
	@Transactional
	public Order buy(Cart cart) {
		if(cart.getOrder() != null) {
			throw new IllegalArgumentException("already bought");
		}
		for(val i : cartProductRepository.findAllByCart(cart)) {
			minusQuantity(i.getProduct(), i.getQuantity());
		}
		val order = orderRepository.save(Order.builder()
				.status(Status.IN_PROGRESS)
				.account(cart.getAccount())
				.createdAt(new Date())
				.build());
		cart.setOrder(order);
		cartRepository.save(cart);
		return order;
	}
	
	@Transactional
	public Order buy(Account account) {
		val cart = cartRepository.findByAccountAndOrderIsNull(account)
				.orElseThrow(() -> new IllegalArgumentException("no cart"));
		return buy(cart);
	}
	
	@Transactional
	public Order cancel(Order order) {
		val cart = cartRepository.findByOrder(order).get();
		for(val i : cartProductRepository.findAllByCart(cart)) {
			plusQuantity(i.getProduct(), i.getQuantity());
		}
		order.setStatus(Status.CANCELLED);
		return orderRepository.save(order);
	}
	
	@Transactional
	public Order complete(Order order) {
		order.setStatus(Status.COMPLETED);
		return orderRepository.save(order);
	}
}
