package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatesHavingSameStaffsVehicle {
	private LocalDate startDate;
	private LocalDate endDate;
	private List<UserIDName> drivers;
	private List<UserIDName> attendants;
	private int vehicleID;
	private String vehicleRegistrationNumber;
	private Boolean disable;
	private List<Integer> usersStartedTrip;
}
