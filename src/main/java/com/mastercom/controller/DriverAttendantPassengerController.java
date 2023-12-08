package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.mastercom.dto.Password;
import com.mastercom.service.DriverService;

@RestController
@RequestMapping("trackgenie/driverattendantpassenger")
public class DriverAttendantPassengerController {

	@Autowired
	private DriverService driverService;
	
	// TODO : storing encoded password remaining
	@PutMapping("setPassword")
	public ResponseEntity<Object> setPassword(@RequestBody Password password) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
		return driverService.setPassword(userID,password);

	}
	
}
