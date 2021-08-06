package com.chinjja.app.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.chinjja.app.account.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class Order {
	public enum Status {
		IN_PROGRESS,
		CANCELLED,
		COMPLETED,
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	@Builder.Default
	private Status status = Status.IN_PROGRESS;
	
	@NotNull
	private Date createdAt;
	
	@ManyToOne
	@NotNull
	private Account account;
}
