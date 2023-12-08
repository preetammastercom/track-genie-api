package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.mastercom.dto.VideoURL;
import com.mastercom.service.AttendantService;

@RestController
@RequestMapping("trackgenie/attendant")
public class AttendantController {

	@Autowired
	AttendantService attendantService;

	

	@GetMapping("startVehicleScheduleByAttendant/{vehicleScheduleID}")
	public ResponseEntity<Object> startVehicleScheduleByAttendant(
			@PathVariable int vehicleScheduleID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
		return attendantService.startVehicleScheduleByAttendant(userID, vehicleScheduleID);
	}

	@PostMapping("endTripByAttendant/{tripScheduleDetailsID}")
	public ResponseEntity<Object> endTripByAttendant( @PathVariable int tripScheduleDetailsID,
			@RequestBody VideoURL videoURL) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
		return attendantService.endTripByAttendant(userID, tripScheduleDetailsID, videoURL);
	}

	@GetMapping("getVehicleSchedulesAssignedToAttendantToday")
	public ResponseEntity<Object> getVehicleSchedulesAssignedToAttendantToday() {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
		return attendantService.getVehicleSchedulesAssignedToAttendantToday(userID);
	}
	
	@GetMapping("getAttendantProfile")
	public ResponseEntity<Object> getAttendantProfile(){
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
		return attendantService.getAttendantProfile(userID);
	}
}
