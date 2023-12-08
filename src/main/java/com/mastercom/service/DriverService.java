package com.mastercom.service;

import static com.mastercom.constant.ApplicationConstant.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.mastercom.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mastercom.config.ConfigurableParameters;
import com.mastercom.dao.AdminDao;
import com.mastercom.dao.AttendantDao;
import com.mastercom.dao.DriverDao;
import com.mastercom.dao.PassengerDao;
import com.mastercom.embeddableclasses.TripStopID;
import com.mastercom.entity.CurrentLatLong;
import com.mastercom.entity.PassengerStatus;
import com.mastercom.entity.PassengerToRouteID;
import com.mastercom.entity.Role;
import com.mastercom.entity.RouteStop;
import com.mastercom.entity.RouteStopSchedule;
import com.mastercom.entity.Stop;
import com.mastercom.entity.TimeRequiredToReachAtStop;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.User;
import com.mastercom.entity.UserToken;
import com.mastercom.entity.VehicleSchedule;
import com.mastercom.executorservice.Trip;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;
import com.mastercom.handler.InvalidData;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.CurrentLatLongAndTimeRequiredToReachAtOtherStopsJSON;
import com.mastercom.dto.DriverAttendantHomescreen;
import com.mastercom.dto.DriverProfile;
import com.mastercom.dto.Password;
import com.mastercom.dto.StopStudentsCountScheduledTime;
import com.mastercom.dto.StudentDetails;
import com.mastercom.dto.StudentStatus;
import com.mastercom.dto.TripToBeResumedAndAssignedSchedules;
import com.mastercom.dto.VehicleScheduleIDNameRouteName;
import com.mastercom.dto.StudentIDNameAddress;
import com.mastercom.dto.VideoURL;
import com.mastercom.dto.jwtDTO.TripTypeOfJourney;

@Service
public class DriverService {
	@Autowired
	private EncryptionUtil encryptionUtil;
	@Autowired
	AdminDao adminDao;

	@Autowired
	DriverDao driverDao;

	@Autowired
	PassengerService passengerService;

	@Autowired
	FirebaseMessagingService firebaseService;

	@Autowired
	PassengerDao passengerDao;

	@Autowired
	AttendantDao attendantDao;
	@Value("${count.directionAPI}")
	private boolean directionAPICounter;

	
	
	private static final String SERVER_ERROR = "Server Error!!!";
	private static final String InBus = "In Bus";
	private static final String Picked = "Picked";

	@Autowired
	ConfigurableParameters configurableParameters;

	private static final Logger logger = LogManager.getLogger(DriverService.class);

