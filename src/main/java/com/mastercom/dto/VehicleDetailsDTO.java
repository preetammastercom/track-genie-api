package com.mastercom.dto;

import java.time.LocalDate;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDetailsDTO {
	
	private String registerationNumber;
	private String vehicleType;
	private String vehicleInsurance;
	private String RCbook;
	private LocalDate expiryOfInsurance;
	private LocalDate expiryOfFC;
	private LocalDate purchasedDate;
	private int noOfSeats;
	
	
	
}
