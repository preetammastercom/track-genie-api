package com.mastercom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripStaffDetails {

	private String staffUniqueKey;
	private String staffName;
	private String staffLoginTime;
	private String staffVerifiedTime;
	private String adminVerifiedTime;
}
