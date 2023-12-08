package com.mastercom.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInformation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long fileID;
	private String fileName;
	private String fileURL;
	private int hitCount;
	
}
