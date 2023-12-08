package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminProfile {
	private String name;
	private int userAge;
	private String schoolName;
	private long userPhoneNumber;
	private long userAlternatePhoneNumber;
	private String email;
	private String userAddress;
	private String userPhoto;



	

}
