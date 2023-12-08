package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleScheduleNameGeneration {

	private int routeID;
	private int typeOfJourney;
	private String scheduledDepartureTime;
	
}
