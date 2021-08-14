package com.chinjja.app.account;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	private String city;
	
	@NotBlank
	private String street;
	
	@ManyToOne
	@NotNull
	private Account account;
	
	private boolean master;
	
	@Version
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private Long version;
}
