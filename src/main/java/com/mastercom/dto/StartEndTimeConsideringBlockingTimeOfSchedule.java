package com.mastercom.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartEndTimeConsideringBlockingTimeOfSchedule {

	private String scheduleName;
	private LocalTime startTime;
	private LocalTime endTime;//considering blocking time
}
