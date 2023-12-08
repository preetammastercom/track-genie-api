package com.mastercom.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mastercom.dao.AdminDao;
import com.mastercom.dao.AttendantDao;
import com.mastercom.dao.DriverDao;
import com.mastercom.dao.PassengerDao;
import com.mastercom.entity.Role;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.User;
import com.mastercom.entity.UserToken;
import com.mastercom.entity.VehicleSchedule;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;
import com.mastercom.handler.InvalidData;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.AttendantProfile;
import com.mastercom.dto.TripToBeResumedAndAssignedSchedules;
import com.mastercom.dto.VehicleScheduleIDNameRouteName;
import com.mastercom.dto.VideoURL;
import com.mastercom.dto.jwtDTO.TripTypeOfJourney;

@Service
public class AttendantService {

	@Autowired
	DriverDao driverDao;

	@Autowired
	AttendantDao attendantDao;

	@Autowired
	PassengerDao passengerDao;

	@Autowired
	DriverService driverService;

	@Autowired
	AdminDao adminDao;
	
	@Autowired
	FirebaseMessagingService firebaseService;
	
	private static final String SERVER_ERROR="Server Error!!!";

	private static final Logger logger = LogManager.getLogger(AttendantService.class);

	public ResponseEntity<Object> endTripByAttendant(int userID, int tripScheduleDetailsID, VideoURL videoURL) {
		try {
			TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
			if (trip == null) {
				return InvalidData.invalidTripID2();

			}
			TripToStaff tripToStaff = driverDao.getTripToStaffDataOfGivenStaff(tripScheduleDetailsID, userID);
			if (tripToStaff != null) {
				logger.debug("Valid attendantID for a given tripID");
				if (tripToStaff.getStaffVerifiedTime() != null) {
					logger.debug("Failed!!! Verification Time is already recorded.");
					String output = "Failed!!! Verification Time is already recorded.";
					return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
				}
				else {
					if(!adminDao.get_Picked_StudentsIDsListOfGivenTrip(tripScheduleDetailsID).isEmpty()) {
						logger.debug("Failed! Please drop all students.");
						return ResponseHandler.generateResponse2(false, "Failed! Please drop all students.", HttpStatus.OK);
					}
					boolean isSuccess= attendantDao.endTripByAttendant(userID, tripScheduleDetailsID, videoURL);
					if (isSuccess) {
						UserToken userToken = driverDao.getUserToken(userID);
						if (userToken != null) {
							firebaseService.unsubscribeFromScheduleTopic(trip.getVehicleSchedule().getVehicleScheduleID(),
									userToken.getToken());
						}
						firebaseService.sendRefreshMessageToAdminTopic();
						return ResponseHandler.generateResponse2(true, "Trip completed successfully!!!", HttpStatus.OK);
					} else {
						return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				
			} else {
				logger.debug("Invalid attendantID for a given tripID");
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

	public ResponseEntity<Object> getVehicleSchedulesAssignedToAttendantToday(int userID) {

		try {
			User user = adminDao.getUser(userID);
			if (user != null) {
				List<Role> roles = user.getRoles();
				for (Role role : roles) {
					if (role.getRoleID() == 5) {
						logger.debug("Fetching vehicle schedules assigned to attendant");
						return fetchVehicleSchedulesAssignedToAttendantToday(userID);
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

	public ResponseEntity<Object> fetchVehicleSchedulesAssignedToAttendantToday(int userID) {
		try {
			TripToBeResumedAndAssignedSchedules object = new TripToBeResumedAndAssignedSchedules();
			List<Integer> tripIDsGoingOnForAttendant = attendantDao
					.getTripIDsGoingOnForAttendant(userID);
			if(tripIDsGoingOnForAttendant.size()!=0) {
				object.setResumeTripID(tripIDsGoingOnForAttendant.get(0));
				logger.debug("Trip needs to be resumed");
				return ResponseHandler.generateResponse1(true, "Resume Trip", HttpStatus.OK, object);
			}
			List<Integer> vehicleSchedulesIDsVerifiedByAttendantToday = attendantDao
					.getVehicleSchedulesIDsVerifiedByAttendantToday(userID);
			List<Integer> vehicleSchedulesIDsAssignedToAttendantToday = attendantDao
					.getVehicleSchedulesIDsAssignedToAttendantToday(userID);
			logger.debug("Traversing vehicle schedules going on , which are assigned to attendant");
			
			vehicleSchedulesIDsAssignedToAttendantToday.removeAll(vehicleSchedulesIDsVerifiedByAttendantToday);
			logger.debug(
					"Traversing vehicle schedules whose trip is not started yet , which are assigned to attendant");
			List<VehicleScheduleIDNameRouteName> schedules=new ArrayList<>();
			for (Integer id : vehicleSchedulesIDsAssignedToAttendantToday) {
				VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(id);
				schedules.add(new VehicleScheduleIDNameRouteName(vehicleSchedule.getVehicleScheduleID(), vehicleSchedule.getRoute().getRouteName()+" : "+vehicleSchedule.getVehicleScheduleName()));
			}
			if(schedules.size()==0) {
				logger.debug("No schedule is assigned");
				return ResponseHandler.generateResponse1(false, "No schedule is assigned", HttpStatus.NOT_FOUND, null);
			}
			else {
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

	

	public ResponseEntity<Object> startVehicleScheduleByAttendant(int userID, int vehicleScheduleID) {
		try {
			VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
			if (vehicleSchedule == null) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			User user = adminDao.getUser(userID);
			if ((user == null)
					|| (!((user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList()))
							.contains(5)))) {
				return InvalidData.invalidUserID1();
			}
			if (!((attendantDao.getVehicleSchedulesAssignedToAttendantToday(userID).stream()
					.map(obj -> obj.getVehicleScheduleID()).collect(Collectors.toList()))
					.contains(vehicleScheduleID))) {
				logger.debug("Given vehicle Schedule is not assigned to Attendant.");
				String output = "Given vehicle Schedule is not assigned to Attendant.";
				return ResponseHandler.generateResponse1(false, output, HttpStatus.OK, null);
			}
			int tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
			List<TripToStaff> tripToStaffListOfGivenTrip = null;
			if (tripID != (-1)) {
				tripToStaffListOfGivenTrip = driverDao.getTripToStaffData(tripID);
				long t = tripToStaffListOfGivenTrip.stream().filter(obj -> obj.getStaffType().getRoleID() == 5)
						.filter(obj -> obj.getTripStaffID().getStaff().getUserID() == userID).count();

				if (t == 1) {
					return ResponseHandler.generateResponse1(true, "Trip is started.", HttpStatus.OK, new TripTypeOfJourney(tripID, vehicleSchedule.getTypeOfJourney()));
				}
			}
			List<Integer> allActiveTripIDs = driverDao.getAllActiveTripIDs();
			List<Integer> staffAllTripIDs = driverDao.getStaffAllTripIDs(userID);
			allActiveTripIDs.retainAll(staffAllTripIDs);

			if (!allActiveTripIDs.isEmpty()) {
				return driverService.newTripCannotBeStarted(allActiveTripIDs);
			}
			int success;
			if (tripID != (-1)) {
				success = attendantDao.startVehicleScheduleByAttendant(vehicleScheduleID, userID, tripID);
			} else {
				success = attendantDao.startVehicleScheduleByAttendant(vehicleScheduleID, userID, null);
				tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
				if(success==1) {
					 firebaseService.sendMulticastAsync(new Note("Trip started.","Trip of vehicle schedule '" + vehicleSchedule.getVehicleScheduleName()+ "' is started."),driverDao.getTokensOfPassengersWhoEnabledNotificationOfTripStarted(vehicleScheduleID));
					
				}
			}
			if (success != 1) {
				return ResponseHandler.generateResponse1(false,SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
						null);
			}
			logger.debug("Trip started.");
			UserToken userToken=driverDao.getUserToken(userID);
			firebaseService.subscribeToScheduleTopic(vehicleScheduleID, userToken.getToken());
			return ResponseHandler.generateResponse1(true, "Trip is started.", HttpStatus.OK, new TripTypeOfJourney(tripID, vehicleSchedule.getTypeOfJourney()));
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}
	
	public ResponseEntity<Object> getAttendantProfile(int userID) {

		try {
			User user=adminDao.getUser(userID);
			if(user==null) {
				logger.debug("Invalid UserID");
				return ResponseHandler.generateResponse1(false, "Invalid UserID", HttpStatus.NOT_FOUND, null);
			}
			List<Integer> userRoleIDs=user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList());
			if(!(userRoleIDs.contains(5))) {
				logger.debug("Invalid attendantID");
				return ResponseHandler.generateResponse1(false, "Invalid attendantID", HttpStatus.NOT_FOUND, null);
			}
			String name=user.getUserFirstName()+" "+user.getUserMiddleName()+" "+user.getUserLastName();
			AttendantProfile attendant=new AttendantProfile();
			attendant.setName(name);
			attendant.setUserPhoneNumber(user.getUserPhoneNumber());
			attendant.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
attendant.setUserPhoto(user.getUserPhoto());
attendant.setEmail(user.getEmail());
logger.debug("Attendant profile details fetched.");
			return ResponseHandler.generateResponse1(true, "Attendant details fetched.", HttpStatus.OK, attendant);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}


}
