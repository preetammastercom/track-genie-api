package com.mastercom.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripData {

	private int vehicleScheduleID;
	private String vehicleScheduleName;
	private int routeID;
	private String routeName;
	private int pickedPassengersCount;
	private int totalCount;
	private int missedBusCount;
	private int leaveCount;
	private int totalPassengersExcludingLeave;
	private String vehicleRegistrationNumber;
	private String tripStartTime;
	private List<String> driversLoggedin;
	private List<String> attendantsLoggedin;
	private List<String> driversNotLoggedin;
	private List<String> attendantsNotLoggedin;
	private String busCurrentLat;
	private String busCurrentLong;
	private String passengerStatusMessage;
	
	
}
