package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteWithSourceDestination {

	private int routeID;
	private String routeName;
	private String source;
	private String destination;
	
	
}
