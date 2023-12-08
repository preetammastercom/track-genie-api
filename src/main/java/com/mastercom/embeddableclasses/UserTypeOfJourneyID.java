package com.mastercom.embeddableclasses;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import com.mastercom.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserTypeOfJourneyID implements Serializable{
	@ManyToOne
	private User user;
	private int typeOfJourney;   //1 or 2: 1 onward, 2-return
	
	
}
