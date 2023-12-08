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
public class NotificationOnOff implements Serializable{

	@Id
	@OneToOne
	private User user;
	//   1/0     1:ON, 0: OFF
	private int passengerEnteredTheBusAtHome;
	private int passengerGotDownOfTheBusAtSchool;
	private int passengerEnteredTheBusAtSchool;
	private int passengerGotDownOfTheBusAtHome;
	private int missedBus;
	private int tripStarted;
	private int tripVerifiedByAdmin;
	
	
	
}
