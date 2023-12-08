package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentIDNameAddress {

	
	private int userID;
	private String userName;
	private String userUniqueKey;
	private String userAddress;
	private String userPhoto;
	private String latitude;
	private String longitude;
	
	
	
	
}
