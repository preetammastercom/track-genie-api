package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResourcesAssignmentUpdation {
	//@NotNull
	private List<ResourcesAssignmentEntry> entries;
	//@NotNull
	private Boolean trueIfMonthwiseAndfalseIfDaterangewise;
	//@Min(value=1, message="Month value must be between 1 and 12.")
	//@Max(value=12, message="Month value must be between 1 and 12.")
	private Integer month;
	private Integer year;
private LocalDate oldStartDate;
	private LocalDate oldEndDate;
	
	
}
