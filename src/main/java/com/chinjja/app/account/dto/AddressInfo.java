package com.chinjja.app.account.dto;

import javax.validation.constraints.NotBlank;

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
public class AddressInfo {
	@NotBlank
	private String city;
	
	@NotBlank
	private String street;
}
