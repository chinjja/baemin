package com.chinjja.app.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.chinjja.app.util.TestUtils.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.chinjja.app.account.dto.AccountCreateDto;

import lombok.val;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class AccountTests {
	@Autowired
	MockMvc mvc;
	
	@Test
	void whenAfterInitialize_thenShouldExistAdminUser() throws Exception {
		val account = to(mvc.perform(get("/api/accounts/{id}", 1)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(), Account.class);
		
		assertThat(account.getId()).isEqualTo(1);
		assertThat(account.getEmail()).isEqualTo("root@user.com");
		assertThat(account.getPassword()).isNull();
		
		val roles = to(mvc.perform(get("/api/accounts/{id}/roles", 1)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(), String[].class);
		
		assertThat(roles).hasSize(2).contains("USER", "ADMIN");
	}
	
	@Test
	void givenNoAdmin_whenAddRole_thenShouldFail() throws Exception {
		mvc.perform(post("/api/accounts/{id}/roles/{role}", 1, "MANAGER")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	@WithMockUser(roles = "ADMIN")
	void givenAdmin_whenAddRole_thenShouldSuccess() throws Exception {
		mvc.perform(post("/api/accounts/{id}/roles/{role}", 1, "MANAGER")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated());
		
		val roles = to(mvc.perform(get("/api/accounts/{id}/roles", 1)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn(), String[].class);
		
		assertThat(roles).contains("MANAGER");
	}
	
	@Test
	void create() throws Exception {
		val dto = AccountCreateDto.builder()
				.email("user@user.com")
				.password("12345678")
				.name("user")
				.build();
		val account = to(mvc.perform(post("/api/accounts")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(toBytes(dto)))
				.andExpect(status().isCreated())
				.andReturn(), Account.class);
		
		assertThat(account.getEmail()).isEqualTo("user@user.com");
		assertThat(account.getPassword()).isNull();
		assertThat(account.getName()).isEqualTo("user");
		
		mvc.perform(post("/api/accounts")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(toBytes(dto)))
				.andExpect(status().isConflict());
	}
}
