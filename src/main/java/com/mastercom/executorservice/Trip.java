package com.mastercom.executorservice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mastercom.dao.DriverDao;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.UserToken;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;

public class Trip {

	DriverDao driverDao;

	FirebaseMessagingService firebaseService;

	private static final Logger logger = LogManager.getLogger(Trip.class);

	private int tripID;
	
	

	public Trip(DriverDao driverDao, FirebaseMessagingService firebaseService, int tripID) {
		super();
		this.driverDao = driverDao;
		this.firebaseService = firebaseService;
		this.tripID = tripID;
	}

	public void checkTripVerifiedAndNotify() {
		List<TripToStaff> list = driverDao.getTripToStaffData(tripID);
		List<Integer> userIDsNotVerifiedTrip = list.stream().filter(obj -> obj.getStaffVerifiedTime() == null)
				.map(obj1 -> obj1.getTripStaffID().getStaff().getUserID()).collect(Collectors.toList());
		String subject = "Trip Not Verified";
		String content = "Please verify bus of Trip '"
				+ list.get(0).getTripStaffID().getTripDetails().getVehicleScheduleName() + "'";
		Note note = new Note(subject, content);
		for (Integer userID : userIDsNotVerifiedTrip) {
			UserToken userToken = driverDao.getUserToken(userID);
			if (userToken != null) {
				firebaseService.sendNotification(note, userToken.getToken());
			}
		}
		if (!userIDsNotVerifiedTrip.isEmpty()) {
			Trip tripExecutorService = new Trip(driverDao, firebaseService, tripID);
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

			executorService.schedule(tripExecutorService::checkTripVerifiedAndNotify, 10, TimeUnit.MINUTES);
			executorService.shutdown();
		}
	}

}
