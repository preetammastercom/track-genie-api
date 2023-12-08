package com.mastercom.service;

import com.mastercom.config.ConfigurableParameters;
import com.mastercom.dao.AdminDao;
import com.mastercom.dao.DriverDao;
import com.mastercom.dao.PassengerDao;
import com.mastercom.entity.*;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;
import com.mastercom.handler.InvalidData;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.mastercom.constant.ApplicationConstant.*;

@Service
public class PassengerService {

	@Autowired
	AdminDao adminDao;
	@Autowired
	DriverDao driverDao;
	@Autowired
	PassengerDao passengerDao;

	@Autowired
	FirebaseMessagingService firebaseService;

	private static final String SERVER_ERROR = "Server Error!!!";
	private static final String DATA_FOUND = "Data found.";
	private static final String NO_DATA_FOUND = "Data not found.";

	@Autowired
	ConfigurableParameters configurableParameters;

	private static final Logger logger = LogManager.getLogger(PassengerService.class);

	public ResponseEntity<Object> getBusInfo(int userID, int vehicleScheduleID) {

		try {
			if ((adminDao.getVehicleSchedule(vehicleScheduleID)) == null) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID1();
			}
			List<PassengerToRouteID> list = passengerDao.getPassengerToRouteID(userID, vehicleScheduleID);
			logger.debug("Route details of a passenger are fetched.");
			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Invalid User ID for vehicle schedule",
						HttpStatus.NOT_FOUND, null);
			}
			PassengerToRouteID passengerToRouteID = list.get(0);
			BusInfo busInfo = new BusInfo();
			VehicleDetails vehicle = adminDao.getVehicleOfScheduleforGivenDate(vehicleScheduleID, LocalDate.now());
			if (vehicle != null) {
				busInfo.setVehicleRegisterationNumber(vehicle.getRegisterationNumber());
				busInfo.setVehicleType(vehicle.getVehicleType());
			}

			List<Object[]> staffOfVehicleSchedule = passengerDao.getStaffOfVehicleSchedule(vehicleScheduleID,
					LocalDate.now());
			logger.debug("Details of Staff assigned to vehicle schedule are fetched.");
			List<UserNamePhoneNum> drivers = new ArrayList<>();
			List<UserNamePhoneNum> attendants = new ArrayList<>();
			for (Object[] obj2 : staffOfVehicleSchedule) {
				User staff = (User) obj2[1];
				String staffName = staff.getUserFirstName() + " " + staff.getUserMiddleName() + " "
						+ staff.getUserLastName();
				if (((int) (obj2[0])) == 4) {
					UserNamePhoneNum driver1 = new UserNamePhoneNum(staffName, staff.getUserPhoneNumber());
					drivers.add(driver1);
				} else {
					UserNamePhoneNum attendant1 = new UserNamePhoneNum(staffName, staff.getUserPhoneNumber());
					attendants.add(attendant1);
				}
			}
			busInfo.setDrivers(drivers);
			busInfo.setAttendants(attendants);
			busInfo.setRouteID(passengerToRouteID.getRoute().getRouteID());
			busInfo.setRouteName(passengerToRouteID.getRoute().getRouteName());
			return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, busInfo);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public boolean checkUserIsStudent(int userID) {
		User user = adminDao.getUser(userID);
		if (user == null) {
			logger.debug("Invalid userID");
			return false;
		}

		List<Role> roles = user.getRoles();
		for (Role role : roles) {
			if (role.getRoleID() == 3) {
				logger.debug("Valid userID");
				return true;
			}
		}
		logger.debug("Invalid passengerID");
		return false;
	}

	public ResponseEntity<Object> getSchoolContact() {
		try {
			HashMap<String, Object> data = new LinkedHashMap<>();
			ArrayList<Long> adminPhoneNumbers = new ArrayList<>();
			School school = passengerDao.getSchool();
			List<User> admins = adminDao.getUserListOfGivenRole(1);
			logger.debug("Fetched admin details.");
			for (User user : admins) {
				adminPhoneNumbers.add(user.getUserPhoneNumber());
			}
			data.put("School Administrator", adminPhoneNumbers);
			if (school != null) {
				data.put("School Front Desk", school.getSchoolPhoneNum());
			}
			if ((adminPhoneNumbers.isEmpty()) && (school == null)) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			} else {
				return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, data);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getVehicleScheduleIDAndTripIDofStudentCorrespondingToGivenTypeOfJourney(int userID,
			int typeOfJourney, LocalDate date) {
		try {
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID1();
			}
			if (!((typeOfJourney == 1) || (typeOfJourney == 2))) {
				return InvalidData.invalidTypeOfJourney1();
			}
			logger.debug("Fetching Vehicle schedule of student corresponding to given type of journey.");
			int vehicleScheduleID = passengerDao.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID,
					typeOfJourney);

			if (vehicleScheduleID == (-1)) {
				return ResponseHandler.generateResponse1(false,
						"Failed!!! Please inform admin to map vehicle schedule.", HttpStatus.NOT_FOUND, null);
			}
			logger.debug("Fetching tripID corresponding to vehicle schedule.");
			int tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, date);

			HashMap<String, Integer> hm = new LinkedHashMap<>();
			hm.put("TripID", null);
			hm.put("VehicleScheduleID", null);
			if (tripID != (-1)) {
				hm.put("TripID", tripID);
			}
			if (vehicleScheduleID != (-1)) {
				hm.put("VehicleScheduleID", vehicleScheduleID);
			}
			if ((tripID == (-1)) && (vehicleScheduleID == (-1))) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			} else {
				return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, hm);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getScheduledLeaveDates(int userID, int typeOfJourney) {
		try {
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID1();
			}
			if (!((typeOfJourney == 1) || (typeOfJourney == 2) || (typeOfJourney == 3))) {
				return InvalidData.invalidTypeOfJourney1();
			}

			if (typeOfJourney == 3) {
				return getScheduledLeaveDatesBothWays(userID);
			}
			logger.debug("Fetching vehicle schedule corresponding to given type of journey for a user");
			int vehicleScheduleID = passengerDao.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID,
					typeOfJourney);

			if (vehicleScheduleID == (-1)) {
				return ResponseHandler.generateResponse1(false,
						"Failed!!! Please inform admin to assign vehicle schedule.", HttpStatus.NOT_FOUND, null);
			}
			List<LocalDate> leaveDates = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleID);
			logger.debug("Fetched schedule leave dates");
			if (leaveDates.isEmpty()) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			}
			if (leaveDates.contains(LocalDate.now())) {
				logger.debug("Leave dates contains today also. ");
				LocalTime endTime = adminDao.getVehicleSchedule(vehicleScheduleID).getScheduledDepartureTime()
						.minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
				LocalTime currentTime = LocalTime.now();
				if (!(endTime.isAfter(currentTime))) {
					leaveDates.remove(LocalDate.now());
				}
			}
			if (!leaveDates.isEmpty()) {
				return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, leaveDates);
			} else {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	private ResponseEntity<Object> getScheduledLeaveDatesBothWays(int userID) {
		try {
			int vehicleScheduleIDOnward = passengerDao
					.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID, 1);
			int vehicleScheduleIDReturn = passengerDao
					.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID, 2);
			logger.debug("Fetched vehcile schedules mapped to user");
			if ((vehicleScheduleIDOnward == (-1)) || (vehicleScheduleIDReturn == (-1))) {
				return ResponseHandler.generateResponse1(false,
						"Failed!!! Please inform admin to assign vehicle schedule.", HttpStatus.NOT_FOUND, null);
			}
			List<LocalDate> leaveDatesOnward = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleIDOnward);
			List<LocalDate> leaveDatesReturn = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleIDReturn);
			leaveDatesOnward.retainAll(leaveDatesReturn); // common dates
			List<LocalDate> leaveDates = leaveDatesOnward;
			logger.debug("Fetched leave dates");
			if (leaveDates.isEmpty()) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			}
			if (leaveDates.contains(LocalDate.now())) {
				logger.debug("Leave dates contains today's date also.");
				LocalTime endTimeOnward = adminDao.getVehicleSchedule(vehicleScheduleIDOnward)
						.getScheduledDepartureTime().minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
				LocalTime endTimeReturn = adminDao.getVehicleSchedule(vehicleScheduleIDReturn)
						.getScheduledDepartureTime().minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
				LocalTime currentTime = LocalTime.now();
				LocalTime endTime = null;
				if (endTimeOnward.isBefore(endTimeReturn)) {
					endTime = endTimeOnward;
				} else {
					endTime = endTimeReturn;
				}
				if (!(endTime.isAfter(currentTime))) {
					leaveDates.remove(LocalDate.now());
				}
			}
			if (!leaveDates.isEmpty()) {
				return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, leaveDates);
			} else {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getTimeRequiredToReachAtUserStop(int userID, int vehicleScheduleID,
			Integer tripScheduleDetailsID) {
		try {
			if ((adminDao.getVehicleSchedule(vehicleScheduleID)) == null) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID1();
			}
			logger.debug("Fetching route details of a user");
			List<PassengerToRouteID> list = passengerDao.getPassengerToRouteID(userID, vehicleScheduleID);

			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Invalid User ID for given vehicle schedule.",
						HttpStatus.NOT_FOUND, null);
			}
			if (tripScheduleDetailsID != null) {
				TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID.intValue());
				if (trip == null) {
					return InvalidData.invalidTripID1();
				}
				if (vehicleScheduleID != trip.getVehicleSchedule().getVehicleScheduleID()) {
					logger.debug("Invalid tripID for given vehicle schedule.");
					return ResponseHandler.generateResponse1(false, "Invalid Trip ID for given vehicle schedule.",
							HttpStatus.NOT_FOUND, null);
				}
				return calculateTimeRequiredToReachUserStop(userID, vehicleScheduleID,
						tripScheduleDetailsID.intValue());
			} else {
				logger.debug("Fetching tripID corresponding to vehicle schedule.");
				int tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID,
						LocalDate.now());
				if (tripID == (-1)) {
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, "Trip Yet to Start.");
				} else {
					return calculateTimeRequiredToReachUserStop(userID, vehicleScheduleID, tripID);
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

	public ResponseEntity<Object> calculateTimeRequiredToReachUserStop(int userID, int vehicleScheduleID,
			int tripScheduleDetailsID) {
		try {
			List<TripToStaff> tripToStaffList = driverDao.getTripToStaffData(tripScheduleDetailsID);
			int totalStaff = tripToStaffList.size();
			int staffVerifiedCount = 0;
			int adminVerifiedCount = 0;
			for (TripToStaff tripToStaff : tripToStaffList) {
				if (tripToStaff.getStaffVerifiedTime() != null) {
					staffVerifiedCount++;
				}
				if (tripToStaff.getAdminVerifiedTime() != null) {
					adminVerifiedCount++;
				}
			}
			if (staffVerifiedCount == totalStaff) {
				if (adminVerifiedCount == totalStaff) {
					logger.debug("Trip Completed - Verified");
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
							"Trip Completed - Verified");
				} else {
					logger.debug("Trip Completed - Yet to be Verified");
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
							"Trip Completed - Yet to be Verified");
				}
			}
			int statusID = passengerDao.childOnboardStatusOfGivenTrip(userID, tripScheduleDetailsID);
			logger.debug("Fetched passenger status.");
			if (adminDao.getVehicleSchedule(vehicleScheduleID).getTypeOfJourney() == 1) {

				if (statusID != (-1)) {
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, null);
				}
			} else {
				if ((statusID == 2) || (statusID == 3) || (statusID == 4)) {
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, null);
				}
			}

			int stopIDOfUser = -1;
			PassengerToRouteID obj = passengerDao.getPassengerToRouteID(userID, vehicleScheduleID).get(0);
			logger.debug("Route details of a user are fetched. ");
			if (obj.getUserTypeOfJourneyID().getTypeOfJourney() == 1) {
				Stop stop = obj.getPickupPointStop();
				stopIDOfUser = stop.getStopID();
			} else {
				Stop stop = obj.getDropPointStop();
				stopIDOfUser = stop.getStopID();
			}
			logger.debug("Fetching time required to reach at stop is fetched. ");
			TimeRequiredToReachAtStop obj2 = passengerDao.getTimeRequiredToReachAtStop(tripScheduleDetailsID,
					stopIDOfUser);

			if (obj2 == null) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
			}
			String output = obj2.getTime();
			return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, output);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getBusCurrentLatLong(int vehicleScheduleID, Integer tripID, Integer userIDHitAPI) {
		try {
			if (((adminDao.getVehicleSchedule(vehicleScheduleID)) == null)||(!(passengerDao.getVehicleScheduleIDsofPassenger(userIDHitAPI).contains(vehicleScheduleID)))) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			int tripScheduleDetailsID = 0;
			if (tripID == null) {
				logger.debug("Fetching tripID corresponding to vehicle schedule.");
				tripScheduleDetailsID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID,
						LocalDate.now());

				if (tripScheduleDetailsID == (-1)) {
					BusLatLong busLatLong = new BusLatLong(null, null);
					return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, busLatLong);
				}
			} else {
				tripScheduleDetailsID = tripID.intValue();
				TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
				if (trip == null) {
					return InvalidData.invalidTripID1();
				}
				int t1 = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
				if (t1 != tripScheduleDetailsID) {
					logger.debug("Invalid TripID for Given Vehicle Schedule.");
					return ResponseHandler.generateResponse1(false, "Invalid Trip ID for Given Vehicle Schedule.",
							HttpStatus.NOT_FOUND, null);
				}
			}
			logger.debug("Fetching current lat long of trip");
			CurrentLatLong obj = passengerDao.getBusCurrentLatLong(tripScheduleDetailsID);
			if (obj == null) {
				return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.OK, null);
			}
			BusLatLong busLatLong = new BusLatLong(obj.getBusCurrentLat(), obj.getBusCurrentLong());
			return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, busLatLong);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getLogOfPassengerStatus(int userID) {
		try {
			if (!(checkUserIsStudent(userID))) {
				logger.debug("Invalid userID");
				return ResponseHandler.generateResponse4(false, "Invalid User ID.", HttpStatus.NOT_FOUND, null, null);
			}
			List<DayData> data = new ArrayList<>();
			logger.debug("Fetching passenger status records.");
			List<PassengerStatus> list = passengerDao.getPassengerStatus(userID, 0, 10);
			int total = list.size();

			if (total == 0) {
				return ResponseHandler.generateResponse4(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null, null);
			}
			int passValue = 0;
			for (int i = 0; i < total;) {
				List<Object> temp = null;
				if ((i + 1) != total) {
					temp = getSameDayDataOfPassengerStatus(list.get(i), list.get(i + 1));

				} else {
					temp = getSameDayDataOfPassengerStatus(list.get(i), null);
				}
				data.add((DayData) (temp.get(0)));
				if (!((boolean) (temp.get(1)))) {
					passValue = passValue + 1;
					i = i + 1;
				} else {
					passValue = passValue + 2;
					i = i + 2;
				}
				if (data.size() == 5) {
					break;
				}
			}

			return ResponseHandler.generateResponse4(true, DATA_FOUND, HttpStatus.OK, data, passValue);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse4(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null, null);
		}
	}

	public List<Object> getSameDayDataOfPassengerStatus(PassengerStatus obj, PassengerStatus obj2) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM uuuu");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		logger.debug("Date time is formatted.");
		LocalDate date = null;
		DayData dayDataObj = new DayData();
		LocalDate date2 = null;
		int statusID = obj.getUserStatusCode().getStatusID();
		int typeOfJourney = obj.getTypeOfJourney();
		int typeOfJourney2 = 0;
		int statusID2 = 0;
		if (obj2 != null) {
			typeOfJourney2 = obj2.getTypeOfJourney();
			statusID2 = obj2.getUserStatusCode().getStatusID();
		}
		if ((statusID == 1) || (statusID == 2)) {
			date = obj.getPassengerPickedUpTime().toLocalDate();
		} else {
			date = obj.getUpdatedTime().toLocalDate();
		}
		if (typeOfJourney == 1) {
			if (statusID == 1) {
				dayDataObj.setPicked_Up_from_home(obj.getPassengerPickedUpTime().format(format));
			} else if (statusID == 2) {
				dayDataObj.setPicked_Up_from_home(obj.getPassengerPickedUpTime().format(format));
				dayDataObj.setReached_School(obj.getPassengerDropTime().format(format));
			} else if (statusID == 3) {
				dayDataObj.setMissed_Bus_Onward_Journey(obj.getUpdatedTime().format(format));
			} else if (statusID == 4) {
				dayDataObj.setScheduled_Leave_Onward_Journey(obj.getUpdatedTime().format(format));
			}
		} else {
			if (statusID == 1) {
				dayDataObj.setPicked_up_from_school(obj.getPassengerPickedUpTime().format(format));
			} else if (statusID == 2) {
				dayDataObj.setPicked_up_from_school(obj.getPassengerPickedUpTime().format(format));
				dayDataObj.setReached_Home(obj.getPassengerDropTime().format(format));
			} else if (statusID == 3) {
				dayDataObj.setMissed_Bus_Return_Journey(obj.getUpdatedTime().format(format));
			} else if (statusID == 4) {
				dayDataObj.setScheduledLeaveReturnJourney(obj.getUpdatedTime().format(format));
			}
		}
		boolean flag = false;
		if (obj2 != null) {
			logger.debug("obj2 is not null. ");
			if ((statusID2 == 1) || (statusID2 == 2)) {
				date2 = obj2.getPassengerPickedUpTime().toLocalDate();
			} else {
				date2 = obj2.getUpdatedTime().toLocalDate();
			}
			if (date2.isEqual(date)) {
				if (typeOfJourney == 1) {
					if (typeOfJourney2 == 2) {
						flag = true;
						if (statusID2 == 1) {
							dayDataObj.setPicked_up_from_school(obj2.getPassengerPickedUpTime().format(format));
						} else if (statusID2 == 2) {
							dayDataObj.setPicked_up_from_school(obj2.getPassengerPickedUpTime().format(format));
							dayDataObj.setReached_Home(obj2.getPassengerDropTime().format(format));
						} else if (statusID2 == 3) {
							dayDataObj.setMissed_Bus_Return_Journey(obj2.getUpdatedTime().format(format));
						} else if (statusID2 == 4) {
							dayDataObj.setScheduledLeaveReturnJourney(obj2.getUpdatedTime().format(format));
						}
					}
				} else {
					if (typeOfJourney2 == 1) {
						flag = true;
						if (statusID2 == 1) {
							dayDataObj.setPicked_Up_from_home(obj2.getPassengerPickedUpTime().format(format));
						} else if (statusID2 == 2) {
							dayDataObj.setPicked_Up_from_home(obj2.getPassengerPickedUpTime().format(format));
							dayDataObj.setReached_School(obj2.getPassengerDropTime().format(format));
						} else if (statusID2 == 3) {
							dayDataObj.setMissed_Bus_Onward_Journey(obj2.getUpdatedTime().format(format));
						} else if (statusID2 == 4) {
							dayDataObj.setScheduled_Leave_Onward_Journey(obj2.getUpdatedTime().format(format));
						}
					}
				}
			}
		}
		List<Object> list = new ArrayList<>();
		dayDataObj.setDate(formatter.format(date));
		list.add(dayDataObj);
		if (date2 == null) {
			list.add(false);
		} else {
			if (date.isEqual(date2)) {
				if (flag) {
					list.add(true);
				} else {
					list.add(false);
				}
			} else {
				list.add(false);
			}
		}
		return list;
	}

	public ResponseEntity<Object> getLogOfPassengerStatus_ShowMore(int userID, int passValue) {
		try {
			if (!(checkUserIsStudent(userID))) {
				logger.debug("Invalid userID");
				return ResponseHandler.generateResponse4(false, "Invalid User ID.", HttpStatus.NOT_FOUND, null, null);
			}
			if (passValue < 0) {
				logger.debug("Pass Value cannot be negative. ");
				return ResponseHandler.generateResponse4(false, "Pass Value cannot be negative.", HttpStatus.OK, null,
						null);
			}

			List<DayData> data = new ArrayList<>();
			logger.debug("Fetching passenger status records.");
			List<PassengerStatus> list = passengerDao.getPassengerStatus(userID, passValue, 10);

			if (list.isEmpty()) {
				return ResponseHandler.generateResponse4(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null, null);
			}
			int total = list.size();
			for (int i = 0; i < total;) {
				List<Object> temp = null;
				if ((i + 1) != total) {
					temp = getSameDayDataOfPassengerStatus(list.get(i), list.get(i + 1));
				} else {
					temp = getSameDayDataOfPassengerStatus(list.get(i), null);
				}
				data.add((DayData) (temp.get(0)));
				if (!((boolean) (temp.get(1)))) {
					passValue = passValue + 1;
					i = i + 1;
				} else {
					passValue = passValue + 2;
					i = i + 2;
				}
				if (data.size() == 5) {
					break;
				}
			}

			return ResponseHandler.generateResponse4(true, DATA_FOUND, HttpStatus.OK, data, passValue);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse4(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null, null);
		}
	}

	public ResponseEntity<Object> scheduleLeaveNew(int userID,
			PassengerIDScheduledLeaveDateTypeOfJourney passengerID_ScheduledLeaveDate_TypeOfJourney) {
		try {
			int typeOfJourney = passengerID_ScheduledLeaveDate_TypeOfJourney.getTypeOfJourney();
			LocalDate startDate = LocalDate.parse(passengerID_ScheduledLeaveDate_TypeOfJourney.getStartDate());
			LocalDate endDate = LocalDate.parse(passengerID_ScheduledLeaveDate_TypeOfJourney.getEndDate());
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID2();
			}
			if (!((typeOfJourney == 1) || (typeOfJourney == 2) || (typeOfJourney == 3))) {
				return InvalidData.invalidTypeOfJourney2();
			}

			if (startDate.isBefore(LocalDate.now())) {
				return ResponseHandler.generateResponse2(false, "Failed!!! Start date must be current or future date.",
						HttpStatus.OK);
			}
			if (endDate.isBefore(startDate)) {
				return ResponseHandler.generateResponse2(false,
						"Failed!!! End date must be same as or future date of start date.", HttpStatus.OK);
			}

			int successStatus = 0;
			if (typeOfJourney == 3) {
				List<Object> list1 = scheduleLeaveCode(userID, 1, startDate, endDate);
				List<Object> list2 = scheduleLeaveCode(userID, 2, startDate, endDate);

				int size1 = list1.size();
				int size2 = list2.size();

				if ((size1 == 0) || (size2 == 0)) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! Please inform admin to map vehicle schedule.", HttpStatus.NOT_FOUND);
				}
				if ((size1 == 1) && (size2 == 1)) {
					return ResponseHandler.generateResponse2(false, "Failed!!! Already applied for Leave.",
							HttpStatus.OK);
				}
				if (size1 == 2) {
					return ResponseHandler.generateResponse2(false,
							"Failed for all dates!!! For current date's onward journey, user status is already recorded as "
									+ (String) (list1.get(1)),
							HttpStatus.OK);
				}
				if (size2 == 2) {
					return ResponseHandler.generateResponse2(false,
							"Failed for all dates!!! For current date's return journey, user status is already recorded as "
									+ (String) (list2.get(1)),
							HttpStatus.OK);
				}
				if ((size1 == 1) && (size2 == 3)) {
					successStatus = passengerDao.scheduleLeave((PassengerToRouteID) (list2.get(0)),
							(List<LocalDate>) (list2.get(1)), (int) (list2.get(2)));
				} else if ((size2 == 1) && (size1 == 3)) {
					successStatus = passengerDao.scheduleLeave((PassengerToRouteID) (list1.get(0)),
							(List<LocalDate>) (list1.get(1)), (int) (list1.get(2)));
				} else {
					int successStatus2 = passengerDao.scheduleLeave((PassengerToRouteID) (list2.get(0)),
							(List<LocalDate>) (list2.get(1)), (int) (list2.get(2)));
					int successStatus1 = passengerDao.scheduleLeave((PassengerToRouteID) (list1.get(0)),
							(List<LocalDate>) (list1.get(1)), (int) (list1.get(2)));
					if ((successStatus2 == 1) && (successStatus1 == 1)) {
						successStatus = 1;
					}
				}
				if (successStatus == 1) {
					return ResponseHandler.generateResponse2(true, "Succcess!!! Applied for leave.", HttpStatus.OK);
				} else {
					return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<Object> list = scheduleLeaveCode(userID, typeOfJourney, startDate, endDate);
				int size = list.size();

				if (size == 0) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! Please inform admin to map vehicle schedule.", HttpStatus.NOT_FOUND);
				} else if (size == 1) {
					return ResponseHandler.generateResponse2(false, "Failed!!! Already applied for Leave.",
							HttpStatus.OK);
				} else if (size == 2) {
					return ResponseHandler.generateResponse2(false,
							"Failed for all dates!!! For current date's onward journey, user status is already recorded as "
									+ (String) (list.get(1)),
							HttpStatus.OK);
				} else if (size == 3) {
					successStatus = passengerDao.scheduleLeave((PassengerToRouteID) (list.get(0)),
							(List<LocalDate>) (list.get(1)), (int) (list.get(2)));
				}
			}
			if (successStatus == 1) {
				return ResponseHandler.generateResponse2(true, "Succcess!!! Applied for leave.", HttpStatus.OK);
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

	public List<Object> scheduleLeaveCode(int userID, int typeOfJourney, LocalDate startDate, LocalDate endDate) {
		List<Object> list = new ArrayList<>();
		int vehicleScheduleID = passengerDao.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID,
				typeOfJourney);
		if (vehicleScheduleID == (-1)) {
			logger.debug("Vehicle schedule is not mapped.");
			return list; // list size is 0
		}
		LocalDate date = startDate;
		ArrayList<LocalDate> dates = new ArrayList<>();
		int count = 0;
		ArrayList<LocalDate> datesAlreadyApplied = new ArrayList<>();
		while ((endDate.isAfter(date)) || (endDate.isEqual(date))) {
			if (passengerDao.checkUserAppliedForLeave(userID, vehicleScheduleID, date)) {
				datesAlreadyApplied.add(date);
			} else {
				dates.add(date);
			}
			date = date.plusDays(1L);
			count++;
		}
		if ((!datesAlreadyApplied.isEmpty())
				&& ((count == (datesAlreadyApplied.size())) && (datesAlreadyApplied.get(0).isEqual(startDate))
						&& (datesAlreadyApplied.get((datesAlreadyApplied.size()) - 1).isEqual(endDate)))) {

			logger.debug("Already applied for leave for selected dates and journey. ");
			list.add(1);
			return list; // list size is 1

		}
		PassengerToRouteID details = passengerDao.getPassengerToRouteID(userID, vehicleScheduleID).get(0);
		int tripID = -1;
		if (dates.contains(LocalDate.now())) {
			tripID = passengerDao.getTripIDCorrespondingToGivenVehicleSchedule(vehicleScheduleID, LocalDate.now());
			if (tripID != (-1)) {
				int status = passengerDao.childOnboardStatusOfGivenTrip(userID, tripID);
				if (status != (-1)) {
					HashMap<Integer, String> hm = new HashMap<>();
					hm.put(1, "Child onboarded.");
					hm.put(2, "Child dropped.");
					hm.put(3, "Child missed bus.");

					logger.debug("For current date's trip, user status is already recorded");
					list.add(2);
					list.add(hm.get(status));
					return list; // list size is 2
				}
			}
		}
		list.add(details);
		list.add(dates);
		list.add(tripID);

		return list; // list size is 3
	}

	public ResponseEntity<Object> cancelScheduledLeave2(int userID, int typeOfJourney, String date1) {
		try {
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID2();
			}
			if (!((typeOfJourney == 1) || (typeOfJourney == 2) || (typeOfJourney == 3))) {
				return InvalidData.invalidTypeOfJourney2();
			}
			LocalDate date = LocalDate.parse(date1);
			LocalDate todayDate = LocalDate.now();
			if (date.isBefore(todayDate)) {
				return ResponseHandler.generateResponse2(false, "Please select current or future date.", HttpStatus.OK);
			}
			int successStatus = 0;
			if (typeOfJourney == 3) {

				int vehicleScheduleID1 = passengerDao
						.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID, 1);
				int vehicleScheduleID2 = passengerDao
						.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID, 2);
				if (vehicleScheduleID1 == (-1)) {
					logger.debug("Onward journey vehicle schedule is not mapped to user");
					return ResponseHandler.generateResponse2(false,
							"Failed!!! Please inform admin to map vehicle schedule for onward journey. ",
							HttpStatus.NOT_FOUND);
				}
				if (vehicleScheduleID2 == (-1)) {
					logger.debug("Return journey vehicle schedule is not mapped to user");
					return ResponseHandler.generateResponse2(false,
							"Failed!!! Please inform admin to map vehicle schedule for return journey. ",
							HttpStatus.NOT_FOUND);
				}
				List<LocalDate> leaveDates1 = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleID1);
				List<LocalDate> leaveDates2 = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleID2);
				logger.debug("Cheking - whether leave is applied on selected date by user");
				if ((!(leaveDates1.contains(date))) && (!(leaveDates2.contains(date)))) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! On selected date, leave was not applied for onward and return journey.",
							HttpStatus.NOT_FOUND);
				}
				if (!(leaveDates1.contains(date))) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! On selected date, leave was not applied for onward journey.",
							HttpStatus.NOT_FOUND);
				}
				if (!(leaveDates2.contains(date))) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! On selected date, leave was not applied for return journey.",
							HttpStatus.NOT_FOUND);
				}
				if (date.isEqual(todayDate)) {
					LocalTime scheduledArrivalTime1 = adminDao.getVehicleSchedule(vehicleScheduleID1)
							.getScheduledArrivalTime();
					LocalTime endTime1 = scheduledArrivalTime1
							.minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
					LocalTime currentTime = LocalTime.now();
					if (!(endTime1.isAfter(currentTime))) {
						return ResponseHandler.generateResponse2(false,
								"Failed!!! Leave can be canceled till "
										+ configurableParameters.getCancelLeaveBeforeTime()
										+ " minutes before of scheduled onward trip start time.",
								HttpStatus.OK);
					}
					LocalTime scheduledArrivalTime2 = adminDao.getVehicleSchedule(vehicleScheduleID2)
							.getScheduledArrivalTime();
					LocalTime endTime2 = scheduledArrivalTime2
							.minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
					if (!(endTime2.isAfter(currentTime))) {
						return ResponseHandler.generateResponse2(false,
								"Failed!!! Leave can be canceled till "
										+ configurableParameters.getCancelLeaveBeforeTime()
										+ " minutes before of scheduled return trip start time.",
								HttpStatus.OK);
					}
				}

				int successStatus1 = passengerDao.cancelScheduledLeave(userID, vehicleScheduleID1, date);
				int successStatus2 = passengerDao.cancelScheduledLeave(userID, vehicleScheduleID2, date);
				if ((successStatus1 == 1) && (successStatus2 == 1)) {
					successStatus = 1;
				} else {
					successStatus = -1;
				}
			} else {
				int vehicleScheduleID = passengerDao
						.getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(userID, typeOfJourney);
				if (vehicleScheduleID == (-1)) {
					logger.debug("vehicle schedule is not mapped to user");
					return ResponseHandler.generateResponse2(false,
							"Failed!!! Please inform admin to map vehicle schedule. ", HttpStatus.NOT_FOUND);
				}
				logger.debug("Cheking - whether leave is applied on selected date by user");
				List<LocalDate> leaveDates = passengerDao.getSheduledLeaveDates(userID, vehicleScheduleID);
				if (!(leaveDates.contains(date))) {
					return ResponseHandler.generateResponse2(false,
							"Failed!!! On selected date, leave was not applied.", HttpStatus.NOT_FOUND);
				}

				if (date.isEqual(todayDate)) {
					LocalTime scheduledArrivalTime = adminDao.getVehicleSchedule(vehicleScheduleID)
							.getScheduledArrivalTime();
					LocalTime endTime = scheduledArrivalTime
							.minusMinutes(configurableParameters.getCancelLeaveBeforeTime());
					LocalTime currentTime = LocalTime.now();

					if (!(endTime.isAfter(currentTime))) {
						return ResponseHandler.generateResponse2(false,
								"Failed!!! Leave can be canceled till "
										+ configurableParameters.getCancelLeaveBeforeTime()
										+ " minutes before of scheduled trip start time.",
								HttpStatus.OK);
					}
				}
				successStatus = passengerDao.cancelScheduledLeave(userID, vehicleScheduleID, date);
			}
			if (successStatus == 1) {
				return ResponseHandler.generateResponse2(true, "Success!!! Leave canceled.", HttpStatus.OK);
			} else {
				return ResponseHandler.generateResponse2(true, "Server Error!!!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//use it for second phase
	public ResponseEntity<Object> update_On_Off_Notification(int userID, boolean check, int parameter) {
		try {
			// 1: WhenBusArrivedAtSchool;
			// 2: WhenBusLeftSchool;
			// 3: WhenBusArrivedAtYourHome;
			// 4: WhenBusLeftYourHome;
			// 5: tripStarted;
			// 6. tripVerifiedByAdmin

			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID2();
			}
			return passengerDao.update_On_Off_Notification(userID, check, parameter);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * meaning of id values: 1: pickup from home 2: drop to school 3: pickup from
	 * school 4: drop to home 5: missed bus
	 */

	public void notifyPassengerStatus(int userID, int id) {
		User user = adminDao.getUser(userID);
		NotificationOnOff notificationOnOff = driverDao.getNotificationOnOff(userID);
		UserToken userToken = driverDao.getUserToken(userID);
		if (userToken == null) {
			logger.debug("user token is not there.");
			return;
		}
		if (notificationOnOff == null) {
			logger.debug("NotificationOnOff object is null.");
			return;
		}
		String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
		LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
		map.put(1, "Pickup from Home");
		map.put(2, "Drop to School");
		map.put(3, "Pickup from School");
		map.put(4, "Drop to Home");
		map.put(5, "Missed Bus");
		map.put(6, "Missed Bus");
		String subject = map.get(id);
		if ((id == 1) && (notificationOnOff.getPassengerEnteredTheBusAtHome() == 1)) {
			String content = name + " entered the bus at home";
			Note note = new Note(subject, content);
			firebaseService.sendNotification(note, userToken.getToken());

		} else if ((id == 2) && (notificationOnOff.getPassengerGotDownOfTheBusAtSchool() == 1)) {
			String content = name + " got down of the bus at school.";
			Note note = new Note(subject, content);
			firebaseService.sendNotification(note, userToken.getToken());

		} else if ((id == 3) && (notificationOnOff.getPassengerEnteredTheBusAtSchool() == 1)) {
			String content = name + " entered the bus at school";
			Note note = new Note(subject, content);
			firebaseService.sendNotification(note, userToken.getToken());

		} else if ((id == 4) && (notificationOnOff.getPassengerGotDownOfTheBusAtHome() == 1)) {
			String content = name + " got down of the bus at home.";
			Note note = new Note(subject, content);
			firebaseService.sendNotification(note, userToken.getToken());

		} else if ((id == 5) && (notificationOnOff.getMissedBus() == 1)) {
			String content = name + " missed bus at school.";
			Note note = new Note(subject, content);
firebaseService.sendNotification(note, userToken.getToken());

		} else if ((id == 6) && (notificationOnOff.getMissedBus() == 1)) {
			String content = name + " missed bus at home.";
			Note note = new Note(subject, content);
			firebaseService.sendNotification(note, userToken.getToken());

		}
		
		firebaseService.sendRefreshMessageToToken(userToken.getToken());
	}

	public void notifyPassengerPickedDroppedAtWrongLocation(int userID, int id, CurrentLatLong currentLatLong,
			TripDetails trip) {
		User user = adminDao.getUser(userID);
		String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
		int routeID = trip.getVehicleSchedule().getRoute().getRouteID();
		String routeName = trip.getVehicleSchedule().getRoute().getRouteName();
		String vehicleScheduleName = trip.getVehicleSchedule().getVehicleScheduleName();
		String uniqueKey = user.getUserUniqueKey();
		String latitude = "";
		String longitude = "";
		if (currentLatLong != null) {
			latitude = currentLatLong.getBusCurrentLat();
			longitude = currentLatLong.getBusCurrentLong();
		}
		String subject = "Important Alert!";
		String content = "Kindly note: ";
		String adminContent = null;
		if ((id == 1) || (id == 3)) {
			if (currentLatLong != null) {
				content = content + name + " has been pickedup at \"" + latitude + "," + longitude
						+ "\" location which is not the usual pickup point.";
				adminContent = "Kindly note: Below Student has been pickedup at \"" + latitude + "," + longitude
						+ "\" location which is not the usual pickup point." + System.lineSeparator();
			} else {
				content = content + name + " has been pickedup at location which is not the usual pickup point.";
				adminContent = "Kindly note: Below Student has been pickedup at location which is not the usual pickup point."
						+ System.lineSeparator();
			}

		} else {
			if (currentLatLong != null) {
				content = content + name + " has been dropped at \"" + latitude + "," + longitude
						+ "\" location which is not the usual drop point.";
				adminContent = "Kindly note: Below Student has been dropped at \"" + latitude + "," + longitude
						+ "\" location which is not the usual drop point." + System.lineSeparator();
			} else {
				content = content + name + " has been dropped at location which is not the usual drop point.";
				adminContent = "Kindly note: Below Student has been dropped at location which is not the usual drop point."
						+ System.lineSeparator();
			}

		}
		String c = "Name:" + name + System.lineSeparator() + "Unique Key:" + uniqueKey + System.lineSeparator()
				+ "RouteID:" + routeID + System.lineSeparator() + "Route Name:" + routeName + System.lineSeparator()
				+ "Vehicle Schedule Name:" + vehicleScheduleName;
		adminContent = adminContent.concat(c);
		List<User> admins = adminDao.getUserListOfGivenRole(1);
		int countOfNotificationsStoredinDB = 0;
		Note adminNote = new Note(subject, adminContent);
		for (User admin : admins) {
			int success = adminDao.storeNotification(admin.getUserID(), 1, adminNote);
			if (success == 1) {
				countOfNotificationsStoredinDB++;
			}
			UserToken adminToken = driverDao.getUserToken(admin.getUserID());
			if (adminToken != null) {
firebaseService.sendNotification(adminNote, adminToken.getToken());
			}
		}
		if (countOfNotificationsStoredinDB == (admins.size())) {
			logger.debug("Notifications for all admins stored in DB.");
		} else {
			logger.debug("Notifications for some admins stored in DB.");
		}

		UserToken userToken = driverDao.getUserToken(userID);
		if (userToken == null) {
			logger.debug("user token is not there.");
			return;
		}
		Note note = new Note(subject, content);
		firebaseService.sendNotification(note, userToken.getToken());
		
		firebaseService.sendRefreshMessageToToken(userToken.getToken());

	}

	public ResponseEntity<Object> updateProfilePictureOfPassenger(int userID, FileURL fileURL) {
		try {
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidUserID2();
			}
			logger.debug("Valid user.");
			return adminDao.updateProfilePicture(userID, fileURL);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	public void unsubscribeFcmTokenOfPassengerFromTopicOnLogout(int userID, String fcmToken) {
		List<PassengerToRouteID> passengerToRouteDetails = passengerDao.getPassengerToRouteDetails(userID);
		List<Integer> vehicleScheduleIDs = passengerToRouteDetails.stream()
				.filter(obj -> obj.getVehicleSchedule() != null)
				.map(obj -> obj.getVehicleSchedule().getVehicleScheduleID()).collect(Collectors.toList());

		if (!vehicleScheduleIDs.isEmpty()) {

			for (Integer vehicleScheduleID : vehicleScheduleIDs) {
				firebaseService.unsubscribeFromPassengerScheduleTopic(vehicleScheduleID, fcmToken);
			}
		}

	}

	public ResponseEntity<Object> passengerHomescreen(int userID, int vehicleScheduleID, Integer tripDetailsID,
			LocalDate date) {
		try {
			if ((adminDao.getVehicleSchedule(vehicleScheduleID)) == null) {
				return InvalidData.invalidVehicleScheduleID1();
			}
			if (!(checkUserIsStudent(userID))) {
				return InvalidData.invalidPassengerID1();
			}
			List<PassengerToRouteID> list = passengerDao.getPassengerToRouteID(userID, vehicleScheduleID);
			if (list.isEmpty()) {
				return ResponseHandler.generateResponse1(false, "Invalid User ID for given vehicle schedule.",
						HttpStatus.NOT_FOUND, null);
			}
			TripDetails trip = null;
			if (tripDetailsID != null) {
				int tripID = tripDetailsID.intValue();
				trip = driverDao.getTripDetails(tripID);
				if (trip == null) {
					return InvalidData.invalidTripID1();
				}
				if (vehicleScheduleID != trip.getVehicleSchedule().getVehicleScheduleID()) {
					logger.debug("Invalid tripID for a user");
					return ResponseHandler.generateResponse1(false, "Invalid Trip ID for user.", HttpStatus.NOT_FOUND,
							null);
				}
			} else {
				trip = passengerDao.getTripCorrespondingToGivenVehicleSchedule(vehicleScheduleID, date);
			}
			PassengerHomescreen passengerHomescreen = getPassengerHomescreenObject(userID, trip, list.get(0));
			return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, passengerHomescreen);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, "Server Error!!!", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}

	}

	public PassengerHomescreen getPassengerHomescreenObject(int userID, TripDetails trip,
			PassengerToRouteID passengerToRouteIDObject) {
		String passengerStatusToBeShownAtPassengerStop;
		PassengerHomescreen passengerHomescreen = new PassengerHomescreen();
		if (trip == null) {
			// if trip not going on
			passengerHomescreen.setChildOnboardStatus(TRIP_NOT_STARTED);
			passengerHomescreen.setShowCompleteRouteLine(false);
			passengerStatusToBeShownAtPassengerStop = PASSENGER_NOT_BOARDED;
		} else {
			// if trip going on
			int tripDetailsID = trip.getTripDetailsID();
			int status = passengerDao.childOnboardStatusOfGivenTrip(userID, tripDetailsID);
			String output = null;
			if (status == 1) {
				output = PASSENGER_ONBOARDED;
			} else if (status == 2) {
				output = PASSENGER_DROPPED;
			} else if (status == 3) {
				output = PASSENGER_MISSED_BUS;
			} else if (status == 4) {
				output = PASSENGER_LEAVE;
			} else {
				output = PASSENGER_NOT_BOARDED;
			}
			passengerStatusToBeShownAtPassengerStop = output;
			passengerHomescreen.setChildOnboardStatus(output);
			if ((status == 2) || (status == 4)) {
				passengerHomescreen.setShowCompleteRouteLine(false);
			} else {
				passengerHomescreen.setShowCompleteRouteLine(true);
			}
		}
		int stopIDOfUser;
		if (passengerToRouteIDObject.getUserTypeOfJourneyID().getTypeOfJourney() == 1) {
			stopIDOfUser = passengerToRouteIDObject.getPickupPointStop().getStopID();
		} else {
			stopIDOfUser = passengerToRouteIDObject.getDropPointStop().getStopID();
		}
		List<StopScheduledTime> list = new ArrayList<>();
		List<Object[]> stopsOfScheduleAndTheirScheduledTime = adminDao
				.getRouteStopScheduleDetailsAsPerStopsOrderOfSchedule(passengerToRouteIDObject.getVehicleSchedule());
		for (Object[] stopSchedule : stopsOfScheduleAndTheirScheduledTime) {
			StopScheduledTime s = new StopScheduledTime();
			Stop stop = (Stop) stopSchedule[0];
			if ((stop.getStopID()) == stopIDOfUser) {
				s.setStatus(passengerStatusToBeShownAtPassengerStop);
				s.setLocation(YOUR_LOCATION);
			}
			s.setStop(stop);
			s.setStopOrder((int) (stopSchedule[1]));
			s.setScheduledArrivalTime((LocalTime) (stopSchedule[3]));
			s.setScheduledDepartureTime((LocalTime) (stopSchedule[2]));
			list.add(s);
		}
		passengerHomescreen.setList(list);
		passengerHomescreen.setScheduledTripStartTimeIfTripNotStarted(
				(trip == null) ? (passengerToRouteIDObject.getVehicleSchedule().getScheduledDepartureTime()) : null);
		return passengerHomescreen;
	}

}
