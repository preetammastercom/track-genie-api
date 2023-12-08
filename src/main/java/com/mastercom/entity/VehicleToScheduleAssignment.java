package com.mastercom.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mastercom.embeddableclasses.ScheduleDateID;
import com.mastercom.embeddableclasses.TripStaffID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VehicleToScheduleAssignment {

	@EmbeddedId
	private ScheduleDateID scheduleDateID=new ScheduleDateID();
	
	@ManyToOne
	private VehicleDetails vehicleDetails;
	
	
	
	
	 
}
