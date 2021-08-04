package com.chinjja.app.account.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
	Optional<Account> findByEmail(String email);
	Optional<Account> findTopByOrderByIdAsc();
}
