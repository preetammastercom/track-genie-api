package com.mastercom.entity;

import java.time.LocalTime;

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
public class RouteStopSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer routeStopScheduleID;
	@ManyToOne
	private Stop stop;
	@ManyToOne
	private Route route;
	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	private LocalTime scheduledDepartureTime;
	private LocalTime scheduledArrivalTime;
	
	
}
