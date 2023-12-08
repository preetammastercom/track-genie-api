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
public class TripDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer tripDetailsID;
	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	private String vehicleScheduleName;
	private LocalDateTime busReachedDestination;
	private LocalDateTime tripStart;
	

}
