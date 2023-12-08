package com.mastercom.dto;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteStops {

	private String routeName;
	private List<StopOrderDetails> stopsWithStopOrderDetails;
	
	
}
