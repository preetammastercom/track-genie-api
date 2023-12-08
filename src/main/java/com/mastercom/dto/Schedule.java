package com.mastercom.dto;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule {

	private String vehicleScheduleName;
	private int typeOfJourney;
	private int routeID;
	private List<StopSchedule> stopSchedules;
	private int blockingTimeInMinutes;
	
}
