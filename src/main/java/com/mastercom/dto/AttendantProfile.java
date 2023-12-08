package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendantProfile {
	private String name;
	private long userPhoneNumber;
	private long userAlternatePhoneNumber;
	private String email;
	private String userPhoto;
	
}
