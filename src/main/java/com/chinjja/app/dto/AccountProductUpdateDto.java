package com.chinjja.app.dto;

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
public class AccountProductUpdateDto {
	@PositiveOrZero
	private Integer quantity;
}
