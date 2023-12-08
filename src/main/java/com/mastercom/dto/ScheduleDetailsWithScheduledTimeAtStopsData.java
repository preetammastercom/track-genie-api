package com.mastercom.dto;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDetailsWithScheduledTimeAtStopsData {

private String vehicleScheduleName;
private String routeName;
private int typeOfJourney;
private List<StopSchedule> stopSchedules;
private int blockingTime;
}
