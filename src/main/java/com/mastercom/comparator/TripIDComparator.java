package com.mastercom.comparator;

import java.util.Comparator;

import com.mastercom.entity.TripDetails;

public class TripIDComparator implements Comparator<TripDetails>{

	@Override
	public int compare(TripDetails o1, TripDetails o2) {
		if(o1.getTripDetailsID() < o2.getTripDetailsID()) {
			return (-1);
		}
		else if(o1.getTripDetailsID() > o2.getTripDetailsID()) {
			return 1;
		}
		else {
			return 0;
		}
	}

}
