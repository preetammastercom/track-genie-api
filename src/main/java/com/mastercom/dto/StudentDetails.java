package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetails {

	private String userName;
	private long userPhoneNumber;
	private long userAlternatePhoneNumber;
	private String userAddress;
	private String userPhoto; 
	private String userUniqueKey; 
	private String userSex;
	private String userClass; 
	private String priGuardian;
	private String secGuardian;
	
	
	
	
	
}
