package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerDTO {

	private String userFirstName;
	private String userMiddleName;
	private String userLastName;
	private long userPhoneNumber;
	private long userAlternatePhoneNumber;
	private String userAddress;
	private String userPhoto; 
	private String userUniqueKey; //(id given bySchool)
	private int userAge;
	private String userSex;
	private String userClass;    //Class and Section in the school
	private String priGuardian;
	private String secGuardian;
	private String email;
	
}
