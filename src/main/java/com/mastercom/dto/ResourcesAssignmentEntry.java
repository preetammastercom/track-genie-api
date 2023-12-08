package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourcesAssignmentEntry {

	//@NotNull
	private LocalDate startDate;
	//@NotNull
	private LocalDate endDate;
	//@NotNull
	private Integer vehicleID;
	//@NotNull
	//@Size(min=1, message="Minumum 1 driver must be assigned.")
	private List<Integer> driverIDs;
	//@NotNull
	private List<Integer> attendantIDs;
}
