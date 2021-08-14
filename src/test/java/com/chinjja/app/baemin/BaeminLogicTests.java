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
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.ProductUpdateDto;
import com.chinjja.app.dto.SellerInfo;
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
		Account seller_account;
		Seller seller;
		
		@BeforeEach
		void setup() throws Exception {
			buyer = accountService.create(AccountCreateDto.builder()
					.email("buyer@user.com")
					.password("12345678")
					.name("buyer")
					.build())
					.withPassword(null);

			seller_account = accountService.create(AccountCreateDto.builder()
					.email("seller@user.com")
					.password("12345678")
					.name("seller")
					.build())
					.withPassword(null);
			
			seller = baeminService.createSeller(seller_account, SellerInfo.builder()
					.name("I am seller")
					.description("ok")
					.build());
		}
		
		@Test
		void seller() throws Exception {
			val get_seller = Bridge.seller(mvc, seller.getId());
			assertThat(seller).isEqualTo(get_seller);
		}
		
		@Test
		void sellers() throws Exception {
			val sellers = Bridge.sellers(mvc);
			assertThat(sellers).hasSize(1).contains(seller);
		}
		
		@Test
		@WithSeller
		void createProducts() throws Exception {
			val orange = Bridge.new_product(mvc, seller, ProductInfo.builder()
					.code("ORANGE")
					.title("fresh orange")
					.description("this is orange")
					.price(new BigDecimal("1000"))
					.quantity(100)
					.build());
			
			assertThat(orange.getInfo()).isEqualTo(ProductInfo.builder()
					.code("ORANGE")
					.title("fresh orange")
					.description("this is orange")
					.price(new BigDecimal("1000"))
					.quantity(100)
					.build());
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
				orange = baeminService.createProduct(seller, ProductInfo.builder()
						.code("ORANGE")
						.title("fresh orange")
						.description("this is orange")
						.price(new BigDecimal("1000"))
						.quantity(100)
						.build());

				banana = baeminService.createProduct(seller, ProductInfo.builder()
						.code("BANANA")
						.title("dirty banana")
						.description("this is banana")
						.price(new BigDecimal("500"))
						.quantity(10)
						.build());
			}
			
			@Test
			@WithSeller
			void whenUpdateProduct_thenShouldSuccess() throws Exception {
				val desc_change = Bridge.update(mvc, orange, ProductUpdateDto.builder()
						.description("this is not orange")
						.build());
				
				assertThat(desc_change.getInfo()).isEqualTo(orange.getInfo()
						.withDescription("this is not orange"));
				
				val price_change = Bridge.update(mvc, orange, ProductUpdateDto.builder()
						.price(new BigDecimal("100000"))
						.quantity(9999)
						.build());
				
				assertThat(price_change.getInfo()).isEqualTo(orange.getInfo()
						.withPrice(new BigDecimal("100000"))
						.withQuantity(9999));
			}
			
			@Test
			void whenUpdateProduct_thenShouldThrow401() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					whenUpdateProduct_thenShouldSuccess();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
			}
			
			@Test
			@WithBuyer
			void whenUpdateProduct_thenShouldThrow403() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					whenUpdateProduct_thenShouldSuccess();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
			}
			
			@Test
			@WithSeller
			void shouldBeChangedToZero() throws Exception {
				val orange2 = Bridge.plus_quantity(mvc, orange, -100);
				assertThat(orange2.getInfo().getQuantity()).isEqualTo(0);
			}
			
			@Test
			@WithSeller
			void whenChangedToNegative_thenShouldReturn400() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					Bridge.plus_quantity(mvc, orange, -101);
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			}
			
			@Test
			void whenChangeQuantity_thenShouldReturn401() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					shouldBeChangedToZero();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
			}
			
			@Test
			@WithBuyer
			void whenChangeQuantity_thenShouldReturn403() throws Exception {
				val ex = assertThrows(ResponseStatusException.class, () -> {
					shouldBeChangedToZero();
				});
				assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
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
			void whenTakeCart_thenShouldReturnNull() throws Exception {
				val products = Bridge.products(mvc, buyer);
				assertThat(products).isEmpty();
			}
			
			@Nested
			class AddToCart {
				AccountProduct orangeInCart;
				AccountProduct bananaInCart;
				
				@BeforeEach
				void setup() {
					orangeInCart = baeminService.addToCart(buyer, orange, 10);
					bananaInCart = baeminService.addToCart(buyer, banana, 10);
				}
				
				@Test
				@WithBuyer
				void findAll() throws Exception {
					val list = Bridge.products(mvc, buyer);
					assertThat(list).hasSize(2).contains(orangeInCart, bananaInCart);
				}
				
				@Test
				void whenFindAll_thenShouldThrow403() throws Exception {
					val ex = assertThrows(ResponseStatusException.class, () -> {
						findAll();
					});
					assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
					assertThat(orange2.getInfo().getQuantity()).isEqualTo(100);
					
					val banana2 = Bridge.product(mvc, banana.getId());
					assertThat(banana2.getInfo().getQuantity()).isEqualTo(10);
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
						assertThat(orange2.getInfo().getQuantity()).isEqualTo(90);

						val banana2 = Bridge.product(mvc, banana.getId());
						assertThat(banana2.getInfo().getQuantity()).isEqualTo(0);
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
						@WithBuyer
						void shouldNotBeModified() throws Exception {
							val e1 = assertThrows(ResponseStatusException.class, () -> {
								Bridge.cancel(mvc, cancelled);
							});
							assertThat(e1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
							
							val e2 = assertThrows(ResponseStatusException.class, () -> {
								Bridge.complete(mvc, cancelled);
							});
							assertThat(e2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void productStatusShouldBeRestored() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getInfo().getQuantity()).isEqualTo(100);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getInfo().getQuantity()).isEqualTo(10);
						}
						
						@Test
						@WithBuyer
						void whenTakeCart_thenShouldReturnNull() throws Exception {
							val products = Bridge.products(mvc, buyer);
							assertThat(products).isEmpty();
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
						@WithBuyer
						void shouldNotBeModified() throws Exception {
							val e1 = assertThrows(ResponseStatusException.class, () -> {
								Bridge.cancel(mvc, completed);
							});
							assertThat(e1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
							
							val e2 = assertThrows(ResponseStatusException.class, () -> {
								Bridge.complete(mvc, completed);
							});
							assertThat(e2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void productStatusShouldBeRetained() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getInfo().getQuantity()).isEqualTo(90);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getInfo().getQuantity()).isEqualTo(0);
						}
						
						@Test
						@WithBuyer
						void whenTakeCart_thenShouldReturnNull() throws Exception {
							val products = Bridge.products(mvc, buyer);
							assertThat(products).isEmpty();
						}
					}
				}
			}
		}
	}
}
