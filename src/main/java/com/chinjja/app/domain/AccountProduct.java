package com.chinjja.app.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import com.chinjja.app.account.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "product_id"}))
public class AccountProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@NotNull
	private Account account;
	
	@ManyToOne
	@NotNull
	private Product product;
	
	@PositiveOrZero
	private int quantity;
	
	@Version
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private Long version;
}
