package com.mastercom.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveRouteData {
	
	private int tripDetailsID;
	private String routeName;
	private LocalDateTime lastUpdatedTime;
	private StudentCount studentCount;
	private List<StaffUploadedVideo> drivers;
	private List<StaffUploadedVideo> attendants;
	private List<String> driversNotLoggedin;
	private List<String> attendantsNotLoggedin;
	private String vehicleRegisterationNumber;
	private String tripStartTime;
	
	
	
	
}
