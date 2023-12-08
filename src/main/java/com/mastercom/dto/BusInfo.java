package com.mastercom.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusInfo {

	private int routeID;
	private String routeName;
	private String vehicleRegisterationNumber;
	private String vehicleType;
	private List<UserNamePhoneNum> drivers;
	private List<UserNamePhoneNum> attendants;
	
	
	
	
}
