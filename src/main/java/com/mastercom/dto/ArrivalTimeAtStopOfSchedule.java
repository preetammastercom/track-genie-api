package com.mastercom.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrivalTimeAtStopOfSchedule {


	
	
	private int scheduleID;
	private String scheduleName;
	private int typeOfJourney;
	private int stopID;
	private int stopOrder;
	private LocalTime scheduledArrivalTime;
	private int blockingTimeInMinutes;
	
}
