package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentIDNameStatus {

	private int userID;
	private String userName;
	private String userUniqueKey;
	private String userStatus;
	private String userPhoto; 
	private String pickedTime;
	private String droppedTime;
	private String missedBusTime;
	
}
