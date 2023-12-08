package com.mastercom.dto.jwtDTO;

import lombok.Data;

@Data
public class AuthRequest {
	//private String mobileNumber;
	//private String otp;
	//private String authType;
	private String deviceID;
	private String userUniqueKey; // this is now unique in User table
	private int roleID;
	private String password;
	private String fcmToken;
	
}
