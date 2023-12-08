package com.mastercom.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserStatusCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer statusID;
	private String status; 
	//1: PickedUpFromSource, 2: ReachedDestination  3: MissedBus    4: ScheduledLeave

	
}
