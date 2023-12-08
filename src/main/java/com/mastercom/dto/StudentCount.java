package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCount {
	private int totalStudents;
	private int pickedStudentsCount;
	private int missedBusCount;
	private int leaveCount;
	private int totalStudentsExcludingStudentsOnScheduldeLeave;
	private String passengerStatusMessage;
	
}
