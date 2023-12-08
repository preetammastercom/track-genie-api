package com.mastercom.dto;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffVehicleDates {
	//@NotNull
	private LocalDate startDate;
	//@NotNull
	private LocalDate endDate;
	//@NotNull
	//@Size(min=1, message="Minimum 1 driver must be assigned.")
	private Set<Integer> driverIDs;
	//@NotNull
	private Set<Integer> attendantIDs;
	//@NotNull
	private Integer vehicleDetailsID;
	
}
