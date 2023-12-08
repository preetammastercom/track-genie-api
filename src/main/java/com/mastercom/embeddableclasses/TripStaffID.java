package com.mastercom.embeddableclasses;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import com.mastercom.entity.TripDetails;
import com.mastercom.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class TripStaffID implements Serializable{

	@ManyToOne
	private TripDetails tripDetails;
	@ManyToOne
	private User staff;
	
	
	
}
