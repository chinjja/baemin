package com.chinjja.app.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import com.chinjja.app.account.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@NotNull
	private Account seller;
	
	@PositiveOrZero
	@NotNull
	private BigDecimal price;
	
	@NotBlank
	private String code;
	
	@NotBlank
	private String title;
	
	@NotBlank
	private String description;
}
