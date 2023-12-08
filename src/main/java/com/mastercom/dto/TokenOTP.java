package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenOTP {

	private String token;
	private int otp;
	
	
	
	
}
