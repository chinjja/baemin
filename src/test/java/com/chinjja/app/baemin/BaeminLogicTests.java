package com.chinjja.app.baemin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.AccountProductUpdateDto;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.SellerInfo;
import com.chinjja.app.dto.SellerUpdateDto;
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
		void updateSeller() throws Exception {
			val updated = Bridge.update(mvc, seller, SellerUpdateDto.builder()
					.name("Updated")
					.build());
			
			assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(updated.getBody().getInfo()).isEqualTo(seller.getInfo().withName("Updated"));
			
			val updated2 = Bridge.update(mvc, seller, SellerUpdateDto.builder()
					.description("UpdatedDesc")
					.build());
			
			assertThat(updated2.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(updated2.getBody().getInfo()).isEqualTo(seller.getInfo().withName("Updated").withDescription("UpdatedDesc"));
		}
		
		@Test
		void seller() throws Exception {
			val one = Bridge.seller(mvc, seller.getId());
			assertThat(one.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(one.getBody()).isEqualTo(seller);
		}
		
		@Test
		void sellers() throws Exception {
			val sellers = Bridge.sellers(mvc);
			assertThat(sellers.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(sellers.getBody()).hasSize(1).contains(seller);
		}
		
		@Test
		void sellersByAccount() throws Exception {
			val sellers = Bridge.sellers(mvc, seller_account);
			assertThat(sellers.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(sellers.getBody()).hasSize(1).contains(seller);
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
			
			assertThat(orange.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			
			val body = orange.getBody();
			assertEquals("ORANGE", body.getCode());
			assertEquals("fresh orange", body.getTitle());
			assertEquals("this is orange", body.getDescription());
			assertEquals(new BigDecimal("1000"), body.getPrice());
			assertEquals(100, body.getQuantity());
		}
		
		@Test
		void createProductsWithoutUser() throws Exception {
			val orange = Bridge.new_product(mvc, seller, ProductInfo.builder()
					.code("ORANGE")
					.title("fresh orange")
					.description("this is orange")
					.price(new BigDecimal("1000"))
					.quantity(100)
					.build());
			assertThat(orange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
			void conflictProducts() throws Exception {
				val copy = Bridge.new_product(mvc, seller, ProductInfo.builder()
						.code("ORANGE")
						.title("fresh orange")
						.description("this is orange")
						.price(new BigDecimal("1000"))
						.quantity(100)
						.build());
				assertThat(copy.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			}
			
			@Test
			@WithSeller
			void whenUpdateProduct_thenShouldSuccess() throws Exception {
				val desc_change = Bridge.update(mvc, orange, ProductInfo.builder()
						.description("this is not orange")
						.build());
				
				assertThat(desc_change.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertEquals("this is not orange", orange.getDescription());
				
				val price_change = Bridge.update(mvc, orange, ProductInfo.builder()
						.price(new BigDecimal("100000"))
						.quantity(9999)
						.build());
				
				assertThat(price_change.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertEquals(new BigDecimal("100000"), orange.getPrice());
				assertEquals(9999, orange.getQuantity());
			}
			
			@Test
			void whenUpdateProduct_thenShouldThrow401() throws Exception {
				val updated = Bridge.update(mvc, orange, ProductInfo.builder()
						.description("this is not orange")
						.build());
				assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			}
			
			@Test
			@WithBuyer
			void whenUpdateProduct_thenShouldThrow403() throws Exception {
				val updated = Bridge.update(mvc, orange, ProductInfo.builder()
						.description("this is not orange")
						.build());
				assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			}
			
			@Test
			@WithBuyer
			void addToCart() throws Exception {
				val cartProduct = Bridge.add_to_cart(mvc, buyer, orange, 10);
				assertThat(cartProduct.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(cartProduct.getBody().getQuantity()).isEqualTo(10);
				assertThat(cartProduct.getBody().getProduct()).isEqualTo(orange);
			}
			
			@Test
			void addToCartWithoutUser() throws Exception {
				val cartProduct = Bridge.add_to_cart(mvc, buyer, orange, 10);
				assertThat(cartProduct.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			}
			
			@Test
			@WithSeller
			void addToCartWithSeller() throws Exception {
				val cartProduct = Bridge.add_to_cart(mvc, buyer, orange, 10);
				assertThat(cartProduct.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			}
			
			@Test
			@WithBuyer
			void whenTakeCart_thenShouldReturnNull() throws Exception {
				val products = Bridge.products(mvc, buyer);
				assertThat(products.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(products.getBody()).isEmpty();
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
				void testDelete() throws Exception {
					val res = Bridge.delete(mvc, orangeInCart);
					assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
					
					val products = Bridge.products(mvc, buyer);
					assertThat(products.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(products.getBody()).hasSize(1).contains(bananaInCart);
				}
				
				@Test
				@WithBuyer
				void testEtag() throws Exception {
					val products = Bridge.products(mvc, buyer);
					assertThat(products.getStatusCode()).isEqualTo(HttpStatus.OK);
					val etag = products.getHeaders().getETag();
					assertThat(etag).isNotNull();
					
					mvc.perform(get("/api/accounts/{id}/products", buyer.getId())
							.accept(MediaType.APPLICATION_JSON)
							.header("If-None-Match", etag))
							.andExpect(status().isNotModified());
					
					Bridge.update(mvc, orangeInCart, AccountProductUpdateDto.builder()
							.quantity(90)
							.build());
					
					mvc.perform(get("/api/accounts/{id}/products", buyer.getId())
							.accept(MediaType.APPLICATION_JSON)
							.header("If-None-Match", etag))
							.andExpect(status().isOk());
				}
				
				@Test
				@WithBuyer
				void findAll() throws Exception {
					val list = Bridge.products(mvc, buyer);
					assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(list.getBody()).hasSize(2).contains(orangeInCart, bananaInCart);
				}
				
				@Test
				void whenFindAll_thenShouldThrow403() throws Exception {
					val list = Bridge.products(mvc, buyer);
					assertThat(list.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				}
				
				@Test
				@WithBuyer
				void buy() throws Exception {
					val order = Bridge.buy(mvc, buyer);
					assertThat(order.getStatusCode()).isEqualTo(HttpStatus.CREATED);
					assertThat(order.getBody().getStatus()).isEqualTo(Status.IN_PROGRESS);
				}
				
				@Test
				@WithSeller
				void whenBuy_thenShouldThrow403() throws Exception {
					val order = Bridge.buy(mvc, buyer);
					assertThat(order.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
				}
				
				@Test
				void whenBuy_thenShouldThrow401() throws Exception {
					val order = Bridge.buy(mvc, buyer);
					assertThat(order.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				}
				
				@Test
				@WithBuyer
				void addToCart() throws Exception {
					val orangeInCart = Bridge.add_to_cart(mvc, buyer, orange, 5);
					assertThat(orangeInCart.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(orangeInCart.getBody().getQuantity()).isEqualTo(15);
				}
				
				@Test
				@WithBuyer
				void updateAccountProduct() throws Exception {
					val updated = Bridge.update(mvc, orangeInCart, AccountProductUpdateDto.builder()
							.quantity(97)
							.build());
					assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(updated.getBody().getQuantity()).isEqualTo(97);
				}
				
				@Test
				@WithSeller
				void shouldThrow403() throws Exception {
					val updated = Bridge.update(mvc, orangeInCart, AccountProductUpdateDto.builder()
							.quantity(97)
							.build());
					assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
				}
				
				@Test
				void shouldThrow401() throws Exception {
					val updated = Bridge.update(mvc, orangeInCart, AccountProductUpdateDto.builder()
							.quantity(97)
							.build());
					assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				}
				
				@Test
				void shouldKeepQuantity() throws Exception {
					val orange2 = Bridge.product(mvc, orange.getId());
					assertThat(orange2.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(orange2.getBody().getQuantity()).isEqualTo(100);
					
					val banana2 = Bridge.product(mvc, banana.getId());
					assertThat(banana2.getStatusCode()).isEqualTo(HttpStatus.OK);
					assertThat(banana2.getBody().getQuantity()).isEqualTo(10);
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
						assertThat(orange2.getStatusCode()).isEqualTo(HttpStatus.OK);
						assertThat(orange2.getBody().getQuantity()).isEqualTo(90);

						val banana2 = Bridge.product(mvc, banana.getId());
						assertThat(banana2.getStatusCode()).isEqualTo(HttpStatus.OK);
						assertThat(banana2.getBody().getQuantity()).isEqualTo(0);
					}
					
					@Test
					@WithBuyer
					void cancel() throws Exception {
						val order1 = Bridge.cancel(mvc, order);
						assertThat(order1.getStatusCode()).isEqualTo(HttpStatus.OK);
						assertThat(order1.getBody().getStatus()).isEqualTo(Status.CANCELLED);
					}
					
					@Test
					void whenTakeOrder_thenShouldReturnOrder() throws Exception {
						val orders = Bridge.orders(mvc, buyer, Status.IN_PROGRESS);
						assertThat(orders.getStatusCode()).isEqualTo(HttpStatus.OK);
						assertThat(orders.getBody()).hasSize(1);
					}
					
					@Test
					@WithBuyer
					void whenPlusQuantity_thenShouldReturn400() throws Exception {
						val updated = Bridge.update(mvc, orangeInCart, AccountProductUpdateDto.builder()
								.quantity(97)
								.build());
						assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
							val cancel = Bridge.cancel(mvc, cancelled);
							assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
							
							val complete = Bridge.complete(mvc, cancelled);
							assertThat(complete.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void productStatusShouldBeRestored() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(orange2.getBody().getQuantity()).isEqualTo(100);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(banana2.getBody().getQuantity()).isEqualTo(10);
						}
						
						@Test
						@WithBuyer
						void whenTakeCart_thenShouldReturnNull() throws Exception {
							val products = Bridge.products(mvc, buyer);
							assertThat(products.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(products.getBody()).isEmpty();
						}
					}
					
					@Test
					void complete() throws Exception {
						val order1 = Bridge.complete(mvc, order);
						assertThat(order1.getStatusCode()).isEqualTo(HttpStatus.OK);
						assertThat(order1.getBody().getStatus()).isEqualTo(Status.COMPLETED);
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
							val cancel = Bridge.cancel(mvc, completed);
							assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
							
							val complete = Bridge.complete(mvc, completed);
							assertThat(complete.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
						}
						
						@Test
						void productStatusShouldBeRetained() throws Exception {
							val orange2 = Bridge.product(mvc, orange.getId());
							assertThat(orange2.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(orange2.getBody().getQuantity()).isEqualTo(90);

							val banana2 = Bridge.product(mvc, banana.getId());
							assertThat(banana2.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(banana2.getBody().getQuantity()).isEqualTo(0);
						}
						
						@Test
						@WithBuyer
						void whenTakeCart_thenShouldReturnNull() throws Exception {
							val products = Bridge.products(mvc, buyer);
							assertThat(products.getStatusCode()).isEqualTo(HttpStatus.OK);
							assertThat(products.getBody()).isEmpty();
						}
					}
				}
			}
		}
	}
}
