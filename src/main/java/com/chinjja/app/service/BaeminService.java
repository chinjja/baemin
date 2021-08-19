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
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.OrderProduct;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.AccountProductUpdateDto;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.ProductUpdateDto;
import com.chinjja.app.dto.SellerInfo;
import com.chinjja.app.dto.SellerUpdateDto;
import com.chinjja.app.repo.AccountProductRepository;
import com.chinjja.app.repo.OrderProductRepository;
import com.chinjja.app.repo.OrderRepository;
import com.chinjja.app.repo.ProductRepository;
import com.chinjja.app.repo.SellerRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class BaeminService {
	private final ProductRepository productRepository;
	private final AccountProductRepository accountProductRepository;
	private final OrderProductRepository orderProductRepository;
	private final OrderRepository orderRepository;
	private final SellerRepository sellerRepository;
	
	private final ModelMapper mapper = new ModelMapper() {{
		getConfiguration()
		.setSkipNullEnabled(true);
	}};
	
	@Transactional
	public Seller createSeller(Account account, @Valid SellerInfo dto) {
		val seller = new Seller();
		seller.setInfo(dto);
		seller.setAccount(account);
		return sellerRepository.save(seller);
	}
	
	@Transactional
	public Seller update(Seller seller, @Valid SellerUpdateDto dto) {
		mapper.map(dto, seller.getInfo());
		return sellerRepository.save(seller);
	}
	
	public Iterable<Seller> findAllSeller() {
		return sellerRepository.findAll();
	}
	
	public Iterable<Seller> findAllSellerByAccount(Account account) {
		return sellerRepository.findAllByAccount(account);
	}
	
	@Transactional
	public Product createProduct(Seller seller, @Valid ProductInfo dto) {
		if(productRepository.findBySellerAndInfoCode(seller, dto.getCode()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "seller or code is conflict");
		}
		val product = new Product();
		product.setInfo(dto);
		product.setSeller(seller);
		return productRepository.save(product);
	}
	
	public Iterable<Product> findProductsBySeller(Seller seller) {
		return productRepository.findAllBySeller(seller);
	}
	
	@Transactional
	public Product update(Product product, @Valid ProductUpdateDto dto) {
		mapper.map(dto, product.getInfo());
		return productRepository.save(product);
	}
	
	@Transactional
	public Product plusQuantity(Product product, int quantity) {
		if(quantity == 0) {
			return product;
		}
		val info = product.getInfo();
		val new_quantity = info.getQuantity() + quantity;
		if(new_quantity < 0) {
			throw new IllegalArgumentException("cannot be less than zero");
		}
		info.setQuantity(new_quantity);
		return productRepository.save(product);
	}
	
	@Transactional
	public Product minusQuantity(Product product, int quantity) {
		return plusQuantity(product, -quantity);
	}
	
	@Transactional
	public AccountProduct plusQuantity(AccountProduct cp, int quantity) {
		if(quantity == 0) {
			return cp;
		}
		val new_quantity = cp.getQuantity() + quantity;
		if(new_quantity < 0) {
			throw new IllegalArgumentException("cannot be less than zero");
		}
		cp.setQuantity(new_quantity);
		return accountProductRepository.save(cp);
	}
	
	@Transactional
	public AccountProduct minusQuantity(AccountProduct cp, int quantity) {
		return plusQuantity(cp, -quantity);
	}
	
	@Transactional
	public AccountProduct update(AccountProduct product, @Valid AccountProductUpdateDto dto) {
		mapper.map(dto, product);
		return accountProductRepository.save(product);
	}
	
	@Transactional
	public void delete(AccountProduct entity) {
		accountProductRepository.delete(entity);
	}
	
	@Transactional
	public AccountProduct addToCart(Account account, Product product, int quantity) {
		val cartProduct = accountProductRepository.findByAccountAndProduct(account, product);
		if(cartProduct.isPresent()) {
			return plusQuantity(cartProduct.get(), quantity);
		}
		else {
			return accountProductRepository.save(AccountProduct.builder()
						.account(account)
						.product(product)
						.quantity(quantity)
						.build());
		}
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
	public Order buy(Account account) {
		val items = accountProductRepository.findAllByAccount(account);
		if(items.isEmpty()) {
			throw new IllegalArgumentException("empty cart");
		}
		val order = orderRepository.save(Order.builder()
				.status(Status.IN_PROGRESS)
				.account(account)
				.createdAt(new Date())
				.build());
		for(val i : items) {
			val product = i.getProduct();
			val quantity = i.getQuantity();
			minusQuantity(product, quantity);
			orderProductRepository.save(OrderProduct.builder()
					.order(order)
					.product(product)
					.quantity(quantity)
					.build());
		}
		accountProductRepository.deleteAll();
		return order;
	}
	
	@Transactional
	public Order cancel(Order order) {
		if(order.getStatus() != Status.IN_PROGRESS) {
			throw new IllegalArgumentException("when status is in progress you can cancel it");
		}
		val items = orderProductRepository.findAllByOrder(order);
		for(val i : items) {
			plusQuantity(i.getProduct(), i.getQuantity());
		}
		order.setStatus(Status.CANCELLED);
		return orderRepository.save(order);
	}
	
	@Transactional
	public Order complete(Order order) {
		if(order.getStatus() != Status.IN_PROGRESS) {
			throw new IllegalArgumentException("when status is in progress you can cancel it");
		}
		order.setStatus(Status.COMPLETED);
		return orderRepository.save(order);
	}
	
	public Iterable<AccountProduct> findCartProducts(Account account) {
		return accountProductRepository.findAllByAccount(account);
	}
	
	public Iterable<OrderProduct> findOrderProducts(Order order) {
		return orderProductRepository.findAllByOrder(order);
	}
}
