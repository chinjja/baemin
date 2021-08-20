package com.chinjja.app.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "code"}))
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@NotNull
	private Seller seller;
	
	@Version
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private Long version;
	
	@PositiveOrZero
	@NotNull
	private BigDecimal price;
	
	@PositiveOrZero
	@NotNull
	private int quantity;
	
	@NotBlank
	private String code;
	
	@NotBlank
	private String title;
	
	@NotBlank
	private String description;
}
