package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripStaff {

	private String staffUniqueKey;
	private String staffName;
	private String staffLoginTime;
	private String staffVerifiedTime;
	private String adminVerifiedTime;
	private String staffVerifiedVideo;
	
}
