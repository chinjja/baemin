package com.chinjja.app.baemin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.ProductCreateDto;
import com.chinjja.app.util.Bridge;

import lombok.val;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class BaeminTests {
	@Autowired
	MockMvc mvc;

	static Account buyer;
	static Account seller;

	static Product orange;
	static Product banana;

	@Test
	@Order(1)
	void setupUsers() throws Exception {
		buyer = Bridge.new_account(mvc, AccountCreateDto.builder()
				.email("buyer@user.com")
				.password("12345678")
				.name("buyer")
				.build());

		assertNotNull(buyer);

		seller = Bridge.new_account(mvc, AccountCreateDto.builder()
				.email("seller@user.com")
				.password("12345678")
				.name("seller")
				.build());

		assertNotNull(seller);
	}

	@Test
	@Order(2)
	@WithMockUser("seller@user.com")
	void setupProduct() throws Exception {
		assertNotNull(buyer);
		assertNotNull(seller);

		orange = Bridge.new_product(mvc, seller, ProductCreateDto.builder()
				.code("ORANGE")
				.title("fresh orange")
				.description("this is orange")
				.price(new BigDecimal("1000"))
				.quantity(100)
				.build());
		assertNotNull(orange);

		banana = Bridge.new_product(mvc, seller, ProductCreateDto.builder()
				.code("BANANA")
				.title("dirty banana")
				.description("this is banana")
				.price(new BigDecimal("500"))
				.quantity(10)
				.build());
		assertNotNull(banana);
	}

	@Test
	@Order(3)
	void verifyProducts() throws Exception {
		val products = Bridge.products(mvc, seller);
		assertThat(products).hasSize(2);

		val orange2 = Bridge.product(mvc, orange.getId());
		assertThat(orange2.getQuantity()).isEqualTo(100);

		val banana2 = Bridge.product(mvc, banana.getId());
		assertThat(banana2.getQuantity()).isEqualTo(10);
	}
	
	@Test
	@Order(4)
	@WithMockUser("buyer@user.com")
	void addToCart() throws Exception {
		Bridge.addToCart(mvc, buyer, orange, 10);
		Bridge.addToCart(mvc, buyer, banana, 10);
	}
	
	@Test
	@Order(5)
	void verifyProductsAfterAddToCart() throws Exception {
		val orange2 = Bridge.product(mvc, orange.getId());
		assertThat(orange2.getQuantity()).isEqualTo(100);

		val banana2 = Bridge.product(mvc, banana.getId());
		assertThat(banana2.getQuantity()).isEqualTo(10);
	}
	
	@Test
	@Order(6)
	@WithMockUser("buyer@user.com")
	void buy() throws Exception {
		val order = Bridge.buy(mvc, buyer);
		assertThat(order.getStatus()).isEqualTo(Status.IN_PROGRESS);
	}
	
	@Test
	@Order(7)
	void verifyProductsAfterBuy() throws Exception {
		val orange2 = Bridge.product(mvc, orange.getId());
		assertThat(orange2.getQuantity()).isEqualTo(90);

		val banana2 = Bridge.product(mvc, banana.getId());
		assertThat(banana2.getQuantity()).isEqualTo(0);
	}
	
	@Test
	@Order(8)
	void shouldFailWithoutAuthentication() throws Exception {
		val orders = Bridge.orders(mvc, buyer, Status.IN_PROGRESS);
		val ex = assertThrows(ResponseStatusException.class, () -> {
			Bridge.cancel(mvc, orders[0]);
		});
		assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	@Order(9)
	@WithMockUser("buyer@user.com")
	void cancelOrder() throws Exception {
		val orders = Bridge.orders(mvc, buyer, Status.IN_PROGRESS);
		val order = Bridge.cancel(mvc, orders[0]);
		assertThat(order.getStatus()).isEqualTo(Status.CANCELLED);
	}
	
	@Test
	@Order(10)
	void verifyProductsAfterCancel() throws Exception {
		val orange2 = Bridge.product(mvc, orange.getId());
		assertThat(orange2.getQuantity()).isEqualTo(100);

		val banana2 = Bridge.product(mvc, banana.getId());
		assertThat(banana2.getQuantity()).isEqualTo(10);
	}
}
