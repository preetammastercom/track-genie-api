package com.mastercom.fcm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class FCMRestController {

	@Autowired
	FirebaseMessagingService firebaseService;
	
	
	//commented on 4th july, as its not required
	
	
	
}
