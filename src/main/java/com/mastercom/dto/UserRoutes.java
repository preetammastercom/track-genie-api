package com.mastercom.dto;


import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRoutes {
	private Integer userID;
	private Set<Integer> routes;
	private String userFirstName;
	private String userMiddleName;
	private String userLastName;
	private Long userPhoneNumber;
	private Long userAlternatePhoneNumber;
	private String userAddress;
	private String userPhoto;
	private String userUniqueKey; // (id given bySchool)
	private int userAge;
	private String userSex;
	private String drivingLicense;
	private String govId;
	private String email;

}
