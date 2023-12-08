package com.mastercom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StartStopTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@ManyToOne
	private Stop stop;
	@ManyToOne
	private Route route;
	private LocalDateTime actualArrivalTime; 
	private LocalDateTime actualDepartureTime;
	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	@ManyToOne
	private TripDetails tripDetails;
	
	
}
