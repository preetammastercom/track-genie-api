package com.mastercom.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRangeDataInScheduleList {
	private LocalDate startDate;
	private LocalDate endDate;
	private List<UserIDName> drivers;
	private List<UserIDName> attendants;
	private Integer vehicleID;
	private String vehicleRegistrationNumber;
	private Boolean disable;
	private List<Integer> usersStartedTrip;
	private Boolean showAddResourcesOption;
}
