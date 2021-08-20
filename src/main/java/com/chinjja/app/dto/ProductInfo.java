package com.chinjja.app.dto;

import java.math.BigDecimal;

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
	private BigDecimal price;
	private int quantity;
	private String code;
	private String title;
	private String description;
}
