package com.mastercom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Stop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer stopID;
	private String stopName;
	private String stopAddress;
	@Column(length=20)
	private String stopLatitude;
	@Column(length=20)
	private String stopLongitude;
	
	
	

}
