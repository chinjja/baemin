package com.chinjja.app.util;

import static com.chinjja.app.util.TestUtils.to;
import static com.chinjja.app.util.TestUtils.toBytes;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;
import com.chinjja.app.account.Address;
import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressInfo;
import com.chinjja.app.domain.AccountProduct;
import com.chinjja.app.domain.Order;
import com.chinjja.app.domain.Order.Status;
import com.chinjja.app.domain.Product;
import com.chinjja.app.domain.Seller;
import com.chinjja.app.dto.AccountProductInfo;
import com.chinjja.app.dto.ProductInfo;
import com.chinjja.app.dto.SellerInfo;

public class Bridge {
	public static ResponseEntity<Seller> new_seller(MockMvc mvc, Account account, SellerInfo dto) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/sellers", account.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Seller.class);
	}
	
	public static ResponseEntity<Seller> update(MockMvc mvc, ResponseEntity<Seller> seller, SellerInfo dto) throws Exception {
		return to(mvc.perform(patch("/api/sellers/{id}", seller.getBody().getId())
				.accept(MediaType.APPLICATION_JSON)
				.header("If-Match", seller.getHeaders().getETag())
				.contentType(MediaType.APPLICATION_JSON)
				.content(toBytes(dto)))
		.andReturn(), Seller.class);
	}
	
	public static ResponseEntity<Seller[]> sellers(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/sellers", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Seller[].class);
	}
	
	public static ResponseEntity<Seller> seller(MockMvc mvc, Seller seller) throws Exception {
		return to(mvc.perform(get("/api/sellers/{id}", seller.getId())
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
	public static ResponseEntity<Product> product(MockMvc mvc, Product product) throws Exception {
		return to(mvc.perform(get("/api/products/{id}", product.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Product.class);
	}
	
	public static ResponseEntity<Product> update(MockMvc mvc, ResponseEntity<Product> product, ProductInfo dto) throws Exception {
		return to(mvc.perform(patch("/api/products/{id}", product.getBody().getId())
				.accept(MediaType.APPLICATION_JSON)
				.header("If-Match", product.getHeaders().getETag())
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
	
	public static ResponseEntity<Account> account(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}", account.getId())
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
				.andReturn(), String[].class);
	}
	
	public static ResponseEntity<Address> new_address(MockMvc mvc, Account account, AddressInfo dto) throws Exception {
		return to(mvc.perform(post("/api/accounts/{id}/addresses", account.getId())
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Address.class);
	}
	
	public static ResponseEntity<Address> update_address(MockMvc mvc, ResponseEntity<Address> address, AddressInfo dto) throws Exception {
		return to(mvc.perform(patch("/api/addresses/{id}", address.getBody().getId())
						.accept(MediaType.APPLICATION_JSON)
						.header("If-Match", address.getHeaders().getETag())
						.contentType(MediaType.APPLICATION_JSON)
						.content(toBytes(dto)))
				.andReturn(), Address.class);
	}
	
	public static ResponseEntity<Address> address(MockMvc mvc, Address address) throws Exception {
		return to(mvc.perform(get("/api/adresses/{id}", address.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Address.class);
	}
	
	public static ResponseEntity<Address[]> addresses(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/addresses", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Address[].class);
	}
	
	public static ResponseEntity<Address> master_address(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/addresses/master", account.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Address.class);
	}
	
	public static ResponseEntity<AccountProduct> add_to_cart(MockMvc mvc, Account account, Product product, int quantity) throws Exception {
		return to(mvc.perform(put("/api/accounts/{id}/products/{product_id}", account.getId(), product.getId())
						.param("quantity", ""+quantity)
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), AccountProduct.class);
	}
	
	public static ResponseEntity<AccountProduct> account_product(MockMvc mvc, AccountProduct entity) throws Exception {
		return to(mvc.perform(get("/api/account-products/{id}", entity.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), AccountProduct.class);
	}
	
	public static ResponseEntity<AccountProduct> update(MockMvc mvc, ResponseEntity<AccountProduct> cartProduct, AccountProductInfo dto) throws Exception {
		return to(mvc.perform(patch("/api/account-products/{id}", cartProduct.getBody().getId())
						.accept(MediaType.APPLICATION_JSON)
						.header("If-Match", cartProduct.getHeaders().getETag())
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
	
	public static ResponseEntity<Order> order(MockMvc mvc, Order order) throws Exception {
		return to(mvc.perform(get("/api/orders/{id}", order.getId())
						.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<Order[]> orders(MockMvc mvc, Account account, Status status) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/orders", account.getId())
					.param("status", status == null ? null : status.toString())
					.accept(MediaType.APPLICATION_JSON))
				.andReturn(), Order[].class);
	}
	
	public static ResponseEntity<Order> cancel(MockMvc mvc, ResponseEntity<Order> order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/cancel", order.getBody().getId())
						.accept(MediaType.APPLICATION_JSON)
						.header("If-Match", order.getHeaders().getETag()))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<Order> complete(MockMvc mvc, ResponseEntity<Order> order) throws Exception {
		return to(mvc.perform(patch("/api/orders/{id}/complete", order.getBody().getId())
						.accept(MediaType.APPLICATION_JSON)
						.header("If-Match", order.getHeaders().getETag()))
				.andReturn(), Order.class);
	}
	
	public static ResponseEntity<AccountProduct[]> products(MockMvc mvc, Account account) throws Exception {
		return to(mvc.perform(get("/api/accounts/{id}/products", account.getId())
				.accept(MediaType.APPLICATION_JSON))
		.andReturn(), AccountProduct[].class);
		
	}
}
