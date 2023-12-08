package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayData {

	private String date;
	private String scheduledLeaveReturnJourney;
	private String missed_Bus_Return_Journey;
	private String reached_Home;
	private String picked_up_from_school;
	private String scheduled_Leave_Onward_Journey;
	private String missed_Bus_Onward_Journey;
	private String reached_School;
	private String picked_Up_from_home;
	
}
