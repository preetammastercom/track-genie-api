package com.mastercom.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer roleID;
	@Column(length=20)
	private String roleName; // Admin, Teacher, Parents/Student, Driver and Attendant
	
	@JsonIgnore
	@ManyToMany(mappedBy = "roles",fetch = FetchType.EAGER)
	private List<User> users;

	
}
