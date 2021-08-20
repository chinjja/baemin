package com.chinjja.app.account.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.chinjja.app.account.Account;
import com.chinjja.app.account.Address;

public interface AddressRepository extends CrudRepository<Address, Long> {
	Iterable<Address> findAllByAccount(Account account);
	Optional<Address> findTopByAccountAndMasterIsTrue(Account account);
	Iterable<Address> findByAccountAndMasterIsTrue(Account account);
}
