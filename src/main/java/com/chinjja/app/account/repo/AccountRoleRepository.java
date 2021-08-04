package com.chinjja.app.account.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.AccountRole;

public interface AccountRoleRepository extends CrudRepository<AccountRole, Long> {
	Iterable<AccountRole> findAllByAccount(Account account);
	Optional<AccountRole> findByAccountAndRole(Account account, String role);
	void deleteByAccountAndRole(Account account, String role);
}
