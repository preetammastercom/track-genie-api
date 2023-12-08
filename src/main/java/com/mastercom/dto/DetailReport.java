package com.mastercom.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailReport {

	private int routeID;
	private String routeName;
	private String vehicleScheduleName;
	private List<TripStaffDetails> drivers;
	private List<TripStaffDetails> attendants;
	private String tripStartTime;
	private String tripDestinationTime;
	private List<PassengerDetailsOfTrip> passengers;
}
