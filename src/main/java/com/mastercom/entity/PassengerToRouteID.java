package com.mastercom.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;


import com.mastercom.embeddableclasses.UserTypeOfJourneyID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PassengerToRouteID {
	@EmbeddedId
	private UserTypeOfJourneyID userTypeOfJourneyID=new UserTypeOfJourneyID();
	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	@ManyToOne
	private Route route;
	@ManyToOne
	private Stop pickupPointStop;
	@ManyToOne
	private Stop dropPointStop;
		
	
	
	
	
}
