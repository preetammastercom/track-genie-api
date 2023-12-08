package com.mastercom.entity;

import java.time.LocalDate;

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
public class PlannedCancelJourney {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer plannedCancelJourneyID;
	@ManyToOne
	private User user;
	@ManyToOne
	private Route route;
	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	private LocalDate date;
	private int typeOfJourney;
	

}
