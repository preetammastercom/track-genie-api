package com.mastercom.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopSchedule {

	private int stopID;
	private String scheduledDepartureTime;
	private String scheduledArrivalTime;
	

	
}
