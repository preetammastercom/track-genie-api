package com.mastercom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class School {
	@Id
	private Integer schoolID;
	@Column(length=50)
	private String schoolName;
	@Column(length=250)
	private String schoolAddr;
	@Column(length=50)
	private String schoolPrimaryContactName;
	private long schoolPhoneNum;
	@Column(length=50)
	private String schoolEmailID;
	
}
