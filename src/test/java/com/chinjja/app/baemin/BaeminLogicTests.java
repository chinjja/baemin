package com.chinjja.app.baemin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.ProductCreateDto;
import com.chinjja.app.service.BaeminService;
import com.chinjja.app.util.Bridge;

import lombok.val;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class BaeminLogicTests {
	@Autowired
	MockMvc mvc;

	@Autowired
	AccountService accountService;
	
	@Autowired
	BaeminService baeminService;

	@Nested
	class GenUsers {
		Account buyer;
		Account seller;
		
		@BeforeEach
		void setup() throws Exception {
			buyer = accountService.create(AccountCreateDto.builder()
					.email("buyer@user.com")
					.password("12345678")
					.name("buyer")
					.build());

			seller = accountService.create(AccountCreateDto.builder()
					.email("seller@user.com")
					.password("12345678")
					.name("seller")
					.build());
		}
		
		@Test
		@WithSeller
		void createProducts() throws Exception {
			val orange = Bridge.new_product(mvc, seller, ProductCreateDto.builder()
					.code("ORANGE")
					.title("fresh orange")
					.description("this is orange")
					.price(new BigDecimal("1000"))
					.quantity(100)
					.build());
			
			assertThat(orange.getCode()).isEqualTo("ORANGE");
			assertThat(orange.getTitle()).isEqualTo("fresh orange");
			assertThat(orange.getDescription()).isEqualTo("this is orange");
			assertThat(orange.getPrice()).isEqualTo(new BigDecimal("1000"));
			assertThat(orange.getQuantity()).isEqualTo(100);
			
			val orange2 = Bridge.product_plus_quantity(mvc, orange, -100);
			assertThat(orange2.getQuantity()).isEqualTo(0);
			
			val ex = assertThrows(ResponseStatusException.class, () -> {
				Bridge.product_plus_quantity(mvc, orange, -1);
			});
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
		
		@Test
		@WithSeller
		void conflictProducts() throws Exception {
			createProducts();
			val ex = assertThrows(ResponseStatusException.class, () -> {
				createProducts();
			});
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
		}
		
		@Test
		void createProductsWithoutUser() throws Exception {
			assertThrows(Exception.class, () -> {
				createProducts();
			});
		}
		
		@Nested
		class GenProducts {
			Product orange;
			Product banana;
			
			@BeforeEach
			void setup() {
				orange = baeminService.createProduct(seller, ProductCreateDto.builder()
						.code("ORANGE")
						.title("fresh orange")
						.description("this is orange")
						.price(new BigDecimal("1000"))
						.quantity(100)
						.build());

				banana = baeminService.createProduct(seller, ProductCreateDto.builder()
						.code("BANANA")
						.title("dirty banana")
						.description("this is banana")
						.price(new BigDecimal("500"))
						.quantity(10)
						.build());
			}
			
			@Test
			@WithBuyer
			void addToCart() throws Exception {
				Bridge.addToCart(mvc, buyer, orange, 10);
				
				val orange2 = Bridge.product(mvc, orange.getId());
				assertThat(orange2.getQuantity()).isEqualTo(100);
			}
			
			@Test
			void addToCartWithoutUser() throws Exception {
				assertThrows(Exception.class, () -> {
					addToCart();
				});
			}
			
			@Test
			@WithSeller
			void addToCartWithSeller() throws Exception {
				assertThrows(Exception.class, () -> {
					addToCart();
				});
			}
			
			
			@Nested
			class AddToCart {
				@BeforeEach
				void setup() {
					baeminService.addToCart(buyer, orange, 10);
					baeminService.addToCart(buyer, banana, 10);
				}
				
				@Test
				@WithBuyer
				void buy() throws Exception {
					val order = Bridge.buy(mvc, buyer);
					assertThat(order.getStatus()).isEqualTo(Status.IN_PROGRESS);
					
					val orange2 = Bridge.product(mvc, orange.getId());
					assertThat(orange2.getQuantity()).isEqualTo(90);

					val banana2 = Bridge.product(mvc, banana.getId());
					assertThat(banana2.getQuantity()).isEqualTo(0);
				}
				
				@Test
				void buyWithoutUser() throws Exception {
					assertThrows(Exception.class, () -> {
						Bridge.buy(mvc, buyer);
					});
				}
				
				@Nested
				class Buy {
					Order order;
					
					@BeforeEach
					void setup() {
						order = baeminService.buy(buyer);
					}
					
					@Test
					@WithBuyer
					void cancel() throws Exception {
						val order1 = Bridge.cancel(mvc, order);
						assertThat(order1.getStatus()).isEqualTo(Status.CANCELLED);
						
						val orange2 = Bridge.product(mvc, orange.getId());
						assertThat(orange2.getQuantity()).isEqualTo(100);

						val banana2 = Bridge.product(mvc, banana.getId());
						assertThat(banana2.getQuantity()).isEqualTo(10);
					}
					
					@Test
					void complete() throws Exception {
						val order1 = Bridge.complete(mvc, order);
						assertThat(order1.getStatus()).isEqualTo(Status.COMPLETED);
						
						val orange2 = Bridge.product(mvc, orange.getId());
						assertThat(orange2.getQuantity()).isEqualTo(90);

						val banana2 = Bridge.product(mvc, banana.getId());
						assertThat(banana2.getQuantity()).isEqualTo(0);
					}
				}
			}
		}
	}
}
