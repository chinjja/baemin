package com.chinjja.app.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import com.chinjja.app.account.service.AccountService;
import com.chinjja.app.util.Bridge;

import lombok.val;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class AccountTests {
	@Autowired
	MockMvc mvc;
	
	@Autowired
	AccountService accountService;
	
	Account account;
	
	@BeforeEach
	void setup() throws Exception {
		account = accountService.findByEmail("root@user.com");
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
	void test_address_unauth() throws Exception {
		val dto = AddressInfo.builder()
				.city("chang")
				.street("60-1")
				.build();
		val address = Bridge.new_address(mvc, account, dto);
		
		assertThat(address.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
		assertEquals(account.withPassword(null), address.getBody().getAccount());
		assertEquals("chang", address.getBody().getCity());
		assertEquals("60-1", address.getBody().getStreet());
		
		val addresses = Bridge.addresses(mvc, account);
		assertThat(addresses.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(addresses.getBody()).hasSize(1).contains(address.getBody());
		
		val updated = Bridge.update_address(mvc, address.getBody(), AddressInfo.builder()
				.city("hello")
				.build());
		assertEquals(account.withPassword(null), updated.getBody().getAccount());
		assertEquals("hello", updated.getBody().getCity());
		assertEquals("60-1", updated.getBody().getStreet());
	}
	
	@Nested
	class CreatedFourAddress {
		Address addr1;
		Address addr2;
		Address addr3;
		Address addr4;
		
		@BeforeEach
		void setup() throws Exception {
			val dto = AddressInfo.builder()
					.city("chang")
					.build();
			addr1 = accountService.createAddress(account, dto.withStreet("60-1"));
			addr2 = accountService.createAddress(account, dto.withStreet("60-2"));
			addr3 = accountService.createAddress(account, dto.withStreet("60-3"));
			addr4 = accountService.createAddress(account, dto.withStreet("60-4"));
		}
		
		@Test
		@WithMockUser("root@user.com")
		void list() throws Exception {
			val list = Bridge.addresses(mvc, account);
			assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(list.getBody()).hasSize(4).contains(addr1, addr2, addr3, addr4);
		}
		
		@Test
		void list_unauth() throws Exception {
			val list = Bridge.addresses(mvc, account);
			assertThat(list.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
		
		@Test
		@WithMockUser("root@user.com")
		void noMasterAddress() throws Exception {
			val master = Bridge.master_address(mvc, account);
			assertThat(master.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		}
		
		@Test
		void noMasterAddressUnauth() throws Exception {
			val master = Bridge.master_address(mvc, account);
			assertThat(master.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
		
		@Nested
		class ModifiedOneAddressToMaster {
			@BeforeEach
			void setup() throws Exception {
				accountService.updateAddress(addr1, AddressInfo.builder()
						.master(true)
						.build());
			}
			
			@Test
			@WithMockUser("root@user.com")
			void masterAddress() throws Exception {
				val master = Bridge.master_address(mvc, account);
				assertThat(master.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(master.getBody()).isEqualTo(addr1.withMaster(true));
			}
			
			@Test
			@WithMockUser("root@user.com")
			void masterAddress2() throws Exception {
				val updated = Bridge.update_address(mvc, addr2, AddressInfo.builder()
						.master(true)
						.build());
				val master = Bridge.master_address(mvc, account);
				assertThat(master.getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(master.getBody()).isEqualTo(updated.getBody());
			}
		}
	}
}
