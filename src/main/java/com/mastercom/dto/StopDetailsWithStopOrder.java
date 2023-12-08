package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopDetailsWithStopOrder {

	private String stopName;
	private String stopAddress;
	private String stopLatitude;
	private String stopLongitude;
	private int stopOrder;

	
}
