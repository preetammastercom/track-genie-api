package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.mastercom.dto.FileURL;
import com.mastercom.dto.PassengerIDScheduledLeaveDateTypeOfJourney;
import com.mastercom.service.PassengerService;

@RestController
@RequestMapping("trackgenie/passenger")
public class PassengerController {

	@Autowired
	PassengerService passengerService;

	@PutMapping("updateProfilePictureOfPassenger")
	public ResponseEntity<Object> updateProfilePictureOfPassenger(@RequestBody FileURL fileURL) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.updateProfilePictureOfPassenger(userID, fileURL);

	}

	@GetMapping("getBusInfo/{vehicleScheduleID}")
	public ResponseEntity<Object> getBusInfo(@PathVariable int vehicleScheduleID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getBusInfo(userID, vehicleScheduleID);
	}

	@GetMapping("getSchoolContact")
	public ResponseEntity<Object> getSchoolContact() {
		return passengerService.getSchoolContact();
	}

	@PostMapping("scheduleLeave")
	public ResponseEntity<Object> scheduleLeave(
			@RequestBody PassengerIDScheduledLeaveDateTypeOfJourney passengerID_ScheduledLeaveDate_TypeOfJourney) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.scheduleLeaveNew(userID, passengerID_ScheduledLeaveDate_TypeOfJourney);
	}

	@GetMapping("getVehicleScheduleIDAndTripIDofStudentCorrespondingToGivenTypeOfJourney/{typeOfJourney}")
	public ResponseEntity<Object> getVehicleScheduleIDAndTripIDofStudentCorrespondingToGivenTypeOfJourney(
			@PathVariable int typeOfJourney) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getVehicleScheduleIDAndTripIDofStudentCorrespondingToGivenTypeOfJourney(userID,
				typeOfJourney, LocalDate.now());
	}

	@GetMapping("getSheduledLeaveDates/{typeOfJourney}")
	public ResponseEntity<Object> getScheduledLeaveDates(@PathVariable int typeOfJourney) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getScheduledLeaveDates(userID, typeOfJourney);
	}

	@DeleteMapping("cancelScheduledLeave/{typeOfJourney}/{date}")
	public ResponseEntity<Object> cancelScheduledLeave(@PathVariable int typeOfJourney, @PathVariable String date) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.cancelScheduledLeave2(userID, typeOfJourney, date);
	}

	@RequestMapping(value = { "getTimeRequiredToReachAtUserStop/{vehicleScheduleID}",
			"getTimeRequiredToReachAtUserStop/{vehicleScheduleID}/{tripScheduleDetailsID}" }, method = RequestMethod.GET)
	public ResponseEntity<Object> getTimeRequiredToReachAtUserStop(@PathVariable int vehicleScheduleID,
			@PathVariable(required = false) Integer tripScheduleDetailsID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getTimeRequiredToReachAtUserStop(userID, vehicleScheduleID, tripScheduleDetailsID);
	}

	@RequestMapping(value = { "getBusCurrentLatLong/{vehicleScheduleID}",
			"getBusCurrentLatLong/{vehicleScheduleID}/{tripScheduleDetailsID}" }, method = RequestMethod.GET)
	public ResponseEntity<Object> getBusCurrentLatLong(@PathVariable int vehicleScheduleID,
			@PathVariable(required = false) Integer tripScheduleDetailsID) {
		Integer userIDHitAPI = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getBusCurrentLatLong(vehicleScheduleID, tripScheduleDetailsID, userIDHitAPI);
	}

	@GetMapping("getLogOfPassengerStatus")
	public ResponseEntity<Object> getLogOfPassengerStatus() {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getLogOfPassengerStatus(userID);
	}

	@GetMapping("getLogOfPassengerStatus_ShowMore/{passValue}")
	public ResponseEntity<Object> getLogOfPassengerStatusShowMore(@PathVariable int passValue) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.getLogOfPassengerStatus_ShowMore(userID, passValue);
	}

	/*
	 * meaning of id values: 1: pickup from home 2: drop to school 3: pickup from
	 * school 4: drop to home
	 */

	// TODO : do changes in these 5 apis in second phase
	@PutMapping("enable_passengerEnteredTheBusAtHome/{check}")
	public ResponseEntity<Object> enable_passengerEnteredTheBusAtHome(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 1);
	}

	@PutMapping("enable_passengerGotDownOfTheBusAtSchool/{check}")
	public ResponseEntity<Object> enablePassengerGotDownOfTheBusAtSchool(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 2);
	}

	@PutMapping("enable_passengerEnteredTheBusAtSchool/{check}")
	public ResponseEntity<Object> enablePassengerEnteredTheBusAtSchool(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 3);
	}

	@PutMapping("enable_passengerGotDownOfTheBusAtHome/{check}")
	public ResponseEntity<Object> enablePassengerGotDownOfTheBusAtHome(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 4);
	}

	@PutMapping("enable_tripStarted/{check}")
	public ResponseEntity<Object> enable_tripStarted(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 5);
	}

	@PutMapping("enable_tripVerifiedByAdmin/{check}")
	public ResponseEntity<Object> enable_tripVerifiedByAdmin(@PathVariable boolean check) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.update_On_Off_Notification(userID, check, 6);
	}

	@RequestMapping(value = { "passengerHomescreen/{vehicleScheduleID}",
			"passengerHomescreen/{vehicleScheduleID}/{tripDetailsID}" }, method = RequestMethod.GET)
	public ResponseEntity<Object> passengerHomescreen(@PathVariable int vehicleScheduleID,
			@PathVariable(required = false) Integer tripDetailsID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return passengerService.passengerHomescreen(userID, vehicleScheduleID, tripDetailsID, LocalDate.now());
	}

}
