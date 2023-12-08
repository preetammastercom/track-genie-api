package com.mastercom.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteDetails {

	private int routeID;
	private String routeName;
	private String startLocation;
	private long stopsCount;
	
}
