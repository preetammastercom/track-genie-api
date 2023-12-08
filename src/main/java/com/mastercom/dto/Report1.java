package com.mastercom.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report1 {

	private int routeID;
	private String routeName;
	private int vehicleScheduleID;
	private String vehicleScheduleName;
	private List<TripStaff> drivers;
	private List<TripStaff> attendants;
	private String tripStartTime;
	private String tripDestinationTime;

	

	
}