	public ResponseEntity<Object> getStudentDetails(int tripID, int userID, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();
			}
			User user = adminDao.getUser(userID);
			if ((user != null) && (passengerDao.getVehicleScheduleIDsofPassenger(userID)
					.contains(trip.getVehicleSchedule().getVehicleScheduleID()))) {
				List<Role> roles = user.getRoles();
				for (Role role : roles) {
					if (role.getRoleID() == 3) {
						logger.debug("User is passenger.");
						StudentDetails student = new StudentDetails();
						student.setUserName(user.getUserFirstName() + " " + user.getUserMiddleName() + " "
								+ user.getUserLastName());
						student.setPriGuardian(user.getPriGuardian());
						student.setSecGuardian(user.getSecGuardian());
						student.setUserPhoneNumber(user.getUserPhoneNumber());
						student.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
						student.setUserClass(user.getUserClass());
						student.setUserSex(user.getUserSex());
						student.setUserUniqueKey(user.getUserUniqueKey());
						student.setUserPhoto(user.getUserPhoto());
						student.setUserAddress(user.getUserAddress());
						return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, student);
					}
				}
				return InvalidData.invalidUserID1();

			} else {
				return InvalidData.invalidUserID1();

			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> endTripByDriver(int userID, int tripScheduleDetailsID, VideoURL videoURL) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if (trip == null) {
				return InvalidData.invalidTripID2();

			}
			TripToStaff tripToStaff = driverDao.getTripToStaffDataOfGivenStaff(tripScheduleDetailsID, userID);
			if (tripToStaff != null) {
				logger.debug("User is a driver for a given trip");
				if (tripToStaff.getStaffVerifiedTime() != null) {
					logger.debug("Failed!!! Verification Time is already recorded.");
					String output = "Failed!!! Verification Time is already recorded.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				} else {
					if(!adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripScheduleDetailsID).isEmpty()) {
						logger.debug("Failed! Please drop all students.");
						return ResponseHandler.generateResponse2(false, "Failed! Please drop all students.", HttpStatus.OK);
					}
					boolean isSuccess = driverDao.endTripByDriver(userID, tripScheduleDetailsID, videoURL);
					if (isSuccess) {
						UserToken userToken = driverDao.getUserToken(userID);
						if (userToken != null) {
							firebaseService.unsubscribeFromScheduleTopic(
									trip.getVehicleSchedule().getVehicleScheduleID(), userToken.getToken());
						}
						firebaseService.sendRefreshMessageToAdminTopic();
						return ResponseHandler.generateResponse2(true, "Trip completed successfully!!!", HttpStatus.OK);
					} else {
						return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}

			} else {
				logger.debug("Invalid user for a given trip");
				String output = "Invalid user ID for a given trip.";
				return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public ResponseEntity<Object> getStopStudentsCountScheduledTimeList(int tripScheduleDetailsID, int userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();

			}
			int vehicleScheduleID = trip.getVehicleSchedule().getVehicleScheduleID();
			List<RouteStop> routeStopList = driverDao.getStopsWithStopOrderOfVehicleSchedule(vehicleScheduleID);
			logger.debug("Stops with stop order are fetched of vehicle schedule");
			List<StopStudentsCountScheduledTime> list = new ArrayList<>();
			for (RouteStop routeStop : routeStopList) {
				StopStudentsCountScheduledTime s = new StopStudentsCountScheduledTime();
				Stop stop = routeStop.getStop();
				s.setStop(stop);
				s.setStopOrder(routeStop.getStopOrder());

				List<RouteStopSchedule> routeStopScheduleList = driverDao
						.getRouteStopScheduleOfGivenStop(routeStop.getStop().getStopID(), vehicleScheduleID);

				if (!routeStopScheduleList.isEmpty()) {
					// logger.debug("Arrival time and departure time are fetched for a stop");
					RouteStopSchedule routeStopSchedule = routeStopScheduleList.get(0);
					s.setScheduledArrivalTime(routeStopSchedule.getScheduledArrivalTime());
					s.setScheduledDepartureTime(routeStopSchedule.getScheduledDepartureTime());

				}

				List<Integer> userIDs = new ArrayList<>();
				if (trip.getVehicleSchedule().getTypeOfJourney() == 1) {
					userIDs.addAll(driverDao.getUserIDsOfGivenPickUpStopOfOnwardJourneyVehicleSchedule(stop.getStopID(),
							vehicleScheduleID));
				} else {
					userIDs.addAll(driverDao.getUserIDsOfGivenDropStopOfReturnJourneyVehicleSchedule(stop.getStopID(),
							vehicleScheduleID));
				}

				List<Integer> pickedUserIDs = adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
				pickedUserIDs.retainAll(userIDs);
				List<Integer> missedBusUserIDs = adminDao
						.get_missedBus_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
				missedBusUserIDs.retainAll(userIDs);
				s.setPickedStudentsCount(pickedUserIDs.size());
				s.setMissedBusStudentsCount(missedBusUserIDs.size());

				List<Integer> scheduledLeaveUserIDs = adminDao
						.get_scheduledLeave_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
				scheduledLeaveUserIDs.retainAll(userIDs);
				logger.debug(
						"Picked, missed bus, schedule leave passengers are fetched for a trip  and passengers of stop are fetched.");
				int count = userIDs.size() - scheduledLeaveUserIDs.size();
				s.setTotalStudentsCountExcludingStudentsOnScheduledLeave(count);

				list.add(s);
			}
			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Data not found.", HttpStatus.NOT_FOUND, null);
			} else {
				return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, list);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> getStudentsStatusOfGivenStopOfGivenTrip(int tripScheduleDetailsID, int stopID,
			int userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();

			}
			Stop stop = adminDao.getStop(stopID);
			if (stop == null) {
				return InvalidData.invalidStopID1();
			}

			VehicleSchedule vehicleSchedule = trip.getVehicleSchedule();
			List<Integer> stopIDs = driverDao.getStopIDsOfGivenVehicleSchedule(vehicleSchedule.getVehicleScheduleID());

			if ((stopIDs.isEmpty()) || (!(stopIDs.contains(stopID)))) {
				logger.debug("Trip don't have given stop");
				String output = "Trip don't have given stop.";
				return ResponseHandler.generateResponse1(false, output, HttpStatus.OK, null);
			}

			List<Integer> userIDs = new ArrayList<>();
			if (vehicleSchedule.getTypeOfJourney() == 1) {
				userIDs.addAll(driverDao.getUserIDsOfGivenPickUpStopOfOnwardJourneyVehicleSchedule(stop.getStopID(),
						vehicleSchedule.getVehicleScheduleID()));
			} else {
				userIDs.addAll(driverDao.getUserIDsOfGivenDropStopOfReturnJourneyVehicleSchedule(stop.getStopID(),
						vehicleSchedule.getVehicleScheduleID()));
			}

			List<StudentStatus> list = new ArrayList<>();

			List<Integer> pickedUserIDs = adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
			pickedUserIDs.retainAll(userIDs);

			List<Integer> droppedUserIDs = adminDao.get_Dropped_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
			droppedUserIDs.retainAll(userIDs);

			List<Integer> missedBusUserIDs = adminDao.get_missedBus_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
			missedBusUserIDs.retainAll(userIDs);

			List<Integer> scheduledLeaveUserIDs = adminDao
					.get_scheduledLeave_StudentsIDsListOfGivenTrip(tripScheduleDetailsID);
			scheduledLeaveUserIDs.retainAll(userIDs);
			logger.debug(
					"Picked, dropped, missed bus, schedule leave passengers are fetched for a trip, and all passengers of a stop are fetched.");
			List<Integer> yetToBePickedUserIDs = new ArrayList<>();
			yetToBePickedUserIDs.addAll(userIDs);
			yetToBePickedUserIDs.removeAll(pickedUserIDs);
			yetToBePickedUserIDs.removeAll(droppedUserIDs);
			yetToBePickedUserIDs.removeAll(missedBusUserIDs);
			yetToBePickedUserIDs.removeAll(scheduledLeaveUserIDs);

			for (Integer userID : pickedUserIDs) {
				StudentStatus studentStatus = new StudentStatus();
				User user = adminDao.getUser(userID);
				studentStatus.setUserID(userID);
				studentStatus.setUserName(
						user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
				studentStatus.setUserUniqueKey(user.getUserUniqueKey());
				studentStatus.setUserAddress(user.getUserAddress());
				studentStatus.setUserPhoto(user.getUserPhoto());
				studentStatus.setUserStatus("Picked");
				list.add(studentStatus);
			}

			for (Integer userID : droppedUserIDs) {
				StudentStatus studentStatus = new StudentStatus();
				User user = adminDao.getUser(userID);
				studentStatus.setUserID(userID);
				studentStatus.setUserName(
						user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
				studentStatus.setUserUniqueKey(user.getUserUniqueKey());
				studentStatus.setUserAddress(user.getUserAddress());
				studentStatus.setUserPhoto(user.getUserPhoto());
				studentStatus.setUserStatus("Dropped");
				list.add(studentStatus);
			}

			for (Integer userID : missedBusUserIDs) {
				StudentStatus studentStatus = new StudentStatus();
				User user = adminDao.getUser(userID);
				studentStatus.setUserID(userID);
				studentStatus.setUserName(
						user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
				studentStatus.setUserUniqueKey(user.getUserUniqueKey());
				studentStatus.setUserAddress(user.getUserAddress());
				studentStatus.setUserPhoto(user.getUserPhoto());
				studentStatus.setUserStatus("Missed Bus");
				list.add(studentStatus);
			}

			for (Integer userID : scheduledLeaveUserIDs) {
				StudentStatus studentStatus = new StudentStatus();
				User user = adminDao.getUser(userID);
				studentStatus.setUserID(userID);
				studentStatus.setUserName(
						user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
				studentStatus.setUserUniqueKey(user.getUserUniqueKey());
				studentStatus.setUserAddress(user.getUserAddress());
				studentStatus.setUserPhoto(user.getUserPhoto());
				studentStatus.setUserStatus("Scheduled Leave");
				list.add(studentStatus);
			}

			for (Integer userID : yetToBePickedUserIDs) {
				StudentStatus studentStatus = new StudentStatus();
				User user = adminDao.getUser(userID);
				studentStatus.setUserID(userID);
				studentStatus.setUserName(
						user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
				studentStatus.setUserUniqueKey(user.getUserUniqueKey());
				studentStatus.setUserAddress(user.getUserAddress());
				studentStatus.setUserPhoto(user.getUserPhoto());
				studentStatus.setUserStatus("Yet to be Picked");
				list.add(studentStatus);
			}
			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Data not found.", HttpStatus.NOT_FOUND, null);
			} else {
				return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, list);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> getStudentDetailsHavingUniqueKey(String userUniqueKey, int option, int tripID,
			Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();
			}
			List<User> passengers = adminDao.getUserListOfGivenRole(3);
			List<User> passengersHavingGivenUniqueKey = passengers.stream()
					.filter(passenger -> passenger.getUserUniqueKey().equals(userUniqueKey))
					.collect(Collectors.toList());
			if (passengersHavingGivenUniqueKey.isEmpty()) {
				logger.debug("Invalid Unique Key");
				return ResponseHandler.generateResponse1(false, "Invalid Unique Key", HttpStatus.NOT_FOUND, null);
			}
			User user = passengersHavingGivenUniqueKey.get(0);
			List<PassengerToRouteID> passengerToRouteIDDetails = passengerDao
					.getPassengerToRouteDetails(user.getUserID());
			////////////
			List<Integer> vehicleScheduleIDs = passengerToRouteIDDetails.stream()
					.filter(obj -> obj.getVehicleSchedule() != null)
					.map(obj -> obj.getVehicleSchedule().getVehicleScheduleID()).collect(Collectors.toList());
			if (!(vehicleScheduleIDs.contains(trip.getVehicleSchedule().getVehicleScheduleID()))) {
				logger.debug("Wrong Student for Trip");
				return ResponseHandler.generateResponse1(false, "Wrong Student for Trip", HttpStatus.NOT_FOUND, null);
			}
			int statusID = passengerDao.childOnboardStatusOfGivenTrip(user.getUserID(), tripID);
			if (trip.getVehicleSchedule().getTypeOfJourney() == 1) {
				if (!((option == 1) || (option == 2))) {
					logger.debug("Invalid option");
					return ResponseHandler.generateResponse1(false, "Invalid option", HttpStatus.NOT_FOUND, null);
				}
				switch (statusID) {
				case 1: {
					if (option == 1) {
						logger.debug("Failed! Student is already picked.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already picked.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 2: {
					if (option == 1) {
						logger.debug("Failed! Student is dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is dropped.", HttpStatus.OK,
								null);
					} else {
						logger.debug("Failed! Student is already dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already dropped.",
								HttpStatus.OK, null);
					}
				}
				case 3: {
					if (option == 2) {
						logger.debug("Failed! Student has missed bus.");
						return ResponseHandler.generateResponse1(false, "Failed! Student has missed bus.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 4: {
					logger.debug("Failed! Student is on leave.");
					return ResponseHandler.generateResponse1(false, "Failed! Student is on leave.", HttpStatus.OK,
							null);

				}
				default: {
					if (option == 2) {
						logger.debug("Failed! Student is not picked yet, and so cannot be dropped.");
						return ResponseHandler.generateResponse1(false,
								"Failed! Student is not picked yet, and so cannot be dropped.", HttpStatus.OK, null);
					}
					break;
				}

				}
			} else {
				if (!((option == 3) || (option == 4))) {
					logger.debug("Invalid option");
					return ResponseHandler.generateResponse1(false, "Invalid option", HttpStatus.NOT_FOUND, null);
				}
				switch (statusID) {
				case 1: {
					if (option == 3) {
						logger.debug("Failed! Student is already picked.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already picked.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 2: {
					if (option == 3) {
						logger.debug("Failed! Student is dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is dropped.", HttpStatus.OK,
								null);
					} else {
						logger.debug("Failed! Student is already dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already dropped.",
								HttpStatus.OK, null);
					}
				}
				case 3: {
					if (option == 4) {
						logger.debug("Failed! Student has missed bus.");
						return ResponseHandler.generateResponse1(false, "Failed! Student has missed bus.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 4: {
					logger.debug("Failed! Student is on leave.");
					return ResponseHandler.generateResponse1(false, "Failed! Student is on leave.", HttpStatus.OK,
							null);

				}
				default: {
					if (option == 4) {
						logger.debug("Failed! Student is not picked yet, and so cannot be dropped.");
						return ResponseHandler.generateResponse1(false,
								"Failed! Student is not picked yet, and so cannot be dropped.", HttpStatus.OK, null);
					}
					break;
				}

				}
			}
			///////////////////
			logger.debug("Valid unique key of passenger.");
			StudentIDNameAddress student = new StudentIDNameAddress();
			student.setUserID(user.getUserID());
			student.setUserName(
					user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
			student.setUserUniqueKey(user.getUserUniqueKey());
			student.setUserAddress(user.getUserAddress());
			student.setUserPhoto(user.getUserPhoto());

			PassengerToRouteID onwardDetails = null;
			PassengerToRouteID returnDetails = null;
			for (PassengerToRouteID obj : passengerToRouteIDDetails) {
				if (obj.getUserTypeOfJourneyID().getTypeOfJourney() == 1) {
					onwardDetails = obj;
				}
				if (obj.getUserTypeOfJourneyID().getTypeOfJourney() == 2) {
					returnDetails = obj;
				}
			}
			switch (option) {
			case 1: {
				student.setLatitude(onwardDetails.getPickupPointStop().getStopLatitude());
				student.setLongitude(onwardDetails.getPickupPointStop().getStopLongitude());
				break;
			}
			case 2: {
				student.setLatitude(onwardDetails.getDropPointStop().getStopLatitude());
				student.setLongitude(onwardDetails.getDropPointStop().getStopLongitude());
				break;
			}
			case 3: {
				student.setLatitude(returnDetails.getPickupPointStop().getStopLatitude());
				student.setLongitude(returnDetails.getPickupPointStop().getStopLongitude());
				break;
			}
			case 4: {
				student.setLatitude(returnDetails.getDropPointStop().getStopLatitude());
				student.setLongitude(returnDetails.getDropPointStop().getStopLongitude());
				break;
			}
			default: {
				break;
			}
			}
			return ResponseHandler.generateResponse1(true, "Data Found.", HttpStatus.OK, student);

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> missedBus(int tripScheduleDetailsID, int userID, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID2();
			}
			VehicleSchedule vehicleSchedule = trip.getVehicleSchedule();
			if (!(driverDao.checkWhetherGivenStudentBelongsToGivenVehicleSchedule(userID,
					vehicleSchedule.getVehicleScheduleID()))) {
				logger.debug("Invalid UserID for a trip");
				String output = "Invalid User ID for a trip";
				return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
			}
			logger.debug("Checking whether passenger status recorded.");
			int status = driverDao.checkWhetherStudentStatusRecordedAndGetStudentStatusID(tripScheduleDetailsID,
					userID);

			if (status == 0) {
				int success = driverDao.missedBus(tripScheduleDetailsID, userID);
				if (success == 1) {
					firebaseService
							.sendRefreshMessageToScheduleTopicAndAdminTopic(vehicleSchedule.getVehicleScheduleID());
					if (vehicleSchedule.getTypeOfJourney() == 1) {
						passengerService.notifyPassengerStatus(userID, 6);
						notifyAdminAboutPassengerMissedBus(userID, 6, vehicleSchedule.getRoute().getRouteID(),
								vehicleSchedule.getRoute().getRouteName(), vehicleSchedule.getVehicleScheduleName());
					} else {
						passengerService.notifyPassengerStatus(userID, 5);
						notifyAdminAboutPassengerMissedBus(userID, 5, vehicleSchedule.getRoute().getRouteID(),
								vehicleSchedule.getRoute().getRouteName(), vehicleSchedule.getVehicleScheduleName());
					}
					String output = "Missed Bus Status is recorded.";
					return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
				} else {
					return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				HashMap<Integer, String> hm = new HashMap<>();
				hm.put(1, "Picked");
				hm.put(2, "Dropped");
				hm.put(3, "Missed Bus");
				hm.put(4, "on Scheduled Leave");
				String output = "Failed!!! Student status is already recorded as " + hm.get(status) + ".";
				return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<Object> getDefaultOptionOfStatus(int tripScheduleDetailsID, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();
			}

			List<RouteStop> routeStopList = driverDao
					.getStopsWithStopOrderOfVehicleSchedule(trip.getVehicleSchedule().getVehicleScheduleID());
			int size = routeStopList.size();
			logger.debug("Stops with stopOrder of vehicle schedule are fetched.");
			int typeOfJourney = trip.getVehicleSchedule().getTypeOfJourney();

			if (typeOfJourney == 1) {
				if ((size == 1) || (size == 0)) {
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 1);
				}
				int lastStopID = routeStopList.get(size - 1).getStop().getStopID();
				if (driverDao.checkVehicleReachedAtGivenStopOfGivenTrip(tripScheduleDetailsID, lastStopID)) {
					logger.debug("vehicle reached at last stop of onward journey.");
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 2);
				} else {
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 1);
				}
			} else {
				if ((size == 1) || (size == 0)) {
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 3);
				}
				int secondStopID = routeStopList.get(size - 2).getStop().getStopID();
				if (driverDao.checkVehicleReachedAtGivenStopOfGivenTrip(tripScheduleDetailsID, secondStopID)) {
					logger.debug("vehicle reached at second stop of return journey. ");
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 4);
				} else {
					return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, 3);
				}
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> getStartEndStopofVehicleSchedule(int vehicleScheduleID) {
		try {
			if ((adminDao.getVehicleSchedule(vehicleScheduleID)) == null) {
				return InvalidData.invalidVehicleScheduleID1();

			}
			List<RouteStop> list = driverDao.getStopsWithStopOrderOfVehicleSchedule(vehicleScheduleID);
			logger.debug("Stops of vehicleSchedule are fetched.");
			HashMap<String, Stop> data = new HashMap<>();
			int size = list.size();
			if (size == 0) {
				return ResponseHandler.generateResponse1(false, "Data not found.", HttpStatus.NOT_FOUND, null);
			} else {
				data.put("Start Stop", list.get(0).getStop());
				data.put("End Stop", list.get(size - 1).getStop());
				return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, data);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> getStopsWithStopOrderOfVehicleSchedule(int vehicleScheduleID, Integer userIDHitAPI) {

		try {
			if (((adminDao.getVehicleSchedule(vehicleScheduleID)) == null) || (!((adminDao
					.getVehicleSchedulesIDsAssignedToStaffToday(userIDHitAPI).contains(vehicleScheduleID))
					|| (driverDao.getTripsGoingOnForStaffAndNotVerifiedByThatStaff(userIDHitAPI).stream().anyMatch(
							i -> Objects.equals(i.getVehicleSchedule().getVehicleScheduleID(), vehicleScheduleID)))))) {
				return InvalidData.invalidVehicleScheduleID1();

			}
			List<RouteStop> list = driverDao.getStopsWithStopOrderOfVehicleSchedule(vehicleScheduleID);
			logger.debug("Stops of  vehicleSchedule are fetched.");
			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Data not found.", HttpStatus.NOT_FOUND, null);
			} else {
				return ResponseHandler.generateResponse1(true, "Data Found.", HttpStatus.OK, list);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> videoUploadAPI(MultipartFile multipartFile) {

		try {
			// if (!(multipartFile.getContentType().equals("video/mp4"))) {
//			return ResponseHandler.generateResponse1(false, "Only .mp4 file is allowed", HttpStatus.OK, null);
//		}
			logger.debug("Content type:" + multipartFile.getContentType());
			return adminDao.fileUpload(multipartFile);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> fetchVehicleSchedulesAssignedToDriverToday(int userID) {
		try {
			TripToBeResumedAndAssignedSchedules object = new TripToBeResumedAndAssignedSchedules();
			List<Integer> tripIDsGoingOnForDriver = driverDao.getTripIDsGoingOnForDriver(userID);
			if (tripIDsGoingOnForDriver.size() != 0) {
				object.setResumeTripID(tripIDsGoingOnForDriver.get(0));
				logger.debug("Trip needs to be resumed");
				return ResponseHandler.generateResponse1(true, "Resume Trip", HttpStatus.OK, object);
			}
			List<Integer> vehicleSchedulesIDsVerifiedByDriverToday = driverDao
					.getVehicleSchedulesIDsVerifiedByDriverToday(userID);
			List<Integer> vehicleSchedulesIDsAssignedToDriverToday = driverDao
					.getVehicleSchedulesIDsAssignedToDriverToday(userID);
			logger.debug("Today's Vehicle schedules are fetched for userID");

			vehicleSchedulesIDsAssignedToDriverToday.removeAll(vehicleSchedulesIDsVerifiedByDriverToday);
			List<VehicleScheduleIDNameRouteName> schedules = new ArrayList<>();
			for (Integer id : vehicleSchedulesIDsAssignedToDriverToday) {
				VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(id);
				schedules.add(new VehicleScheduleIDNameRouteName(vehicleSchedule.getVehicleScheduleID(),
						vehicleSchedule.getRoute().getRouteName() + " : " + vehicleSchedule.getVehicleScheduleName()));

			}
			if (schedules.size() == 0) {
				logger.debug("No schedule is assigned");
				return ResponseHandler.generateResponse1(false, "No schedule is assigned", HttpStatus.NOT_FOUND, null);
			} else {
				object.setShedules(schedules);
				logger.debug("Schedules found.");
				return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, object);
			}

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getVehicleSchedulesAssignedToDriverToday(int userID) {

		try {
			User user = adminDao.getUser(userID);
			if (user != null) {
				List<Role> roles = user.getRoles();
				for (Role role : roles) {
					if (role.getRoleID() == 4) {
						logger.debug("Fetching VehicleSchedules assigned to driver today.");
						return fetchVehicleSchedulesAssignedToDriverToday(userID);
					}
				}
				logger.debug("Invalid driverID");
				String output = "Invalid User ID";
				return ResponseHandler.generateResponse1(false, output, HttpStatus.NOT_FOUND, null);

			} else {
				return InvalidData.invalidUserID1();

			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> updateCurrentLatLongAndTimeRequiredToReachAtOtherStops(
			CurrentLatLongAndTimeRequiredToReachAtOtherStopsJSON details, Integer userIDHitAPI) {
		try {
			int tripID = details.getTripID();
			int stopID = details.getStopID();
			TripDetails trip = driverDao.getTripDetails(tripID);
			if ((trip == null) || (!(driverDao.getTripToStaffData(tripID).stream()
					.filter(i -> i.getStaffType().getRoleID() == DRIVER_ROLE_ID)
					.map(i -> i.getTripStaffID().getStaff().getUserID()).toList().contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID2();
			}
			Stop stop = adminDao.getStop(stopID);
			if (stop == null) {
				return InvalidData.invalidStopID2();
			}
			int routeID = trip.getVehicleSchedule().getRoute().getRouteID();
			List<Integer> stopIDs = adminDao.getStopIDOfRoute(routeID);
			if (!(stopIDs.contains(stopID))) {
				logger.debug("Invalid stopID for a trip.");
				return ResponseHandler.generateResponse2(false, "Invalid stop ID for a trip.", HttpStatus.NOT_FOUND);
			}
			TimeRequiredToReachAtStop timeRequiredToReachAtStop = new TimeRequiredToReachAtStop(
					new TripStopID(trip, stop), details.getTime());
			CurrentLatLong currentLatLong = new CurrentLatLong(trip, details.getBusCurrentLat(),
					details.getBusCurrentLong());
			logger.debug("Objects of CurrentLatLong and TimeRequiredToReachAtStop are prepared.");
			if (driverDao.updateCurrentLatLongAndTimeRequiredToReachAtOtherStops(timeRequiredToReachAtStop,
					currentLatLong)) {
				firebaseService.sendLatLongRefreshMessageToPassengerScheduleTopic(
						trip.getVehicleSchedule().getVehicleScheduleID(), details.getBusCurrentLat(),
						details.getBusCurrentLong());
				return ResponseHandler.generateResponse2(true, "Success!!!!", HttpStatus.OK);
			} else {
				return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<Object> getStudentDetailsHavingQRcodeString(String userQRcodeString, int option, int tripID,
			Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();
			}
			User user = driverDao.getStudentDetailsHavingQRcodeString(userQRcodeString);
			if (user == null) {
				logger.debug("Invalid QRcodeString for a passenger.");
				return ResponseHandler.generateResponse1(false, "Invalid QR Code", HttpStatus.NOT_FOUND, null);
			} else {
				if (!(user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList())
						.contains(3))) {
					logger.debug("Invalid QRcodeString for a passenger.");
					return ResponseHandler.generateResponse1(false, "Invalid QR Code", HttpStatus.NOT_FOUND, null);
				}
			}
			logger.debug("Valid QRcodeString for a passenger.");
			List<PassengerToRouteID> passengerToRouteIDDetails = passengerDao
					.getPassengerToRouteDetails(user.getUserID());
			List<Integer> vehicleScheduleIDs = passengerToRouteIDDetails.stream()
					.filter(obj -> obj.getVehicleSchedule() != null)
					.map(obj -> obj.getVehicleSchedule().getVehicleScheduleID()).collect(Collectors.toList());
			if (!(vehicleScheduleIDs.contains(trip.getVehicleSchedule().getVehicleScheduleID()))) {
				logger.debug("Wrong Student for Trip");
				return ResponseHandler.generateResponse1(false, "Wrong Student for Trip", HttpStatus.NOT_FOUND, null);
			}
			int statusID = passengerDao.childOnboardStatusOfGivenTrip(user.getUserID(), tripID);
			if (trip.getVehicleSchedule().getTypeOfJourney() == 1) {
				if (!((option == 1) || (option == 2))) {
					logger.debug("Invalid option");
					return ResponseHandler.generateResponse1(false, "Invalid option", HttpStatus.NOT_FOUND, null);
				}
				switch (statusID) {
				case 1: {
					if (option == 1) {
						logger.debug("Failed! Student is already picked.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already picked.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 2: {
					if (option == 1) {
						logger.debug("Failed! Student is dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is dropped.", HttpStatus.OK,
								null);
					} else {
						logger.debug("Failed! Student is already dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already dropped.",
								HttpStatus.OK, null);
					}
				}
				case 3: {
					if (option == 2) {
						logger.debug("Failed! Student has missed bus.");
						return ResponseHandler.generateResponse1(false, "Failed! Student has missed bus.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 4: {
					logger.debug("Failed! Student is on leave.");
					return ResponseHandler.generateResponse1(false, "Failed! Student is on leave.", HttpStatus.OK,
							null);

				}
				default: {
					if (option == 2) {
						logger.debug("Failed! Student is not picked yet, and so cannot be dropped.");
						return ResponseHandler.generateResponse1(false,
								"Failed! Student is not picked yet, and so cannot be dropped.", HttpStatus.OK, null);
					}
					break;
				}

				}
			} else {
				if (!((option == 3) || (option == 4))) {
					logger.debug("Invalid option");
					return ResponseHandler.generateResponse1(false, "Invalid option", HttpStatus.NOT_FOUND, null);
				}
				switch (statusID) {
				case 1: {
					if (option == 3) {
						logger.debug("Failed! Student is already picked.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already picked.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 2: {
					if (option == 3) {
						logger.debug("Failed! Student is dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is dropped.", HttpStatus.OK,
								null);
					} else {
						logger.debug("Failed! Student is already dropped.");
						return ResponseHandler.generateResponse1(false, "Failed! Student is already dropped.",
								HttpStatus.OK, null);
					}
				}
				case 3: {
					if (option == 4) {
						logger.debug("Failed! Student has missed bus.");
						return ResponseHandler.generateResponse1(false, "Failed! Student has missed bus.",
								HttpStatus.OK, null);
					}
					break;
				}
				case 4: {
					logger.debug("Failed! Student is on leave.");
					return ResponseHandler.generateResponse1(false, "Failed! Student is on leave.", HttpStatus.OK,
							null);

				}
				default: {
					if (option == 4) {
						logger.debug("Failed! Student is not picked yet, and so cannot be dropped.");
						return ResponseHandler.generateResponse1(false,
								"Failed! Student is not picked yet, and so cannot be dropped.", HttpStatus.OK, null);
					}
					break;
				}

				}
			}
			StudentIDNameAddress student = new StudentIDNameAddress();
			student.setUserID(user.getUserID());
			student.setUserName(
					user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
			student.setUserUniqueKey(user.getUserUniqueKey());
			student.setUserAddress(user.getUserAddress());
			student.setUserPhoto(user.getUserPhoto());

			PassengerToRouteID onwardDetails = null;
			PassengerToRouteID returnDetails = null;
			for (PassengerToRouteID obj : passengerToRouteIDDetails) {
				if (obj.getUserTypeOfJourneyID().getTypeOfJourney() == 1) {
					onwardDetails = obj;
				}
				if (obj.getUserTypeOfJourneyID().getTypeOfJourney() == 2) {
					returnDetails = obj;
				}
			}
			if (option == 1) {
				student.setLatitude(onwardDetails.getPickupPointStop().getStopLatitude());
				student.setLongitude(onwardDetails.getPickupPointStop().getStopLongitude());
			} else if (option == 2) {
				student.setLatitude(onwardDetails.getDropPointStop().getStopLatitude());
				student.setLongitude(onwardDetails.getDropPointStop().getStopLongitude());
			} else if (option == 3) {
				student.setLatitude(returnDetails.getPickupPointStop().getStopLatitude());
				student.setLongitude(returnDetails.getPickupPointStop().getStopLongitude());
			} else if (option == 4) {
				student.setLatitude(returnDetails.getDropPointStop().getStopLatitude());
				student.setLongitude(returnDetails.getDropPointStop().getStopLongitude());
			}
			return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, student);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> endTripProcessContinue(int tripScheduleDetailsID, int userID, int roleID) {
		try {
			if ((driverDao.getTripDetails(tripScheduleDetailsID)) == null) {
				return InvalidData.invalidTripID1();
			}
			if (!((roleID == 4) || (roleID == 5))) {
				return InvalidData.invalidRoleID1();

			}
			List<TripToStaff> list = driverDao.getTripToStaffData(tripScheduleDetailsID);
			List<TripToStaff> staffDataOfGivenUser = list.stream()
					.filter(obj -> obj.getTripStaffID().getStaff().getUserID() == userID
							&& obj.getStaffType().getRoleID() == roleID)
					.collect(Collectors.toList());
			if (staffDataOfGivenUser.isEmpty()) {
				return InvalidData.invalidUserID1();
			}
			TripToStaff staff = staffDataOfGivenUser.get(0);
			if (staff.getStaffVerifiedTime() != null) {
				logger.debug("Failed! Video and Verification time is already recorded.");
				return ResponseHandler.generateResponse1(false,
						"Failed! Video and Verification time is already recorded.", HttpStatus.OK, null);
			}

			if ((adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripScheduleDetailsID).size()) == 0) {
				logger.debug("Picked students count of trip  is 0.");
				return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, true);
			} else {
				logger.debug("Picked students count of trip is not 0.");
				return ResponseHandler.generateResponse1(true, null, HttpStatus.OK, false);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public void notifyDriverAttendant(int userID, int id, Object data) {
		UserToken userToken = driverDao.getUserToken(userID);
		if (userToken != null) {
			if (id == 1) {
				HashMap<String, String> map = (HashMap<String, String>) data;
				String subject = "Deletion of vehicle schedule";
				String content = "Vehicle schedule " + "'" + (map.get("vehicleScheduleName")) + "' of Route " + "'"
						+ map.get("routeName") + "' is deleted.";
				Note note = new Note(subject, content);
				firebaseService.sendNotification(note, userToken.getToken());
				
			}
		} else {
			logger.debug("user token is not there. Please ask user to login. FirstTimeLogin is not done.");
		}
	}

	public void sendDelayNotificationToPassengers(int vehicleScheduleID, long min) {
		String subject = "Bus Delayed.";
		int hrs = (int) (min / 60);
		int modMin = (int) (min % 60);
		String time = "";
		if (hrs != 0) {
			time = time + hrs + " hour ";
		}
		if (modMin != 0) {
			time = time + modMin + " mins";
		}
		time = time + ". ";

		String content = "Bus delayed by " + time + System.lineSeparator() + "Vehicle Schedule Name: "
				+ adminDao.getVehicleSchedule(vehicleScheduleID).getVehicleScheduleName();
		Note note = new Note(subject, content);
		 firebaseService.sendMulticastAsync(note,
				adminDao.getTokensOfVehicleSchedule(vehicleScheduleID));
		
	}

	public ResponseEntity<Object> getLastStopLatLongOfVehicleSchedule(int vehicleScheduleID, Integer userIDHitAPI) {
		try {
			VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
			if ((vehicleSchedule == null) || (!((adminDao.getVehicleSchedulesIDsAssignedToStaffToday(userIDHitAPI)
					.contains(vehicleScheduleID))
					|| (driverDao.getTripsGoingOnForStaffAndNotVerifiedByThatStaff(userIDHitAPI).stream().anyMatch(
							i -> Objects.equals(i.getVehicleSchedule().getVehicleScheduleID(), vehicleScheduleID)))))) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			List<RouteStop> list = driverDao.getStopsWithStopOrderOfVehicleSchedule(vehicleScheduleID);
			int count = list.size();
			if (count == 0) {
				logger.debug("Data not found.");
				return ResponseHandler.generateResponse1(false, "Data not found", HttpStatus.NOT_FOUND, null);
			} else {
				RouteStop routeStop = list.get(count - 1);
				Map<String, String> map = new LinkedHashMap<>();
				map.put("Latitude", routeStop.getStop().getStopLatitude());
				map.put("Longitude", routeStop.getStop().getStopLongitude());
				logger.debug("Data found.");
				return ResponseHandler.generateResponse1(true, "Data found", HttpStatus.OK, map);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> startVehicleScheduleByDriver(int userID, int vehicleScheduleID) {
		try {
			VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);

			if (vehicleSchedule == null) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			User user = adminDao.getUser(userID);
			if ((user == null)
					|| (!((user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList()))
							.contains(4)))) {
				return InvalidData.invalidUserID1();
			}
			if (!((driverDao.getVehicleSchedulesAssignedToDriverToday(userID).stream()
					.map(obj -> obj.getVehicleScheduleID()).collect(Collectors.toList()))
					.contains(vehicleScheduleID))) {
				logger.debug("Given vehicle Schedule is not assigned to Driver.");
				String output = "Given vehicle Schedule is not assigned to Driver.";
				return ResponseHandler.generateResponse1(false, output, HttpStatus.OK, null);
			}

			int tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
			List<TripToStaff> tripToStaffListOfGivenTrip = null;
			long driverCount = 0;
			if (tripID != (-1)) {
				tripToStaffListOfGivenTrip = driverDao.getTripToStaffData(tripID);
				long t = tripToStaffListOfGivenTrip.stream().filter(obj -> obj.getStaffType().getRoleID() == 4)
						.filter(obj -> obj.getTripStaffID().getStaff().getUserID() == userID).count();

				if (t == 1) {
					return ResponseHandler.generateResponse1(true, "Trip is started.", HttpStatus.OK,
							new TripTypeOfJourney(tripID, vehicleSchedule.getTypeOfJourney()));
				}
				driverCount = tripToStaffListOfGivenTrip.stream().filter(obj -> obj.getStaffType().getRoleID() == 4)
						.count();
			}
			List<Integer> allActiveTripIDs = driverDao.getAllActiveTripIDs();
			List<Integer> staffAllTripIDs = driverDao.getStaffAllTripIDs(userID);
			allActiveTripIDs.retainAll(staffAllTripIDs);

			if (!allActiveTripIDs.isEmpty()) {
				return newTripCannotBeStarted(allActiveTripIDs);
			}
			int success;
			if (tripID != (-1)) {
				success = driverDao.startVehicleScheduleByDriver(vehicleScheduleID, userID, tripID);
			} else {
				success = driverDao.startVehicleScheduleByDriver(vehicleScheduleID, userID, null);
				tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
				if (success == 1) {
					 firebaseService.sendMulticastAsync(
							new Note("Trip started.",
									"Trip of vehicle schedule '" + vehicleSchedule.getVehicleScheduleName()
											+ "' is started."),
							driverDao.getTokensOfPassengersWhoEnabledNotificationOfTripStarted(vehicleScheduleID));
					
				}
			}
			if (success != 1) {
				return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
			}
			if (driverCount == 0) {
				LocalTime departureTime = vehicleSchedule.getScheduledDepartureTime();
				LocalTime nowTime = LocalTime.now();
				if (nowTime.isAfter(departureTime)) {
					long diff = ChronoUnit.MINUTES.between(departureTime, nowTime);
					if (diff >= configurableParameters.getNotifyingPassengersAboutBusDelayed()) {

						sendDelayNotificationToPassengers(vehicleScheduleID, diff);
					}
				}
			}
			logger.debug("Trip started.");
			UserToken userToken = driverDao.getUserToken(userID);
			firebaseService.subscribeToScheduleTopic(vehicleScheduleID, userToken.getToken());

			return ResponseHandler.generateResponse1(true, "Trip is started.", HttpStatus.OK,
					new TripTypeOfJourney(tripID, vehicleSchedule.getTypeOfJourney()));
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public ResponseEntity<Object> newTripCannotBeStarted(List<Integer> allActiveTripIDs) {

		logger.debug("Failed! New Trip cannot be started, as vehicle schedule's Trip is not closed");
		StringBuilder sb = new StringBuilder(
				"Failed! New Trip cannot be started, as below vehicle schedule's Trip is not closed:");
		for (Integer activeTripID : allActiveTripIDs) {
			List<TripToStaff> tripToStaff = driverDao.getTripToStaffData(activeTripID);
			TripDetails activeTrip = driverDao.getTripDetails(activeTripID);
			List<Object[]> pendingUsers = new ArrayList<>();
			for (TripToStaff t : tripToStaff) {
				if (t.getStaffVerifiedTime() == null) {
					Object[] arr = new Object[2];
					arr[0] = t.getStaffType().getRoleID();
					arr[1] = t.getTripStaffID().getStaff();
					pendingUsers.add(arr);
				}
			}
			sb.append("    Vehicle Schedule Name:");
			sb.append(activeTrip.getVehicleSchedule().getVehicleScheduleName());
			sb.append(".    Verification pending with admin");
			if (!pendingUsers.isEmpty()) {
				sb.append(" and also with below users: ");
			} else {
				sb.append(".");
			}
			for (Object[] obj : pendingUsers) {
				User staff = (User) (obj[1]);
				if ((int) (obj[0]) == 4) {
					String driverName = staff.getUserFirstName() + " " + staff.getUserMiddleName() + " "
							+ staff.getUserLastName();
					sb.append("   Driver: ");
					sb.append(driverName);
				} else {
					String attendantName = staff.getUserFirstName() + " " + staff.getUserMiddleName() + " "
							+ staff.getUserLastName();
					sb.append("   Attendant: ");
					sb.append(attendantName);
				}
			}
		}
		return ResponseHandler.generateResponse1(false, sb.toString(), HttpStatus.OK, null);

	}

	public ResponseEntity<Object> chooseOptionOfStatusValidateData(int tripScheduleDetailsID, int userID, int option,
			String correctLocation, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID2();
			}
			VehicleSchedule vehicleSchedule = trip.getVehicleSchedule();
			if (!(driverDao.checkWhetherGivenStudentBelongsToGivenVehicleSchedule(userID,
					vehicleSchedule.getVehicleScheduleID()))) {
				logger.debug("Invalid UserID for a given trip.");
				String output = "Invalid User ID for a given trip.";
				return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
			}
			if (!((correctLocation.equals("Yes")) || (correctLocation.equals("No")))) {
				return ResponseHandler.generateResponse2(false, "Invalid location option.", HttpStatus.NOT_FOUND);
			}
			int typeOfJourney = vehicleSchedule.getTypeOfJourney();
			if (((typeOfJourney == 1) && (!((option == 1) || (option == 2))))
					|| ((typeOfJourney == 2) && (!((option == 3) || (option == 4))))) {
				logger.debug("Invalid option selected");
				String output = "Invalid Option selected.";
				return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
			}

			return chooseOptionOfStatus(trip, userID, option, correctLocation);

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public ResponseEntity<Object> chooseOptionOfStatus(TripDetails tripDetails, int userID, int option,
			String correctLocation) {
		try {
			int tripScheduleDetailsID = tripDetails.getTripDetailsID();
			CurrentLatLong currentLatLong = passengerDao.getBusCurrentLatLong(tripScheduleDetailsID);
			int previousStatusID = driverDao
					.checkWhetherStudentStatusRecordedAndGetStudentStatusID(tripScheduleDetailsID, userID);
			int success = 0;
			String output = null;
			switch (option) {
			case 1:
			case 3: {
				switch (previousStatusID) {
				case 0:
					success = driverDao.setPickupStatus(tripScheduleDetailsID, userID);
					break;
				case 1:
					output = "Failed!!! Student status is already recorded as Picked.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				case 2:
					output = "Failed!!! Student status is already recorded as Dropped.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				case 3:
					success = driverDao.setPickupStatusForMissedBus(tripScheduleDetailsID, userID);
					break;
				case 4:
					output = "Failed!!! Student status is already recorded as on ScheduledLeave.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				default:
					// this default case will not be possible as valid values of previousStatusID
					// are 0,1,2,3,4
					break;
				}
				break;
			}
			case 2:
			case 4: {
				switch (previousStatusID) {
				case 0:
					output = "Failed!!! This student was not picked.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				case 1:
					success = driverDao.setDropStatus(tripScheduleDetailsID, userID);
					break;
				case 2:
					output = "Failed!!! Student status is already recorded as Dropped.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				case 3:
					output = "Failed!!! Student status is already recorded as Missed Bus.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				case 4:
					output = "Failed!!! Student status is already recorded as on ScheduledLeave.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				default:
					// this default case will not be possible as valid values of previousStatusID
					// are 0,1,2,3,4
					break;
				}
				break;
			}
			default:
				// this default case will not be possible as valid values of option are 1,2,3,4
				break;
			}
			if (success == 1) {
				firebaseService.sendRefreshMessageToScheduleTopicAndAdminTopic(
						tripDetails.getVehicleSchedule().getVehicleScheduleID());
				if (correctLocation.equals("No")) {
					passengerService.notifyPassengerPickedDroppedAtWrongLocation(userID, option, currentLatLong,
							tripDetails);
				} else {
					passengerService.notifyPassengerStatus(userID, option);
				}
				switch (option) {
				case 1:
				case 3:
					return ResponseHandler.generateResponse2(true, "Success!!! Student is picked up.", HttpStatus.OK);
				case 2:
				case 4:
					return ResponseHandler.generateResponse2(true, "Success!!! Student is dropped.", HttpStatus.OK);
				default:
					// this default case will not be possible as valid values of option are 1,2,3,4
					break;
				}
			}

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public List<String> getAdminTokens() {
		List<User> admins = adminDao.getUserListOfGivenRole(1);
		List<String> adminTokens = new ArrayList<>();
		for (User user : admins) {
			UserToken userToken = driverDao.getUserToken(user.getUserID());
			if (userToken != null) {
				adminTokens.add(userToken.getToken());
			}
		}
		return adminTokens;
	}

	public void notifyAdminAboutPassengerMissedBus(int userID, int id, int routeID, String routeName,
			String vehicleScheduleName) {
		User user = adminDao.getUser(userID);
		String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
		String uniqueKey = user.getUserUniqueKey();
		String subject = "Missed Bus";
		String content = null;
		if (id == 5) {
			content = "Below Student missed bus at school:" + System.lineSeparator();
		} else {
			content = "Below Student missed bus at home:" + System.lineSeparator();
		}
		String c = "Name:" + name + System.lineSeparator() + "Unique Key:" + uniqueKey + System.lineSeparator()
				+ "Route ID : " + routeID + System.lineSeparator() + "Route Name : " + routeName
				+ System.lineSeparator() + "Vehicle Schedule Name:" + vehicleScheduleName;
		content = content.concat(c);
		logger.debug("Inside a method");
		Note note = new Note(subject, content);
		List<String> adminTokens = getAdminTokens();
		for (String token : adminTokens) {
			firebaseService.sendNotification(note, token);
		}
		List<User> admins = adminDao.getUserListOfGivenRole(1);
		int countOfNotificationsStoredinDB = 0;
		for (User admin : admins) {
			int success = adminDao.storeNotification(admin.getUserID(), 1, note);
			if (success == 1) {
				countOfNotificationsStoredinDB++;
			}
		}
		
		if (countOfNotificationsStoredinDB == admins.size()) {
			logger.debug("Notifications for all admins stored in DB.");
		} else {
			logger.debug("Notifications for some admins stored in DB.");
		}

	}

	public ResponseEntity<Object> busReachedDestination(int tripID, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripID);
			if ((trip == null) || (!(driverDao.getTripToStaffData(tripID).stream()
					.filter(i -> i.getStaffType().getRoleID() == DRIVER_ROLE_ID)
					.map(i -> i.getTripStaffID().getStaff().getUserID()).toList().contains(userIDHitAPI)))) {
				logger.debug("Invalid tripID");
				return InvalidData.invalidTripID2();
			}
			if (trip.getBusReachedDestination() != null) {
				return ResponseHandler.generateResponse2(true, "Success!", HttpStatus.OK);
			}
			int success = driverDao.updateBusReachedDestinationTime(tripID);
			if (success != 1) {
				return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			logger.debug("valid tripID");
			Trip tripExecutorService = new Trip(driverDao, firebaseService, tripID);

			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.schedule(tripExecutorService::checkTripVerifiedAndNotify, 10, TimeUnit.MINUTES);
			executorService.shutdown();
			return ResponseHandler.generateResponse2(true, "Success!", HttpStatus.OK);

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public ResponseEntity<Object> getDriverProfile(int userID) {
		try {
			User user = adminDao.getUser(userID);
			if (user == null) {
				logger.debug("Invalid UserID");
				return ResponseHandler.generateResponse1(false, "Invalid UserID", HttpStatus.NOT_FOUND, null);
			}
			List<Integer> userRoleIDs = user.getRoles().stream().map(role -> role.getRoleID())
					.collect(Collectors.toList());
			if (!(userRoleIDs.contains(4))) {
				logger.debug("Invalid driverID");
				return ResponseHandler.generateResponse1(false, "Invalid driverID", HttpStatus.NOT_FOUND, null);
			}
			String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
			DriverProfile driver = new DriverProfile();
			driver.setName(name);
			driver.setUserPhoneNumber(user.getUserPhoneNumber());
			driver.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			driver.setUserPhoto(user.getUserPhoto());
			driver.setEmail(user.getEmail());
			logger.debug("Driver profile details fetched.");

			return ResponseHandler.generateResponse1(true, "Driver details fetched.", HttpStatus.OK, driver);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> enableAllPickedButton(int tripID, Integer userIDHitAPI) {
		TripDetails trip = driverDao.getTripDetails(tripID);
		if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripID).contains(userIDHitAPI)))) {
			return ResponseHandler.generateResponse1(false, "Invalid TripID", HttpStatus.NOT_FOUND, null);
		}
		int totalCount = adminDao
				.getStudentIDsListOfGivenVehicleSchedule(trip.getVehicleSchedule().getVehicleScheduleID()).size();
		int pickedCount = adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripID).size();
		int missedBusCount = adminDao.get_missedBus_StudentsIDsListOfGivenTrip(tripID).size();
		int leaveCount = adminDao.get_scheduledLeave_StudentsIDsListOfGivenTrip(tripID).size();
		if ((totalCount - leaveCount) == (pickedCount + missedBusCount)) {
			return ResponseHandler.generateResponse1(true, "Enable 'All Picked' button", HttpStatus.OK, true);
		} else {
			return ResponseHandler.generateResponse1(true, "Disable 'All Picked' button", HttpStatus.OK, false);
		}

	}

	public ResponseEntity<Object> driverAttendantHomescreen(int tripDetailsID, Integer userIDHitAPI) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripDetailsID);
			if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripDetailsID).contains(userIDHitAPI)))) {
				return InvalidData.invalidTripID1();
			}
			List<PassengerStatus> passengertStatusOfTrip = adminDao.getPassengerStatusOfTrip(tripDetailsID);
			int vehicleScheduleID = trip.getVehicleSchedule().getVehicleScheduleID();
			List<Integer> pickedUserIDsOfTrip = new ArrayList<>();
			List<Integer> droppedUserIDsOfTrip = new ArrayList<>();
			List<Integer> missedBusUserIDsOfTrip = new ArrayList<>();
			List<Integer> scheduledLeaveUserIDsOfTrip = new ArrayList<>();
			for (PassengerStatus passengerStatus : passengertStatusOfTrip) {
				int passengerStatusCode = passengerStatus.getUserStatusCode().getStatusID();
				switch (passengerStatusCode) {
				case 1: {
					pickedUserIDsOfTrip.add(passengerStatus.getTripUser().getUser().getUserID());
					break;
				}
				case 2: {
					droppedUserIDsOfTrip.add(passengerStatus.getTripUser().getUser().getUserID());
					break;
				}
				case 3: {
					missedBusUserIDsOfTrip.add(passengerStatus.getTripUser().getUser().getUserID());
					break;
				}
				case 4: {
					scheduledLeaveUserIDsOfTrip.add(passengerStatus.getTripUser().getUser().getUserID());
					break;
				}
				}
			}
			List<PassengerToRouteID> passengerRouteDetailsListOfGivenVehicleSchedule = adminDao
					.getPassengerRouteDetailsListOfGivenVehicleSchedule(vehicleScheduleID);
			int pickedCount = pickedUserIDsOfTrip.size();
			int droppedCount = droppedUserIDsOfTrip.size();
			int missedBusCount = missedBusUserIDsOfTrip.size();
			int scheduledLeaveCount = scheduledLeaveUserIDsOfTrip.size();
			int totalCount = passengerRouteDetailsListOfGivenVehicleSchedule.size();
			int yetToBePickedUserIDsOfTripCount = totalCount - pickedCount - droppedCount - missedBusCount
					- scheduledLeaveCount;
			int totalCountExcludingStudentsOnScheduledLeave = totalCount - scheduledLeaveCount;
			List<Object[]> stopsOfScheduleAndTheirScheduledTime = adminDao
					.getRouteStopScheduleDetailsAsPerStopsOrderOfSchedule(trip.getVehicleSchedule());
			List<StopStudentsCountScheduledTime> list = new ArrayList<>();
			int lastStopIfOnwardJourneySchedule = -1;
			int firstStopIfReturnJourneySchedule = -1;
			if (trip.getVehicleSchedule().getTypeOfJourney() == ONWARD_JOURNEY) {
				lastStopIfOnwardJourneySchedule = ((Stop) (stopsOfScheduleAndTheirScheduledTime
						.get(stopsOfScheduleAndTheirScheduledTime.size() - 1)[0])).getStopID();
			} else {
				firstStopIfReturnJourneySchedule = ((Stop) (stopsOfScheduleAndTheirScheduledTime.get(0)[0]))
						.getStopID();
			}
			for (Object[] routeStopScheduleDetail : stopsOfScheduleAndTheirScheduledTime) {
				StopStudentsCountScheduledTime s = new StopStudentsCountScheduledTime();
				Stop stop = (Stop) routeStopScheduleDetail[0];
				s.setStopOrder((int) (routeStopScheduleDetail[1]));
				s.setScheduledArrivalTime((LocalTime) (routeStopScheduleDetail[3]));
				s.setScheduledDepartureTime((LocalTime) (routeStopScheduleDetail[2]));
				List<Integer> userIDs = new ArrayList<>();
				if (trip.getVehicleSchedule().getTypeOfJourney() == ONWARD_JOURNEY) {
					userIDs = passengerRouteDetailsListOfGivenVehicleSchedule.stream()
							.filter(object -> object.getPickupPointStop().getStopID() == stop.getStopID())
							.map(object -> object.getUserTypeOfJourneyID().getUser().getUserID())
							.collect(Collectors.toList());
				} else {
					userIDs = passengerRouteDetailsListOfGivenVehicleSchedule.stream()
							.filter(object -> object.getDropPointStop().getStopID() == stop.getStopID())
							.map(object -> object.getUserTypeOfJourneyID().getUser().getUserID())
							.collect(Collectors.toList());
				}
				List<Integer> pickedUserIDs = new ArrayList<>();
				pickedUserIDs.addAll(pickedUserIDsOfTrip);
				pickedUserIDs.retainAll(userIDs);
				List<Integer> missedBusUserIDs = new ArrayList<>();
				missedBusUserIDs.addAll(missedBusUserIDsOfTrip);
				missedBusUserIDs.retainAll(userIDs);
				s.setPickedStudentsCount(pickedUserIDs.size());
				s.setMissedBusStudentsCount(missedBusUserIDs.size());
				List<Integer> scheduledLeaveUserIDs = new ArrayList<>();
				scheduledLeaveUserIDs.addAll(scheduledLeaveUserIDsOfTrip);
				scheduledLeaveUserIDs.retainAll(userIDs);
				int count = userIDs.size() - scheduledLeaveUserIDs.size();
				s.setTotalStudentsCountExcludingStudentsOnScheduledLeave(count);
				List<Integer> droppedUserIDs = new ArrayList<>();
				droppedUserIDs.addAll(droppedUserIDsOfTrip);
				droppedUserIDs.retainAll(userIDs);
				boolean stopSet = false;
				if (trip.getVehicleSchedule().getTypeOfJourney() == ONWARD_JOURNEY) {
					if ((lastStopIfOnwardJourneySchedule != stop.getStopID())
							&& (count == (pickedUserIDs.size() + droppedUserIDs.size()))) {
						Stop tempStop = new Stop();
						BeanUtils.copyProperties(stop, tempStop);
						tempStop.setStopLatitude(null);
						tempStop.setStopLongitude(null);
						s.setStop(tempStop);
						stopSet = true;
					}
				} else {
					if (((firstStopIfReturnJourneySchedule != stop.getStopID())
							|| ((firstStopIfReturnJourneySchedule == stop.getStopID()) && (missedBusCount == 0)
									&& (yetToBePickedUserIDsOfTripCount == 0)))
							&& (count == droppedUserIDs.size())) {
						Stop tempStop = new Stop();
						BeanUtils.copyProperties(stop, tempStop);
						tempStop.setStopLatitude(null);
						tempStop.setStopLongitude(null);
						s.setStop(tempStop);
						stopSet = true;
					}
				}
				if (!stopSet) {
					s.setStop(stop);
				}
				list.add(s);
			}
			DriverAttendantHomescreen obj = new DriverAttendantHomescreen();
			obj.setPickedCount(pickedCount);
			obj.setMissedBusCount(missedBusCount);
			obj.setTotalCountExcludingStudentsOnScheduledLeave(totalCountExcludingStudentsOnScheduledLeave);
			if (droppedCount > 0) {
				obj.setPassengerStatusMessage(In_BUS);
			} else {
				obj.setPassengerStatusMessage(PICKED);
			}
			obj.setTotalStops(stopsOfScheduleAndTheirScheduledTime.size());
			obj.setList(list);
			return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, obj);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> setPassword(Integer userID, Password password) {
		try {

			return driverDao.setPassword(userID, encryptionUtil.encrypt(password.getPassword()));
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<Object> incrementDirectionAPICount(int tripID, Integer userIDHitAPI) {
		TripDetails trip = driverDao.getTripDetails(tripID);
		if ((trip == null) || (!(driverDao.getStaffIDsOfTrip(tripID).contains(userIDHitAPI)))) {
			return InvalidData.invalidTripID2();
		}
		if(!directionAPICounter) {
			return ResponseHandler.generateResponse2(false, "Failed! Counting is not enabled.", HttpStatus.NOT_FOUND);
		}
		return driverDao.incrementDirectionAPICount(trip);
	}

}
