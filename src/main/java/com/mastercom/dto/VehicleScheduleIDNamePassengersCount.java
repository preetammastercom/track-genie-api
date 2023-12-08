package com.mastercom.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleScheduleIDNamePassengersCount {

	private int scheduleID;
	private String scheduleName;
	private long passengersCount;
	
	
	
}
