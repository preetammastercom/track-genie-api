package com.mastercom.controller;

import com.mastercom.dto.jwtDTO.AuthRequest;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;
import com.mastercom.service.AdminService;
import com.mastercom.service.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("trackgenie/public")
public class PublicController {

    private static int hitCount = 0;

    @Autowired
    AdminService adminService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    FirebaseMessagingService firebaseService;
    private static final Logger logger = LogManager.getLogger(PublicController.class);

    // TODO : commented as not required now...may be required in future
    //@PostMapping("validateUserForMobile/{roleID}/{userUniqueKey}/{userPhoneNumber}")
//	public ResponseEntity<Object> validateUserForMobile(@PathVariable int roleID, @PathVariable String userUniqueKey,
//			@PathVariable long userPhoneNumber) {
//		return adminService.isUserHavingRoleUniqueKeyMobileNumber(roleID, userUniqueKey, userPhoneNumber);
//
//	}

    // TODO : commented as not required now...may be required in future
//	@PostMapping("generateOTP")
//	public ResponseEntity<Object> generateOTP(@RequestBody GenerateOTPReq request) {
//		return authenticationService.triggerOTP(request);
//	}

    // TODO : comparing with  encoded password remaining
    @PostMapping("authenticate")
    public ResponseEntity<Object> authenticate(@RequestBody AuthRequest request) {
        return authenticationService.authenticateRequest(request);
    }

    // TODO: later on remove this api
    @PostMapping("send-notification/{token}")
    public String sendNotification(@RequestBody Note note, @PathVariable String token) {
        firebaseService.sendNotification(note, token);
        return "async call, so can't predict";
    }

    // TODO: later on remove this api
    @PostMapping("subscribeToTopic/{fcmToken}")
    public void subscribeToWorldTopic(@PathVariable String fcmToken) {
        firebaseService.subscribeToTopic("A3", fcmToken);
    }

    // TODO: later on remove this api
    @PutMapping("unsubscribeToTopic/{fcmToken}")
    public void unsubscribeToWorldTopic(@PathVariable String fcmToken) {
        firebaseService.unsubscribeFromTopic("Track_Genie_ScheduleId_7", fcmToken);
    }

    // TODO: later on remove this api
    @PostMapping("sendMessageToWorldTopic")
    public void sendMessageToWorldTopic() {
        firebaseService.sendMessageToTopic("A3", "Testing", "Testing");

    }

    // TODO: later on remove this api
    @GetMapping("testAPI2")
    public String testAPI2() {
        return "Hello world";
    }

    // TODO: later on remove this api
    @GetMapping("/videoplayback")
    public ResponseEntity<Object> videoplayback(@RequestParam String v) {
        return adminService.videoplayback(v);
    }


}
