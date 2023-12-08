package com.mastercom.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;


import com.mastercom.embeddableclasses.TripStopID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TimeRequiredToReachAtStop {

	
	@EmbeddedId
	private TripStopID tripStopID=new TripStopID();
	private String time;
	
	
}
