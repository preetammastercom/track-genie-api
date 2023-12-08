package com.mastercom.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWithDateTime {

	private int id;
	private String subject;
	private String content;
	private String dateTime;
	
	
}
