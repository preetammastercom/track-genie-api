package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import com.mastercom.entity.User;
import com.mastercom.entity.VehicleDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDateStaffsVehicle {

	private int scheduleID;
	private String scheduleName;
	private String routeName;
	private LocalDate date;
	private List<User> drivers;
	private List<User> attendants;
	private VehicleDetails vehicleDetails;
}
