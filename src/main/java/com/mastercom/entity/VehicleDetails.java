package com.mastercom.entity;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VehicleDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer vehicleID;
	@Column(length=20)
	private String registerationNumber;
	@Column(length=20)
	private String vehicleType;
	private String vehicleInsurance;
	private String RCbook;
	private LocalDate expiryOfInsurance;
	private LocalDate expiryOfFC;
	private LocalDate purchasedDate;
	private int noOfSeats;
		
	
	
}
