package com.mastercom.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resources {

	private List<Integer> driverIDs;
	private List<Integer> attendantIDs;
	private int vehicleID;
}
