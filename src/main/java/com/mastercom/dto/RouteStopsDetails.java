package com.mastercom.dto;

import java.util.List;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteStopsDetails {

	
	private int routeID;
	private String routeName;
	private List<StopDetailsWithStopOrder> stopsWithStopOrder;
	
}
