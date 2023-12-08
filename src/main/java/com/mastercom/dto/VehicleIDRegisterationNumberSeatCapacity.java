package com.mastercom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleIDRegisterationNumberSeatCapacity {

	private int vehicleID;
	private String registrationNumber;
	private int noOfSeats;
}
