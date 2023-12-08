package com.mastercom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.mastercom.embeddableclasses.TripUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PassengerStatus {

	@EmbeddedId
	private TripUser tripUser=new TripUser();
	@ManyToOne
	private UserStatusCode userStatusCode;
	private LocalDateTime passengerPickedUpTime;
	private LocalDateTime passengerDropTime;
	private int typeOfJourney; // 1 or 2: 1 onward, 2-return
	private LocalDateTime updatedTime; // for missedbus and scheduled leave
	

	


	

	
	

}
