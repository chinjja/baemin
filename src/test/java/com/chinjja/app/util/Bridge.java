package com.chinjja.app.util;

import static com.chinjja.app.util.TestUtils.to;
import static com.chinjja.app.util.TestUtils.toBytes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.domain.CartProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.dto.ProductCreateDto;

public class Bridge {
	public static Product new_product(MockMvc mvc, Account seller, ProductCreateDto dto) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/products", seller.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Product.class);
	}
	public static Product product(MockMvc mvc, Long id) throws Exception {
		return to(mvc.perform(get("/api/products/{id}", id)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product.class);
	}
	
	public static Product[] products(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/products", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product[].class);
	}
	
	public static Product product_plus_quantity(MockMvc mvc, Product product, int quantity) throws Exception {
		return to(mvc.perform(patch("/api/products/{id}/quantity", product.getId())
						.param("quantity", String.valueOf(quantity))
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product.class);
	}
	
	public static Account new_account(MockMvc mvc, AccountCreateDto dto) throws Exception {
		return to(mvc.perform(post("/api/accounts")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Account.class);
	}
	
	public static Account account(MockMvc mvc, long id) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}", id)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Account.class);
	}
	
	public static Address[] addresses(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/addresses", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Address[].class);
	}
	
	public static CartProduct addToCart(MockMvc mvc, Account account, Product product, int quantity) throws Exception {
		return to(mvc.perform(put("/api/accounts/{id}/products/{product_id}", account.getId(), product.getId())
						.param("quantity", ""+quantity)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), CartProduct.class);
	}
	
	public static Order buy(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/orders", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static Order[] orders(MockMvc mvc, Account account, Status status) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/orders", account.getId())
					.param("status", status == null ? null : status.toString())
					.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order[].class);
	}
	
	public static Order cancel(MockMvc mvc, Order order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/cancel", order.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static Order complete(MockMvc mvc, Order order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/complete", order.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
}
