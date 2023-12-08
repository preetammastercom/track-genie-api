package com.mastercom.dto;


import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteStopUpdate {

	String routeName;
	List<Object[]> stopIDStopOrderToBeUpdated;
	List<StopOrderDetails> stopDetailsToBeUpdated;
	List<StopOrderDetails> newStopsToBeAdded;
	List<Integer> tobeDeletedStopIDs;
	HashMap<Integer, String> scheduleNamesToBeUpdated;
}
