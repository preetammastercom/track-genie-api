package com.mastercom.handler;


import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

	//with data
	public static ResponseEntity<Object> generateResponse1(boolean responseStatus, String responseMessage, HttpStatus status, Object data) {
	   Map<String, Object> map=new LinkedHashMap<>();
	   map.put("responseStatus",responseStatus);
	   map.put("responseMessage", responseMessage);
	   map.put("data", data);
	    return new ResponseEntity<>(map, status);
	}
	
	//without data
	public static ResponseEntity<Object> generateResponse2(boolean responseStatus, String responseMessage, HttpStatus status) {
		   Map<String, Object> map=new LinkedHashMap<>();
		   map.put("responseStatus",responseStatus);
		   map.put("responseMessage", responseMessage);
		    return new ResponseEntity<>(map, status);
		}
	
	public static ResponseEntity<Object> generateResponse3(boolean responseStatus, String responseMessage, HttpStatus status,Object data, String tripStatus, LocalTime scheduledTripStartTimeIfTripNotStarted) {
		   Map<String, Object> map=new LinkedHashMap<>();
		   map.put("responseStatus",responseStatus);
		   map.put("responseMessage", responseMessage);
		   map.put("tripStatus", tripStatus);
		   map.put("scheduledTripStartTimeIfTripNotStarted", scheduledTripStartTimeIfTripNotStarted);
		   map.put("data", data);
		    return new ResponseEntity<>(map, status);
		}
	
	public static ResponseEntity<Object> generateResponse4(boolean responseStatus, String responseMessage, HttpStatus status,Object data,Integer passValue) {
		   Map<String, Object> map=new LinkedHashMap<>();
		   map.put("responseStatus",responseStatus);
		   map.put("responseMessage", responseMessage);
		   map.put("data", data);
		   map.put("passValue", passValue);
		    return new ResponseEntity<>(map, status);
		}
	
	public static ResponseEntity<Object> generateResponse5(boolean responseStatus, String responseMessage, HttpStatus status, Object data, Object data2) {
		   Map<String, Object> map=new LinkedHashMap<>();
		   map.put("responseStatus",responseStatus);
		   map.put("responseMessage", responseMessage);
		   map.put("data", data);
		   map.put("data2", data2);
		    return new ResponseEntity<>(map, status);
		}
}
