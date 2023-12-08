package com.mastercom.embeddableclasses;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import com.mastercom.entity.VehicleSchedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ScheduleDateID  implements Serializable{

	@ManyToOne
	private VehicleSchedule vehicleSchedule;
	private LocalDate date;
}
