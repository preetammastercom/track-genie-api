package com.mastercom.dto;

import com.mastercom.entity.Stop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopDataWithOrder {

private Stop stop;
	private int stopOrder;
}
