package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.mastercom.service.DriverService;

@RestController
@RequestMapping("trackgenie/driverattendant")
public class DriverAttendantController {
	@Autowired
	DriverService driverService;

	@GetMapping("getLastStopLatLongOfVehicleSchedule/{vehicleScheduleID}")
	public ResponseEntity<Object> getLastStopLatLongOfVehicleSchedule(@PathVariable int vehicleScheduleID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getLastStopLatLongOfVehicleSchedule(vehicleScheduleID, userIDHitAPI);
	}

	// getStartEndStopofVehicleSchedule

	@GetMapping("getLatLongOrderOfAllStopsOfVehicleSchedule/{vehicleScheduleID}")
	public ResponseEntity<Object> getLatLongOrderOfAllStopsOfVehicleSchedule(@PathVariable int vehicleScheduleID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStopsWithStopOrderOfVehicleSchedule(vehicleScheduleID, userIDHitAPI);
	}

	@GetMapping("getStudentDetails/{tripID}/{userID}")
	public ResponseEntity<Object> getStudentDetails(@PathVariable int tripID, @PathVariable int userID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStudentDetails(tripID, userID, userIDHitAPI);
	}

	@GetMapping("get_Stop_StudentsCount_ScheduledTime_List/{tripScheduleDetailsID}")
	public ResponseEntity<Object> getStopStudentsCountScheduledTimeList(@PathVariable int tripScheduleDetailsID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStopStudentsCountScheduledTimeList(tripScheduleDetailsID, userIDHitAPI);
	}

	@GetMapping("getStudentsStatusOfGivenStopOfGivenTrip/{tripScheduleDetailsID}/{stopID}")
	public ResponseEntity<Object> getStudentsStatusOfGivenStopOfGivenTrip(@PathVariable int tripScheduleDetailsID,
			@PathVariable int stopID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStudentsStatusOfGivenStopOfGivenTrip(tripScheduleDetailsID, stopID, userID);
	}

//	at frontend, options of status are:
//	1: picked from home
//	2: dropped to school
//	3: picked from school
//	4: dropped to home

	@GetMapping("getStudentDetailsHavingUniqueKey/{userUniqueKey}/{option}/{tripID}")
	public ResponseEntity<Object> getStudentDetailsHavingUniqueKey(@PathVariable String userUniqueKey,
			@PathVariable int option, @PathVariable int tripID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStudentDetailsHavingUniqueKey(userUniqueKey, option, tripID, userIDHitAPI);
	}

	@PostMapping("missedBus/{tripScheduleDetailsID}/{userID}")
	public ResponseEntity<Object> missedBus(@PathVariable int tripScheduleDetailsID, @PathVariable int userID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.missedBus(tripScheduleDetailsID, userID, userIDHitAPI);
	}

//	at frontend, options of status are:
//		1: picked from home
//		2: dropped to school
//		3: picked from school
//		4: dropped to home

	@PostMapping("chooseOptionOfStatus/{tripScheduleDetailsID}/{userID}/{option}/{correctLocation}")
	public ResponseEntity<Object> chooseOptionOfStatus(@PathVariable int tripScheduleDetailsID,
			@PathVariable int userID, @PathVariable int option, @PathVariable String correctLocation) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.chooseOptionOfStatusValidateData(tripScheduleDetailsID, userID, option, correctLocation, userIDHitAPI);
	}

	@GetMapping("getDefaultOptionOfStatus/{tripScheduleDetailsID}")
	public ResponseEntity<Object> getDefaultOptionOfStatus(@PathVariable int tripScheduleDetailsID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getDefaultOptionOfStatus(tripScheduleDetailsID, userIDHitAPI);
	}

	@RequestMapping(value = "/videoUploadAPI", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> videoUploadAPI(@RequestParam("multipartFile") MultipartFile multipartFile) {
		return driverService.videoUploadAPI(multipartFile);
	}

	@GetMapping("getStudentDetailsHavingQRcodeString/{userQRcodeString}/{option}/{tripID}")
	public ResponseEntity<Object> getStudentDetailsHavingQRcodeString(@PathVariable String userQRcodeString,
			@PathVariable int option, @PathVariable int tripID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.getStudentDetailsHavingQRcodeString(userQRcodeString, option, tripID, userIDHitAPI);
	}

	@GetMapping("endTripProcessContinue/{tripScheduleDetailsID}/{roleID}")
	public ResponseEntity<Object> endTripProcessContinue(@PathVariable int tripScheduleDetailsID,
			@PathVariable int roleID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.endTripProcessContinue(tripScheduleDetailsID, userID, roleID);
	}

	@GetMapping("enableAllPickedButton/{tripID}")
	public ResponseEntity<Object> enableAllPickedButton(@PathVariable int tripID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.enableAllPickedButton(tripID, userIDHitAPI);
	}

	@GetMapping("driverAttendantHomescreen/{tripDetailsID}")
	public ResponseEntity<Object> driverAttendantHomescreen(@PathVariable int tripDetailsID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.driverAttendantHomescreen(tripDetailsID, userIDHitAPI);
	}
	
	@GetMapping("incrementDirectionAPICount/{tripID}")
	public ResponseEntity<Object> incrementDirectionAPICount(@PathVariable int tripID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return driverService.incrementDirectionAPICount(tripID, userIDHitAPI);
	}
}
