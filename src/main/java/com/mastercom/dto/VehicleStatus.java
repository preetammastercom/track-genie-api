package com.mastercom.dto;

import com.mastercom.entity.VehicleDetails;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleStatus {

	private VehicleDetails vehicleDetails;
	private String status;
	
	
}
