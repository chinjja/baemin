package com.chinjja.app.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@With
public class ProductCreateDto {
	@PositiveOrZero
	@NotNull
	BigDecimal price;
	
	@NotBlank
	String code;
	
	@NotBlank
	String title;
	
	@NotBlank
	String description;
}
