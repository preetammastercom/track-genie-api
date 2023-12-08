package com.mastercom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetailsOfTrip {

	private String name;
	private String uniqueKey;
	private String pickupDateTime;
	private String dropDateTime;
	private String missedBusDateTime;
	private String status;
}
