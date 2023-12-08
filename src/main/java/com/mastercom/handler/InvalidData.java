package com.mastercom.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



public class InvalidData {

	private static final Logger logger = LogManager.getLogger(InvalidData.class);
	
	private static final String INV_VEH_SCH="Invalid Vehicle Schedule ID";
	private static final String INV_VEHICLE="Invalid Vehicle ID";
	private static final String INV_ROUTE="Invalid Route ID";
	private static final String INV_STOP="Invalid Stop ID";
	private static final String INV_USER="Invalid User ID";
	private static final String INV_TRIP="Invalid Trip ID";
	private static final String INV_PASSENGER="Invalid Passenger ID";
	private static final String INV_JOURNEY="Invalid Type Of Journey";
	private static final String INV_ROLE="Invalid Role ID";
	private InvalidData() {
		
	}
	
	public static ResponseEntity<Object> invalidVehicleScheduleID1() {
		logger.error(INV_VEH_SCH);
		return ResponseHandler.generateResponse1(false, INV_VEH_SCH, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidVehicleScheduleID2() {
		logger.error(INV_VEH_SCH);
		return ResponseHandler.generateResponse2(false, INV_VEH_SCH, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidRouteID1() {
		logger.error(INV_ROUTE);
		String output = INV_ROUTE;
		return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidRouteID2() {
		logger.error(INV_ROUTE);
		String output = INV_ROUTE;
		return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidStopID1() {
		logger.error(INV_STOP);
		String output = INV_STOP;
		return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidStopID2() {
		logger.error(INV_STOP);
		String output = INV_STOP;
		return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidVehicleID1() {
		logger.error(INV_VEHICLE);
		String output = INV_VEHICLE;
		return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidVehicleID2() {
		logger.error(INV_VEHICLE);
		String output = INV_VEHICLE;
		return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidUserID1() {
		logger.error(INV_USER);
		String output = INV_USER;
		return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidUserID2() {
		logger.error(INV_USER);
		String output = INV_USER;
		return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidTripID1() {
		logger.error(INV_TRIP);
		String output = INV_TRIP;
		return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidTripID2() {
		logger.error(INV_TRIP);
		String output = INV_TRIP;
		return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidPassengerID1() {
		logger.error(INV_PASSENGER);
		return ResponseHandler.generateResponse1(false, "Invalid Student ID", HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidPassengerID2() {
		logger.error(INV_PASSENGER);
		return ResponseHandler.generateResponse2(false, "Invalid Student ID", HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidTypeOfJourney1() {
		logger.error(INV_JOURNEY);
		return ResponseHandler.generateResponse1(false, INV_JOURNEY, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidTypeOfJourney2() {
		logger.error(INV_JOURNEY);
		return ResponseHandler.generateResponse2(false, INV_JOURNEY, HttpStatus.NOT_FOUND);
	}
	
	public static ResponseEntity<Object> invalidRoleID1() {
		logger.error(INV_ROLE);
		return ResponseHandler.generateResponse1(false, INV_ROLE, HttpStatus.NOT_FOUND, null);
	}
	
	public static ResponseEntity<Object> invalidRoleID2() {
		logger.error(INV_ROLE);
		return ResponseHandler.generateResponse2(false, INV_ROLE, HttpStatus.NOT_FOUND);
	}
	
	
}
