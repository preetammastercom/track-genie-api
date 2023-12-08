package com.mastercom.dto;

import com.mastercom.entity.Route;
import com.mastercom.entity.Stop;
import com.mastercom.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentRouteStop {

	private User user;
	private Stop pickUpStop;
	private Route pickUpRoute;
	private Stop dropStop;
	private Route dropRoute;
	
}
