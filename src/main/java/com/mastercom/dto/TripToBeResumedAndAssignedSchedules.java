package com.mastercom.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripToBeResumedAndAssignedSchedules {

	
	private Integer resumeTripID;
	private List<VehicleScheduleIDNameRouteName> shedules;
	
	
}
