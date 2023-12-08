package com.mastercom.embeddableclasses;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import com.mastercom.entity.User;
import com.mastercom.entity.VehicleSchedule;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class VehicleScheduleDateStaffID implements Serializable{

	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	@ManyToOne
	private User staff;
	private LocalDate date;
	
	
	
}
