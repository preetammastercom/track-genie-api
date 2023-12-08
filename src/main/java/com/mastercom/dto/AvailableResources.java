package com.mastercom.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableResources {

	private List<UserIDName> availableDrivers;
	private List<UserIDName> availableAttendants;
	private List<VehicleIDRegisterationNumberSeatCapacity> availableVehicles;
	
}
