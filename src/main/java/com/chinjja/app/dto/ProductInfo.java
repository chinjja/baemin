package com.chinjja.app.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class ProductInfo {
	@PositiveOrZero
	@NotNull
	BigDecimal price;
	
	@PositiveOrZero
	@NotNull
	long quantity;
	
	@NotBlank
	String code;
	
	@NotBlank
	String title;
	
	@NotBlank
	String description;
}
