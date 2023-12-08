package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesAssignmentChangesToBeDone {

	private List<LocalDate> deleteDates;
	private List<IDDate> addDrivers;
	private List<IDDate> addAttendants;
	private List<IDDate> addVehicles;
	private List<IDDate> disassociateDrivers;
	private List<IDDate> disassociateAttendants;
private List<IDDate> disassociateVehicles;
private List<Integer> userIDs;
private List<Integer> vehicleIDs;
}
