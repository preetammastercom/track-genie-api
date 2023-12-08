package com.mastercom.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopIdNameOrder {

	private int stopID;
	private String stopName;
	private int stopOrder;
	
}
