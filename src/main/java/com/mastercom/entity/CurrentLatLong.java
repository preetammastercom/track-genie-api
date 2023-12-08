package com.mastercom.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CurrentLatLong implements Serializable{

	@Id
	@OneToOne
	private TripDetails tripDetails;
	private String busCurrentLat;
	private String busCurrentLong;
	
	
}
