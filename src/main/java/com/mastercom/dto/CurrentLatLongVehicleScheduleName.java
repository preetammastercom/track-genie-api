package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentLatLongVehicleScheduleName {

	private int tripID;
	private String vehicleScheduleName;
	private String busCurrentLat;
	private String busCurrentLong;
	private String sourceLat;
	private String sourceLong;
	private String destLat;
	private String destLong;
	
	
}
