package com.mastercom.controller;

import static com.mastercom.constant.ApplicationConstant.USER_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


import com.mastercom.service.AdminService;
import com.mastercom.service.AuthenticationService;

@CrossOrigin
@RestController
@RequestMapping("trackgenie/common")
public class CommonController {

	@Autowired
	AdminService adminService;
	
	 @Autowired
	    AuthenticationService authenticationService;
	 
	
	
	@GetMapping("/download/{f}")
	public ResponseEntity<Object> downloadFile(@PathVariable String f) {
		return adminService.downloadFile(f);

	}
	
	 @DeleteMapping("/logout")
	    public ResponseEntity<Object> logout() {
		 Integer userID = (Integer) RequestContextHolder.currentRequestAttributes()
                 .getAttribute(USER_ID, RequestAttributes.SCOPE_REQUEST);
	      return authenticationService.logout(userID);
	        
	    }
	 
	

	  
	    
}
