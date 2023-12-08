package com.mastercom.entity;



import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.mastercom.embeddableclasses.VehicleScheduleDateStaffID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StaffToVehicleScheduleMultiStaff {
	
	@EmbeddedId
	private VehicleScheduleDateStaffID vehicleScheduleDateStaffID=new VehicleScheduleDateStaffID();
	@ManyToOne
	private Role staffType;
		

}
