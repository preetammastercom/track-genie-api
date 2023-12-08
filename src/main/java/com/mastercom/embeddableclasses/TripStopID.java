package com.mastercom.embeddableclasses;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import com.mastercom.entity.Stop;
import com.mastercom.entity.TripDetails;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class TripStopID implements Serializable{

	@ManyToOne
	private TripDetails tripDetails;
	@ManyToOne
	private Stop stop;
	
	
	
	
}
