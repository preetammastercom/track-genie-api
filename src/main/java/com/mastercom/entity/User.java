package com.mastercom.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;
@Data
@Entity
//@SQLDelete(sql = "UPDATE User SET user_deletion_date_time = CURRENT_TIMESTAMP WHERE userid=?")
@Where(clause = "user_deletion_date_time is NULL")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userID;
	@Column(length=20)
	private String userFirstName;
	@Column(length=20)
	private String userMiddleName;
	@Column(length=20)
	private String userLastName;
	private Long userPhoneNumber;
	private Long userAlternatePhoneNumber;
	@Column(length=200)
	private String userAddress;
	private String userPhoto; 
	@Column(unique = true)
	private String userUniqueKey; //(id given bySchool)
	
	private String userQRcode;
	@JsonIgnore
	@Column(unique = true)
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
	@JsonIgnore
	private Integer otp;
	@Column(length=60)
	private String email;
	@JsonIgnore
	private LocalDateTime userDeletionDateTime;
	@JsonIgnore
	private LocalDateTime otpGenerationDateTime;
	@JsonIgnore
	private String deviceID;
	@JsonIgnore
	private String password;
	@JsonIgnore
	private String tempPassword;
	@JsonIgnore
	private LocalDateTime tempPasswordCreationTimeStamp;


	// @JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	private List<Role> roles;
	@JsonIgnore
	private String jwtToken;
	//@Where(clause = "deletion_date_time is NULL")
	//list<RoleDate> roles;


	



	
}
