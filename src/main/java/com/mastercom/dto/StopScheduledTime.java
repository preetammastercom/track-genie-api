package com.mastercom.dto;

import java.time.LocalTime;

import com.mastercom.entity.Stop;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopScheduledTime {
	private int stopOrder;
	private Stop stop;
	private LocalTime scheduledArrivalTime;
	private LocalTime scheduledDepartureTime;
	private String status;
	private String location;
	
	
}
