package com.mastercom.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeletedUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int userID;
	@Column(length=20)
	private String userFirstName;
	@Column(length=20)
	private String userMiddleName;
	@Column(length=20)
	private String userLastName;
	private long userPhoneNumber;
	private long userAlternatePhoneNumber;
	@Column(length=200)
	private String userAddress;
	private String userPhoto; 
	private String userUniqueKey; //(id given bySchool)
	private String userQRcode;
	private String userQRcodeString;
	private int userAge;
	private String userSex;
	@Column(length=10)
	private String userClass;    //Class and Section in the school
	@Column(length=80)
	private String priGuardian;
	@Column(length=80)
	private String secGuardian;
	private String drivingLicense;
	private String govId;
	@Column(length=60)
	private String email;
	@ManyToOne
	private Role role;
	private LocalDate date;
	
	
}
