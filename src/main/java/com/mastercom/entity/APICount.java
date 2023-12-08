package com.mastercom.entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class APICount {

	@Id
	@OneToOne
	private TripDetails tripDetails;
	private long updateCurrentLatLongAndTimeToReachAtStopAPICount;
	private long directionAPICount;
}
