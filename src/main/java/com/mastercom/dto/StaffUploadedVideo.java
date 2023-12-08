package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffUploadedVideo {

	private int userID;
	private String userName;
	private String videoURL;
	private boolean adminVerifiedVideo;
	private String staffClickedTripStart;
	
	
	
}
