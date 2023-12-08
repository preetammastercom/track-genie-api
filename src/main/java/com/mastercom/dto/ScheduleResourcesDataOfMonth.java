package com.mastercom.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResourcesDataOfMonth {
	private int scheduleID;
	private String scheduleName;
	private String routeName;
	private List<DateRangeDataInScheduleList> dateRangesList;
	private boolean showMissingDatesStatus;
	private boolean showStopsTimeMissingStatus;
}
