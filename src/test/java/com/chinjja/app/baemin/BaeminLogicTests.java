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
import com.chinjja.app.domain.CartProduct;
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
					.build())
					.withPassword(null);

			seller = accountService.create(AccountCreateDto.builder()
					.email("seller@user.com")
					.password("12345678")
					.name("seller")
					.build())
					.withPassword(null);
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
			
			val orange2 = Bridge.plus_quantity(mvc, orange, -100);
			assertThat(orange2.getQuantity()).isEqualTo(0);
			
			val ex = assertThrows(ResponseStatusException.class, () -> {
				Bridge.plus_quantity(mvc, orange, -1);
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
				val cartProduct = Bridge.add_to_cart(mvc, buyer, orange, 10);
				assertThat(cartProduct.getQuantity()).isEqualTo(10);
				assertThat(cartProduct.getProduct()).isEqualTo(orange);
			}
			
			@Test
			void addToCartWithoutUser() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					addToCart();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
			}
			
			@Test
			@WithSeller
			void addToCartWithSeller() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					addToCart();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
			}
			
			@Test
			@WithBuyer
			void whenBuy_thenShouldReturn400() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					Bridge.buy(mvc, buyer);
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			}
			
			@Test
			void whenTakeCart_thenShouldReturn404() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					Bridge.cart(mvc, buyer);
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			}
			
			@Nested
			class AddToCart {
				CartProduct orangeInCart;
				CartProduct bananaInCart;
				
				@BeforeEach
				void setup() {
					orangeInCart = baeminService.addToCart(buyer, orange, 10);
					bananaInCart = baeminService.addToCart(buyer, banana, 10);
				}
				
				@Test
				@WithBuyer
				void buy() throws Exception {
					val order = Bridge.buy(mvc, buyer);
					assertThat(order.getStatus()).isEqualTo(Status.IN_PROGRESS);
				}
				
				@Test
				@WithSeller
				void whenBuy_thenShouldThrow403() throws Exception {
					val ex = assertThrows(ResponseStatusException.class, () -> {
						Bridge.buy(mvc, buyer);
					});
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
				}
				
				@Test
				void whenBuy_thenShouldThrow401() throws Exception {
					val ex = assertThrows(ResponseStatusException.class, () -> {
						Bridge.buy(mvc, buyer);
					});
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
				}
				
				@Test
				@WithBuyer
				void addToCart() throws Exception {
					val orangeInCart = Bridge.add_to_cart(mvc, buyer, orange, 5);
					assertThat(orangeInCart.getQuantity()).isEqualTo(15);
				}
				
				@Test
				void shouldReturnCart() throws Exception {
					val cart = Bridge.cart(mvc, buyer);
					assertThat(cart.getOrder()).isNull();
					assertThat(cart.getAccount()).isEqualTo(buyer);
				}
				
				@Test
				@WithBuyer
				void testPlusQuantity() throws Exception {
					val cartProduct2 = Bridge.plus_quantity(mvc, orangeInCart, 5);
					assertThat(cartProduct2.getQuantity()).isEqualTo(15);
				}
				
				@Test
				@WithSeller
				void shouldThrow403() throws Exception {
					val ex = assertThrows(ResponseStatusException.class, () -> {
						testPlusQuantity();
					});
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
				}
				
				@Test
				void shouldThrow401() throws Exception {
					val ex = assertThrows(ResponseStatusException.class, () -> {
						testPlusQuantity();
					});
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
				}
				
				@Test
				void shouldKeepQuantity() throws Exception {
					val orange2 = Bridge.product(mvc, orange.getId());
					assertThat(orange2.getQuantity()).isEqualTo(100);
					
					val banana2 = Bridge.product(mvc, banana.getId());
					assertThat(banana2.getQuantity()).isEqualTo(10);
				}
				
				@Nested
				class Buy {
					Order order;
					
					@BeforeEach
					void setup() {
						order = baeminService.buy(buyer);
					}
					
					@Test
					void shouldConsumeProductQuantity() throws Exception {
						val orange2 = Bridge.product(mvc, orange.getId());
						assertThat(orange2.getQuantity()).isEqualTo(90);

						val banana2 = Bridge.product(mvc, banana.getId());
						assertThat(banana2.getQuantity()).isEqualTo(0);
					}
					
					@Test
					@WithBuyer
					void cancel() throws Exception {
						val order1 = Bridge.cancel(mvc, order);
						assertThat(order1.getStatus()).isEqualTo(Status.CANCELLED);
					}
					
					@Test
					void whenTakeOrder_thenShouldReturnOrder() throws Exception {
						val orders = Bridge.orders(mvc, buyer, Status.IN_PROGRESS);
						assertThat(orders).hasSize(1).contains(orders);
					}
					
					@Test
					@WithBuyer
					void whenPlusQuantity_thenShouldReturn400() throws Exception {
						val ex = assertThrows(ResponseStatusException.class, () -> {
							Bridge.plus_quantity(mvc, orangeInCart, 5);
						});
						assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					}
					
					@Nested
					class Cancel {
						Order cancelled;
						
						@BeforeEach
						void setup() {
							cancelled = baeminService.cancel(order);
						}
						
						@Test
						void productStatusShouldBeRestored() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getQuantity()).isEqualTo(100);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getQuantity()).isEqualTo(10);
						}
						
						@Test
						@WithBuyer
						void whenBuy_thenShouldReturn400() throws Exception {
							val ex = assertThrows(ResponseStatusException.class, () -> {
								Bridge.buy(mvc, buyer);
							});
							assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void whenTakeCart_thenShouldReturn404() throws Exception {
							val ex = assertThrows(ResponseStatusException.class, () -> {
								Bridge.cart(mvc, buyer);
							});
							assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
						}
					}
					
					@Test
					void complete() throws Exception {
						val order1 = Bridge.complete(mvc, order);
						assertThat(order1.getStatus()).isEqualTo(Status.COMPLETED);
					}
					
					@Nested
					class Complete {
						Order completed;
						
						@BeforeEach
						void setup() {
							completed = baeminService.complete(order);
						}
						
						@Test
						void productStatusShouldBeRetained() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getQuantity()).isEqualTo(90);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getQuantity()).isEqualTo(0);
						}
						
						@Test
						@WithBuyer
						void whenBuy_thenShouldReturn400() throws Exception {
							val ex = assertThrows(ResponseStatusException.class, () -> {
								Bridge.buy(mvc, buyer);
							});
							assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void whenTakeCart_thenShouldReturn404() throws Exception {
							val ex = assertThrows(ResponseStatusException.class, () -> {
								Bridge.cart(mvc, buyer);
							});
							assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
						}
					}
				}
			}
		}
	}
}
