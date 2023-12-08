package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentStopRouteUpdate {

	private int onwardRouteID;
	private int onwardPickupStopID;
	private int onwardDropStopID;
	private int returnRouteID;
	private int returnPickupStopID;
	private int returnDropStopID;
	
	
	
}
