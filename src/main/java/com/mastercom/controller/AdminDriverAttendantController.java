package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.mastercom.service.AdminService;

@CrossOrigin
@RestController
@RequestMapping("trackgenie/admindriverattendant")
public class AdminDriverAttendantController {
	@Autowired
	AdminService adminService;
	@GetMapping("getStudentStatusListOfGivenTrip/{tripScheduleDetailsID}")
	ResponseEntity<Object> getStudentStatusListOfGivenTrip(@PathVariable int tripScheduleDetailsID) {
		Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
				RequestAttributes.SCOPE_REQUEST);
		return adminService.getStudentStatusListOfGivenTrip(tripScheduleDetailsID, userID);
	}
}
