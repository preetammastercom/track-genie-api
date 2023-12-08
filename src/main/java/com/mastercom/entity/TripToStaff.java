package com.mastercom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.mastercom.embeddableclasses.TripStaffID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TripToStaff {

	@EmbeddedId
	private TripStaffID tripStaffID=new TripStaffID();
	@ManyToOne
	private Role staffType;
	private LocalDateTime staffLoginTime;
	private LocalDateTime staffVerifiedTime;
	private String staffVerifiedVideo;
	private LocalDateTime adminVerifiedTime;
	

	
	
}
