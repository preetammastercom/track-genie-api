package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import com.mastercom.entity.User;
import com.mastercom.entity.VehicleDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateStaffsVehicle {

	private LocalDate date;
	private List<User> drivers;
	private List<User> attendants;
	private VehicleDetails vehicleDetails;
}
