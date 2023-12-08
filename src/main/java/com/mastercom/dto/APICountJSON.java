package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APICountJSON {
	private Long updateCurrentLatLongAndTimeToReachAtStopAPICount;
	private Long directionAPICount;
}
