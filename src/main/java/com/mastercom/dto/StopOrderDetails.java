package com.mastercom.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopOrderDetails {

	private Integer stopID;
	private String stopName;
	private String stopAddress;
	private String stopLatitude;
	private String stopLongitude;
	private int stopOrder;
	
}
