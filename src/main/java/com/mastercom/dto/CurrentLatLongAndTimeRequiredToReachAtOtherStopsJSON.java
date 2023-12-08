package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentLatLongAndTimeRequiredToReachAtOtherStopsJSON {

	
	private int tripID;
	private int stopID;
	private String time;
	private String busCurrentLat;
	private String busCurrentLong;
	
	
	
}
