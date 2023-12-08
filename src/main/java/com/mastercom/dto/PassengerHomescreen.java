package com.mastercom.dto;

import java.time.LocalTime;
import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerHomescreen {

	private List<StopScheduledTime> list;
	private boolean showCompleteRouteLine;
	private LocalTime scheduledTripStartTimeIfTripNotStarted;
	private String childOnboardStatus;
	
}
