package com.chinjja.app.util;

import static com.chinjja.app.util.TestUtils.to;
import static com.chinjja.app.util.TestUtils.toBytes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressCreateDto;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.AccountProductUpdateDto;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.ProductUpdateDto;
import com.chinjja.app.dto.SellerInfo;

public class Bridge {
	public static ResponseEntity<Seller> new_seller(MockMvc mvc, Account account, SellerInfo dto) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/sellers", account.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Seller.class);
	}
	
	public static ResponseEntity<Seller> seller(MockMvc mvc, Long id) throws Exception {
		return to(mvc.perform(get("/api/sellers/{id}", id)
				.accept(MediaType.APPLICATION_JSON))
		.andReturn(), Seller.class);
	}
	
	public static ResponseEntity<Seller[]> sellers(MockMvc mvc) throws Exception {
		return to(mvc.perform(get("/api/sellers")
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Seller[].class);
	}
	
	public static ResponseEntity<Product> new_product(MockMvc mvc, Seller seller, ProductInfo dto) throws Exception {
		return to(mvc.perform(post("/api/sellers/{id}/products", seller.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Product.class);
	}
	public static ResponseEntity<Product> product(MockMvc mvc, Long id) throws Exception {
		return to(mvc.perform(get("/api/products/{id}", id)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product.class);
	}
	
	public static ResponseEntity<Product> update(MockMvc mvc, Product product, ProductUpdateDto dto) throws Exception {
		return to(mvc.perform(patch("/api/products/{id}", product.getId())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(toBytes(dto)))
		.andReturn(), Product.class);
	}
	
	public static ResponseEntity<Product[]> products(MockMvc mvc, Seller seller) throws Exception {
		return to(mvc.perform(get("/api/sellers/{id}/products", seller.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product[].class);
	}
	
	public static ResponseEntity<Account> new_account(MockMvc mvc, AccountCreateDto dto) throws Exception {
		return to(mvc.perform(post("/api/accounts")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Account.class);
	}
	
	public static ResponseEntity<Account> account(MockMvc mvc, long id) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}", id)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Account.class);
	}
	
	public static ResponseEntity<AccountRole> add_account_role(MockMvc mvc, Account account, String role) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/roles/{role}", account.getId(), role)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), AccountRole.class);
	}
	
	public static ResponseEntity<String[]> account_roles(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/roles", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(), String[].class);
	}
	
	public static ResponseEntity<Address> new_address(MockMvc mvc, Account account, AddressCreateDto dto) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/addresses", account.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andExpect(status().isCreated())
				.andReturn(), Address.class);
	}
	
	public static ResponseEntity<Address[]> addresses(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/addresses", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Address[].class);
	}
	
	public static ResponseEntity<AccountProduct> add_to_cart(MockMvc mvc, Account account, Product product, int quantity) throws Exception {
		return to(mvc.perform(put("/api/accounts/{id}/products/{product_id}", account.getId(), product.getId())
						.param("quantity", ""+quantity)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), AccountProduct.class);
	}
	
	public static ResponseEntity<AccountProduct> update(MockMvc mvc, AccountProduct cartProduct, AccountProductUpdateDto dto) throws Exception {
		return to(mvc.perform(patch("/api/account-products/{id}", cartProduct.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), AccountProduct.class);
	}
	
	public static ResponseEntity<Void> delete(MockMvc mvc, AccountProduct entity) throws Exception {
		return to(mvc.perform(MockMvcRequestBuilders.delete("/api/account-products/{id}", entity.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Void.class);
	}
	
	public static ResponseEntity<Order> buy(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/orders", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<Order[]> orders(MockMvc mvc, Account account, Status status) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/orders", account.getId())
					.param("status", status == null ? null : status.toString())
					.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order[].class);
	}
	
	public static ResponseEntity<Order> cancel(MockMvc mvc, Order order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/cancel", order.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<Order> complete(MockMvc mvc, Order order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/complete", order.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<AccountProduct[]> products(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/products", account.getId())
				.accept(MediaType.APPLICATION_JSON))
		.andReturn(), AccountProduct[].class);
		
	}
}
