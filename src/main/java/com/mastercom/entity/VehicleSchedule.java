package com.mastercom.entity;


import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VehicleSchedule{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer vehicleScheduleID;
	@Column(unique = true)
	private String vehicleScheduleName;
//	@ManyToOne
//	private VehicleDetails vehicleDetails;
	@ManyToOne
	private Route route;
	
	private LocalTime scheduledDepartureTime;  
	
	private LocalTime scheduledArrivalTime; 
	private int typeOfJourney;
	private int blockingTimeInMinutes;
	
}
