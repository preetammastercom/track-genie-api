package com.mastercom.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartEndDate {

	private LocalDate startDate;
	private LocalDate endDate;
}
