package com.chinjja.app.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.chinjja.app.account.dto.AccountCreateDto;
import com.chinjja.app.account.dto.AddressInfo;
import com.chinjja.app.util.Bridge;

import lombok.val;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class AccountTests {
	@Autowired
	MockMvc mvc;
	
	Account account;
	
	@BeforeEach
	void setup() throws Exception {
		account = Bridge.account(mvc, 1).getBody();
	}
	@Test
	void whenAfterInitialize_thenShouldExistAdminUser() throws Exception {
		val account = Bridge.account(mvc, 1);
		
		assertThat(account.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(account.getBody().getId()).isEqualTo(1);
		assertThat(account.getBody().getEmail()).isEqualTo("root@user.com");
		assertThat(account.getBody().getPassword()).isNull();
	}
	
	@Test
	void initRoles() throws Exception {
		val roles = Bridge.account_roles(mvc, account);
		
		assertThat(roles.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(roles.getBody()).hasSize(2).contains("USER", "ADMIN");
	}
	
	@Test
	void givenNoAdmin_whenAddRole_thenShouldFail() throws Exception {
		val role = Bridge.add_account_role(mvc, account, "MANAGER");
		assertThat(role.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	@WithMockUser(roles = "ADMIN")
	void givenAdmin_whenAddRole_thenShouldSuccess() throws Exception {
		val role = Bridge.add_account_role(mvc, account, "MANAGER");
		assertThat(role.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(role.getBody().getRole()).isEqualTo("MANAGER");
		
		val roles = Bridge.account_roles(mvc, account);
		assertThat(roles.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(roles.getBody()).contains("MANAGER");
	}
	
	@Test
	void create() throws Exception {
		val dto = AccountCreateDto.builder()
				.email("user@user.com")
				.password("12345678")
				.name("user")
				.build();
		val account = Bridge.new_account(mvc, dto);
		assertThat(account.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(account.getBody().getEmail()).isEqualTo("user@user.com");
		assertThat(account.getBody().getPassword()).isNull();
		assertThat(account.getBody().getName()).isEqualTo("user");
		
		val conflict = Bridge.new_account(mvc, dto);
		assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}
	
	@Test
	@WithMockUser("root@user.com")
	void test_address() throws Exception {
		val dto = AddressInfo.builder()
				.city("chang")
				.street("60-1")
				.build();
		val address = Bridge.new_address(mvc, account, dto);
		
		assertThat(address.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertEquals(account, address.getBody().getAccount());
		assertEquals("chang", address.getBody().getInfo().getCity());
		assertEquals("60-1", address.getBody().getInfo().getStreet());
		
		val addresses = Bridge.addresses(mvc, account);
		assertThat(addresses.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(addresses.getBody()).hasSize(1).contains(address.getBody());
	}
}
