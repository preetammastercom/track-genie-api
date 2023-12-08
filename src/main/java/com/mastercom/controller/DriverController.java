package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.mastercom.dto.CurrentLatLongAndTimeRequiredToReachAtOtherStopsJSON;
import com.mastercom.dto.VideoURL;
import com.mastercom.service.DriverService;

@RestController
@RequestMapping("trackgenie/driver")
public class DriverController {

	@Autowired
	DriverService driverService;

	@GetMapping("startVehicleScheduleByDriver/{vehicleScheduleID}")
	public ResponseEntity<Object> startVehicleScheduleByDriver(@PathVariable int vehicleScheduleID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.startVehicleScheduleByDriver(userID, vehicleScheduleID);
	}

	@GetMapping("busReachedDestination/{tripID}")
	public ResponseEntity<Object> busReachedDestination(@PathVariable int tripID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.busReachedDestination(tripID, userIDHitAPI);
	}

	@PostMapping("endTripByDriver/{tripScheduleDetailsID}")
	public ResponseEntity<Object> endTripByDriver(@PathVariable int tripScheduleDetailsID,
			@RequestBody VideoURL videoURL) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.endTripByDriver(userID, tripScheduleDetailsID, videoURL);
	}

	@GetMapping("getVehicleSchedulesAssignedToDriverToday")
	public ResponseEntity<Object> getVehicleSchedulesAssignedToDriverToday() {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getVehicleSchedulesAssignedToDriverToday(userID);
	}

	// commented on 4july , as was not using this api in UI
//	@GetMapping("getStartEndStopofVehicleSchedule/{vehicleScheduleID}")
//	public ResponseEntity<Object> getStartEndStopofVehicleSchedule(@PathVariable int vehicleScheduleID){
//		return driverService.getStartEndStopofVehicleSchedule(vehicleScheduleID);
//	}

	@PutMapping("updateCurrentLatLongAndTimeRequiredToReachAtOtherStops")
	public ResponseEntity<Object> updateCurrentLatLongAndTimeRequiredToReachAtOtherStops(
			@RequestBody CurrentLatLongAndTimeRequiredToReachAtOtherStopsJSON details) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.updateCurrentLatLongAndTimeRequiredToReachAtOtherStops(details, userIDHitAPI);
	}

//	at frontend, options of status are:
//	1: picked from home
//	2: dropped to school
//	3: picked from school
//	4: dropped to home

	@GetMapping("getDriverProfile")
	public ResponseEntity<Object> getDriverProfile() {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getDriverProfile(userID);
	}

}
