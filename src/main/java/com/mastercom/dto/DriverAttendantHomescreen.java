package com.mastercom.dto;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverAttendantHomescreen {

	private int pickedCount;
	private int missedBusCount;
	private int totalCountExcludingStudentsOnScheduledLeave;
	private String passengerStatusMessage;
	private int totalStops;
	private List<StopStudentsCountScheduledTime> list;
}
