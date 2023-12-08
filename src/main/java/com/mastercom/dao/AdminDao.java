package com.mastercom.dao;

import static com.mastercom.constant.ApplicationConstant.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mastercom.embeddableclasses.ScheduleDateID;
import com.mastercom.embeddableclasses.TripStaffID;
import com.mastercom.embeddableclasses.UserTypeOfJourneyID;
import com.mastercom.embeddableclasses.VehicleScheduleDateStaffID;
import com.mastercom.entity.APICount;
import com.mastercom.entity.DeletedUser;
import com.mastercom.entity.FileInformation;
import com.mastercom.entity.Notification;
import com.mastercom.entity.NotificationOnOff;
import com.mastercom.entity.PassengerStatus;
import com.mastercom.entity.PassengerToRouteID;

import com.mastercom.entity.Role;
import com.mastercom.entity.Route;
import com.mastercom.entity.RouteStop;
import com.mastercom.entity.RouteStopSchedule;
import com.mastercom.entity.StaffToVehicleScheduleMultiStaff;
import com.mastercom.entity.Stop;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.User;
import com.mastercom.entity.UserToken;
import com.mastercom.entity.VehicleDetails;
import com.mastercom.entity.VehicleSchedule;
import com.mastercom.entity.VehicleToScheduleAssignment;
import com.mastercom.fcm.Note;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.AddStudent;
import com.mastercom.dto.ArrivalTimeAtStopOfSchedule;
import com.mastercom.dto.AttendantDTO;
import com.mastercom.dto.DriverDTO;
import com.mastercom.dto.FileURL;
import com.mastercom.dto.IDDate;
import com.mastercom.dto.OnwardReturnVehicleScheduleID;
import com.mastercom.dto.PassengerDTO;
import com.mastercom.dto.ResourcesAssignmentChangesToBeDone;
import com.mastercom.dto.RouteStopUpdate;
import com.mastercom.dto.RouteStopsDetails;
import com.mastercom.dto.RouteWithSourceDestination;
import com.mastercom.dto.Schedule;
import com.mastercom.dto.StopDetailsWithStopOrder;
import com.mastercom.dto.StopOrderDetails;
import com.mastercom.dto.StopSchedule;
import com.mastercom.dto.StudentRouteStop;
import com.mastercom.dto.StudentStopRouteUpdate;
import com.mastercom.dto.TokenOTP;
import com.mastercom.dto.TokenValue;
import com.mastercom.dto.VehicleDetailsDTO;
import com.mastercom.dto.VehicleScheduleIDName;
import com.mastercom.dto.VehicleIDRegisterationNumber;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Repository
public class AdminDao {
	@Autowired
	SessionFactory factory;

	@Value("${fileStoragePath}")
	private String fileStoragePath;

	private static final String SERVER_ERROR = "Server Error!!!";

	private static final Logger logger = LogManager.getLogger(AdminDao.class);
	public List<Integer> getVehicleSchedulesIDsAssignedToStaffToday(int userID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", LocalDate.now()).list();
		logger.debug("Today's vehicle schedules of driver are fetched.");
		session.close();
		return list;
	}
	public Role getRole(int roleID) {
		Session session = factory.openSession();
		Role role = session.get(Role.class, roleID);
		logger.debug("role details fetched.");
		session.close();
		return role;
	}

	public List<User> getUserListOfGivenRole(int roleID) {
		Session session = factory.openSession();
		List<User> users = session.get(Role.class, roleID).getUsers();
		logger.debug("users of given role are fetched");
		session.close();
		return users;
	}

	public User getUser(int userID) {
		Session session = factory.openSession();
		User user = session.get(User.class, userID);
		logger.debug("User details are fetched.");
		session.close();
		return user;
	}

	public List<Integer> getRoleIDs() {
		Session session = factory.openSession();
		List<Integer> roleIDs = session.createQuery("select r.roleID from Role r").list();
		logger.debug("Role IDs are fetched.");
		session.close();
		return roleIDs;
	}

	public List<User> getAllUsers() {
		Session session = factory.openSession();
		List<User> allUsers = session.createQuery("from User").list();
		logger.debug("List of all users is fetched.");
		session.close();
		return allUsers;
	}

	

	public List<Route> getRouteList() {
		Session session = factory.openSession();
		List<Route> routes = session.createQuery("from Route").list();
		logger.debug("Route list is fetched.");
		session.close();
		return routes;
	}

	public List<Stop> getStopsofRoute(int routeID) {
		Session session = factory.openSession();
		List<Stop> stops = session
				.createQuery("select r.stop from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder ASC")
				.setParameter("temp1", routeID).list();
		logger.debug("Stops of route are fetched.");
		session.close();
		return stops;
	}

	public List<RouteStop> getStopsWithStopOrderofRoute(int routeID) {
		Session session = factory.openSession();
		List<RouteStop> stopsWithStopOrder = session
				.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder ASC")
				.setParameter("temp1", routeID).list();
		logger.debug("Stops of route are fetched.");
		session.close();
		return stopsWithStopOrder;
	}

	public boolean validate(int pickupStopID, int pickupRouteID, int dropStopID, int dropRouteID) {
		Session session = factory.openSession();
		List<Integer> stopIDs = session.createQuery("select s.stopID from Stop s").list();
		List<Integer> routeIDs = session.createQuery("select r.routeID from Route r").list();
		session.close();
		if ((stopIDs.contains(pickupStopID)) && (routeIDs.contains(pickupRouteID)) && (stopIDs.contains(dropStopID))
				&& (routeIDs.contains(dropRouteID))) {
			logger.debug("Valid stopID and  routeID");
			return true;
		} else {
			logger.debug("Invalid stopID or routeID");
			return false;
		}
	}

	public ResponseEntity<Object> addVehicle(VehicleDetailsDTO vehicleDetailsDTO) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			VehicleDetails vehicle = new VehicleDetails();
			vehicle.setRegisterationNumber(vehicleDetailsDTO.getRegisterationNumber());
			vehicle.setVehicleType(vehicleDetailsDTO.getVehicleType());
			vehicle.setVehicleInsurance(vehicleDetailsDTO.getVehicleInsurance());
			vehicle.setRCbook(vehicleDetailsDTO.getRCbook());
			vehicle.setExpiryOfInsurance(vehicleDetailsDTO.getExpiryOfInsurance());
			vehicle.setExpiryOfFC(vehicleDetailsDTO.getExpiryOfFC());
			vehicle.setPurchasedDate(vehicleDetailsDTO.getPurchasedDate());
			vehicle.setNoOfSeats(vehicleDetailsDTO.getNoOfSeats());
			session.save(vehicle);
			tx.commit();
			logger.debug("Vehicle details added.");
			String output = "Vehicle Details added successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.CREATED);

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public boolean checkVehicleRegNo(String registerationNumber) {
		Session session = factory.openSession();
		List<String> regNos = session.createQuery("select v.registerationNumber from VehicleDetails v").list();
		session.close();
		if (regNos.contains(registerationNumber)) {
			logger.debug("vehicleRegisterationNumber is already present in db.");
			return true;
		} else {
			logger.debug("vehicleRegisterationNumber is not present in db.");
			return false;
		}

	}

	

	public Stop getStop(int stopID) {
		Session sesion = factory.openSession();
		Stop stop = sesion.get(Stop.class, stopID);
		logger.debug("Given Stop details are fetched.");
		sesion.close();
		return stop;
	}

	public Route getRoute(int routeID) {
		Session session = factory.openSession();
		Route route = session.get(Route.class, routeID);
		logger.debug("Given route details are fetched.");
		session.close();
		return route;
	}

	public ResponseEntity<Object> deleteRoute(int routeID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Route route1 = session.get(Route.class, routeID);
			session.delete(route1);
			session.createQuery("delete from RouteStop r where r.route.routeID=:routeID")
					.setParameter("routeID", routeID).executeUpdate();
			session.createQuery("delete from RouteStopSchedule r where r.route.routeID=:routeID")
					.setParameter("routeID", routeID).executeUpdate();

			tx.commit();
			logger.debug("Route is deleted.");
			String output = "Route deleted successfully";
			return ResponseHandler.generateResponse1(true, output, HttpStatus.OK, null);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();

			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse1(false, output, HttpStatus.INTERNAL_SERVER_ERROR, null);

		} finally {
			session.close();
		}
	}

	public List<RouteWithSourceDestination> getRouteListWithSourceAndDestination() {
		List<Route> routes = getRouteList();
		logger.debug("Routes are fetched.");
		List<RouteWithSourceDestination> routesS_D = new ArrayList<>();
		Session session = factory.openSession();
		for (Route route : routes) {
			RouteWithSourceDestination r = new RouteWithSourceDestination();
			int routeID = route.getRouteID();
			r.setRouteID(routeID);
			r.setRouteName(route.getRouteName());
			List<String> stopNames = session.createQuery(
					"select rs.stop.stopName from RouteStop rs where rs.route.routeID=:temp1 order by rs.stopOrder ASC")
					.setParameter("temp1", routeID).list();
			int n = stopNames.size();
			if (n != 0) {
				r.setSource(stopNames.get(0));
				r.setDestination(stopNames.get(n - 1));
			}
			routesS_D.add(r);
		}
		session.close();
		return routesS_D;
	}

	

	public ResponseEntity<Object> deleteStudent(int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			session.createQuery("delete from UserToken u where u.user.userID=:temp1").setParameter("temp1", userID)
					.executeUpdate();

			session.createQuery("delete from NotificationOnOff n where n.user.userID=:temp1")
					.setParameter("temp1", userID).executeUpdate();
			session.delete(user);
			DeletedUser deletedUser = new DeletedUser();
			deletedUser.setUserID(userID);
			deletedUser.setUserFirstName(user.getUserFirstName());
			deletedUser.setUserMiddleName(user.getUserMiddleName());
			deletedUser.setUserLastName(user.getUserLastName());
			deletedUser.setUserPhoneNumber(user.getUserPhoneNumber());
			deletedUser.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			deletedUser.setUserAddress(user.getUserAddress());
			deletedUser.setUserPhoto(user.getUserPhoto());
			deletedUser.setUserUniqueKey(user.getUserUniqueKey());
			deletedUser.setUserQRcode(user.getUserQRcode());
			deletedUser.setUserQRcodeString(user.getUserQRcodeString());
			deletedUser.setUserAge(user.getUserAge());
			deletedUser.setUserSex(user.getUserSex());
			deletedUser.setUserClass(user.getUserClass());
			deletedUser.setPriGuardian(user.getPriGuardian());
			deletedUser.setSecGuardian(user.getSecGuardian());
			deletedUser.setDrivingLicense(user.getDrivingLicense());
			deletedUser.setGovId(user.getGovId());
			deletedUser.setEmail(user.getEmail());
			deletedUser.setRole(session.get(Role.class, 3));
			deletedUser.setDate(LocalDate.now());
			session.save(deletedUser);
			// deleet from user_roles
			session.createQuery("delete from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1")
					.setParameter("temp1", userID).executeUpdate();
			tx.commit();
			logger.debug("Student is deleted.");
			String output = "Student data deleted succesfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}
	}

	// gives today and future vehicle schedules
	public List<Object[]> vehicleSchedulesAssignedToDriverForTodayAndFutureDates(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule, s.vehicleScheduleDateStaffID.date from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date >=: temp2 and s.staffType.roleID=4")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's and future's vehicle schedules assigned to driver are fetched.");
		session.close();
		return list;
	}

	public ResponseEntity<Object> deleteDriver(int userID) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			session.createQuery("delete from UserToken u where u.user.userID=:temp1").setParameter("temp1", userID)
					.executeUpdate();
			DeletedUser deletedUser = new DeletedUser();
			deletedUser.setUserID(userID);
			deletedUser.setUserFirstName(user.getUserFirstName());
			deletedUser.setUserMiddleName(user.getUserMiddleName());
			deletedUser.setUserLastName(user.getUserLastName());
			deletedUser.setUserPhoneNumber(user.getUserPhoneNumber());
			deletedUser.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			deletedUser.setUserAddress(user.getUserAddress());
			deletedUser.setUserPhoto(user.getUserPhoto());
			deletedUser.setUserUniqueKey(user.getUserUniqueKey());
			deletedUser.setUserQRcode(user.getUserQRcode());
			deletedUser.setUserQRcodeString(user.getUserQRcodeString());
			deletedUser.setUserAge(user.getUserAge());
			deletedUser.setUserSex(user.getUserSex());
			deletedUser.setUserClass(user.getUserClass());
			deletedUser.setPriGuardian(user.getPriGuardian());
			deletedUser.setSecGuardian(user.getSecGuardian());
			deletedUser.setDrivingLicense(user.getDrivingLicense());
			deletedUser.setGovId(user.getGovId());
			deletedUser.setEmail(user.getEmail());
			deletedUser.setRole(session.get(Role.class, 4));
			deletedUser.setDate(LocalDate.now());
			session.save(deletedUser);

			session.delete(user);
			tx.commit();
			logger.debug("Driver is deleted.");
			String output = "Driver data deleted succesfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		}

		catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	// gives today and future vehicle schedules
	public List<Object[]> vehicleSchedulesAssignedToAttendantForTodayAndFutureDates(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule, s.vehicleScheduleDateStaffID.date from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date>=:temp2 and s.staffType.roleID=5")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's and future's vehicle schedules assigned to attendant are fetched.");
		session.close();
		return list;
	}

	public ResponseEntity<Object> deleteAttendant(int userID) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			session.createQuery("delete from UserToken u where u.user.userID=:temp1").setParameter("temp1", userID)
					.executeUpdate();
			DeletedUser deletedUser = new DeletedUser();
			deletedUser.setUserID(userID);
			deletedUser.setUserFirstName(user.getUserFirstName());
			deletedUser.setUserMiddleName(user.getUserMiddleName());
			deletedUser.setUserLastName(user.getUserLastName());
			deletedUser.setUserPhoneNumber(user.getUserPhoneNumber());
			deletedUser.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			deletedUser.setUserAddress(user.getUserAddress());
			deletedUser.setUserPhoto(user.getUserPhoto());
			deletedUser.setUserUniqueKey(user.getUserUniqueKey());
			deletedUser.setUserQRcode(user.getUserQRcode());
			deletedUser.setUserQRcodeString(user.getUserQRcodeString());
			deletedUser.setUserAge(user.getUserAge());
			deletedUser.setUserSex(user.getUserSex());
			deletedUser.setUserClass(user.getUserClass());
			deletedUser.setPriGuardian(user.getPriGuardian());
			deletedUser.setSecGuardian(user.getSecGuardian());
			deletedUser.setDrivingLicense(user.getDrivingLicense());
			deletedUser.setGovId(user.getGovId());
			deletedUser.setEmail(user.getEmail());
			deletedUser.setRole(session.get(Role.class, 5));
			deletedUser.setDate(LocalDate.now());
			session.save(deletedUser);
			session.delete(user);
			tx.commit();
			logger.debug("Attendant is deleted.");
			String output = "Atendant data deleted succesfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<com.mastercom.dto.StudentRouteStop> getStudentList() {
		List<StudentRouteStop> students = new ArrayList<>();
		List<User> users = getUserListOfGivenRole(3);
		Session session = factory.openSession();
		for (User user : users) {
			int id = user.getUserID();
			Object[] obj1 = (Object[]) session.createQuery(
					"select p.pickupPointStop, p.route from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", id).setParameter("temp2", 1).list().get(0);
			Object[] obj2 = (Object[]) session.createQuery(
					"select p.dropPointStop, p.route from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", id).setParameter("temp2", 2).list().get(0);
			StudentRouteStop studentRouteStop = new StudentRouteStop(user, (Stop) obj1[0], (Route) obj1[1],
					(Stop) obj2[0], (Route) obj2[1]);
			students.add(studentRouteStop);
		}
		logger.debug("Student data with pickup and drop stops are fetched.");
		session.close();
		return students;
	}

	public List<User> usersHavingGivenRoute(int routeID) {
		Session session = factory.openSession();
		List<User> users = session.createQuery(
				"select distinct p.userTypeOfJourneyID.user from PassengerToRouteID p where p.route.routeID=:temp1")
				.setParameter("temp1", routeID).list();
		logger.debug("Passengers of given route are fetched.");
		session.close();
		return users;
	}

	public List<String> vehicleSchedulesHavingRoute(int routeID) {
		Session session = factory.openSession();
		List<String> vehicleScheduleNames = session
				.createQuery("select v.vehicleScheduleName from VehicleSchedule v where v.route.routeID=:temp1")
				.setParameter("temp1", routeID).list();
		logger.debug("Vehicle schedules of route are fetched.");
		session.close();
		return vehicleScheduleNames;
	}

	public List<VehicleSchedule> getVehicleScheduleList() {
		Session session = factory.openSession();
		List<VehicleSchedule> vehicleSchedules = session.createQuery("from VehicleSchedule").list();
		logger.debug("all Vehicle schedules are fetched.");
		session.close();
		return vehicleSchedules;
	}

	public List<String> getVehicleScheduleNamesList() {
		Session session = factory.openSession();
		List<String> scheduleNames = session.createQuery("select v.vehicleScheduleName from VehicleSchedule v").list();
		logger.debug("all Vehicle schedule names are fetched.");
		session.close();
		return scheduleNames;
	}

	public List<Integer> vehicleSchedulesAssignedToUsers(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<Integer> userIDs = session.createQuery(
				"select p.userTypeOfJourneyID.user.userID  from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();
		logger.debug("Vehicle schedule' passengers are fetched.");
		session.close();
		return userIDs;
	}

	public List<VehicleIDRegisterationNumber> getVehicle_ID_RegisterationNumberList() {
		Session session = factory.openSession();
		List<Object[]> list1 = session.createQuery("select v.vehicleID, v.registerationNumber from VehicleDetails v")
				.list();
		logger.debug("List of vehcleID and vehicle registration number is fetched.");
		List<VehicleIDRegisterationNumber> list2 = new ArrayList<>();
		for (Object[] obj : list1) {
			VehicleIDRegisterationNumber vehicle_ID_RegisterationNumber = new VehicleIDRegisterationNumber();
			vehicle_ID_RegisterationNumber.setVehicleID((int) obj[0]);
			vehicle_ID_RegisterationNumber.setRegistrationNumber((String) obj[1]);
			list2.add(vehicle_ID_RegisterationNumber);
		}

		session.close();
		return list2;
	}

	public int deleteVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			VehicleSchedule vehicleSchedule = session.get(VehicleSchedule.class, vehicleScheduleID);
			session.createQuery(
					"delete from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date >=:temp2")
					.setParameter("temp1", vehicleScheduleID).setParameter("temp2", LocalDate.now()).executeUpdate();
			session.delete(vehicleSchedule);
			session.createQuery("delete from RouteStopSchedule r where r.vehicleSchedule.vehicleScheduleID=:temp1")
					.setParameter("temp1", vehicleScheduleID).executeUpdate();
			tx.commit();
			logger.debug("Vehicle schedule deleted.");
			return 1;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return (-1);
		} finally {
			session.close();
		}
	}

	public List<Integer> getDriverAttendantListOfVehicleScheduleForCurrentAndFutureDates(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<Integer> driverAttendantList = session.createQuery(
				"select distinct s.vehicleScheduleDateStaffID.staff.userID from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date >=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", LocalDate.now()).list();
		session.close();
		return driverAttendantList;
	}

	public List<Integer> getVehicleIDs() {
		Session session = factory.openSession();
		List<Integer> vehicleIDs = session.createQuery("select v.vehicleID from VehicleDetails v").list();
		session.close();
		logger.debug("List of vehicleIDs is fetched.");
		return vehicleIDs;
	}

	public List<VehicleDetails> getVehicleDetailsOfGivenIDs(List<Integer> vehicleIDs) {
		Session session = factory.openSession();
		List<VehicleDetails> vehicles = session.createQuery("from VehicleDetails v where v.vehicleID in :temp1")
				.setParameter("temp1", vehicleIDs).list();
		session.close();
		logger.debug("Given vehicles data fetched.");
		return vehicles;
	}

	public ResponseEntity<Object> updateVehicle(int vehicleID, VehicleDetailsDTO vehicleDetailsDTO) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			VehicleDetails vehicle = session.get(VehicleDetails.class, vehicleID);
			vehicle.setRegisterationNumber(vehicleDetailsDTO.getRegisterationNumber());
			vehicle.setVehicleType(vehicleDetailsDTO.getVehicleType());
			vehicle.setVehicleInsurance(vehicleDetailsDTO.getVehicleInsurance());
			vehicle.setRCbook(vehicleDetailsDTO.getRCbook());
			vehicle.setExpiryOfInsurance(vehicleDetailsDTO.getExpiryOfInsurance());
			vehicle.setExpiryOfFC(vehicleDetailsDTO.getExpiryOfFC());
			vehicle.setPurchasedDate(vehicleDetailsDTO.getPurchasedDate());
			vehicle.setNoOfSeats(vehicleDetailsDTO.getNoOfSeats());
			session.update(vehicle);
			tx.commit();
			logger.debug("Vehicle details updated.");
			String output = "Vehicle details updated successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<Integer> getVehicleIDHavingRegNo(String registerationNumber) {
		Session session = factory.openSession();
		List<Integer> vehicleID = session
				.createQuery("select v.vehicleID from VehicleDetails v where v.registerationNumber=:temp1")
				.setParameter("temp1", registerationNumber).list();
		logger.debug("Vehicle details having given registration number is fetched.");
		session.close();
		return vehicleID;
	}

	public List<String> getVehicleShedulesHavingGivenVehicle(int vehicleID) {
		Session session = factory.openSession();
		List<String> vehicleSheduleNames = session.createQuery(
				"select distinct v.scheduleDateID.vehicleSchedule.vehicleScheduleName from VehicleToScheduleAssignment v where v.vehicleDetails.vehicleID=:temp1 and v.scheduleDateID.date>=:temp2")
				.setParameter("temp1", vehicleID).setParameter("temp2", LocalDate.now()).list();
		logger.debug("schedules nmaes are fetched having given vehicle for current or future date.");
		session.close();
		return vehicleSheduleNames;
	}

	public ResponseEntity<Object> deleteVehicle(int vehicleID) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			VehicleDetails vehicleDetails = session.get(VehicleDetails.class, vehicleID);
			session.delete(vehicleDetails);
			tx.commit();
			deleteFile(vehicleDetails.getRCbook());
			deleteFile(vehicleDetails.getVehicleInsurance());
			logger.debug("Vehicle details deleted.");
			String output = "Vehicle details deleted successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}
	}

	public List<TripDetails> getActiveTrips() {
		Session session = factory.openSession();
		List<TripDetails> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails from TripToStaff t where t.adminVerifiedTime is null")
				.list();
		logger.debug("Fetched active trips");
		session.close();
		return list;
	}

	public VehicleDetails getVehicle(int vehicleID) {
		Session session = factory.openSession();
		VehicleDetails vehicle = session.get(VehicleDetails.class, vehicleID);
		logger.debug("Vehicle details fetched.");
		session.close();
		return vehicle;
	}

	public List<VehicleScheduleIDName> getVehicleShedulesOfRoute(int routeID) {
		List<VehicleScheduleIDName> vehicleSchedule_ID_Names = new ArrayList<>();
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select v.vehicleScheduleID, v.vehicleScheduleName from VehicleSchedule v where v.route.routeID=:temp1")
				.setParameter("temp1", routeID).list();
		logger.debug("Vehicle schedules of given route are fetched.");
		session.close();
		for (Object[] obj : list) {
			vehicleSchedule_ID_Names.add(new VehicleScheduleIDName((int) obj[0], (String) obj[1]));
		}
		return vehicleSchedule_ID_Names;
	}

	public List<VehicleSchedule> getShedulesOfRoute(int routeID) {
		Session session = factory.openSession();
		List<VehicleSchedule> list = session.createQuery("from VehicleSchedule v where v.route.routeID=:temp1")
				.setParameter("temp1", routeID).list();
		logger.debug("Vehicle schedules of given route are fetched.");
		session.close();
		return list;
	}

	public List<Object[]> getVehicleShedulesWithPassengersCountOfGivenRouteForGivenTypeOfJourney(int routeID,
			int typeOfJourney) {
		Session session = factory.openSession();
		List<Integer> vehicleScheduleIDs = session.createQuery(
				"select v.vehicleScheduleID from VehicleSchedule v where v.route.routeID=:temp1 and v.typeOfJourney=:temp2")
				.setParameter("temp1", routeID).setParameter("temp2", typeOfJourney).list();
		List<Object[]> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID, p.vehicleSchedule.vehicleScheduleName, count(p.userTypeOfJourneyID.user.userID) from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID in :temp3 group by p.vehicleSchedule.vehicleScheduleID")
				.setParameterList("temp3", vehicleScheduleIDs).list();
		logger.debug("vehicle schedules of given route for given typeOfJourney are fetched.");
		session.close();
		return list;
	}

	public int assignVehicleScheduleToStudent(int userID,
			OnwardReturnVehicleScheduleID onward_Return_VehicleScheduleID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			PassengerToRouteID onwardJourneyPassengerToRouteID = (PassengerToRouteID) session.createQuery(
					"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", userID).setParameter("temp2", 1).list().get(0);
			PassengerToRouteID returnJourneyPassengerToRouteID = (PassengerToRouteID) session.createQuery(
					"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", userID).setParameter("temp2", 2).list().get(0);
			VehicleSchedule onwardVehicleSchedule = session.get(VehicleSchedule.class,
					onward_Return_VehicleScheduleID.getOnwardVehicleScheduleID());
			VehicleSchedule returnVehicleSchedule = session.get(VehicleSchedule.class,
					onward_Return_VehicleScheduleID.getReturnVehicleScheduleID());
			onwardJourneyPassengerToRouteID.setVehicleSchedule(onwardVehicleSchedule);

			returnJourneyPassengerToRouteID.setVehicleSchedule(returnVehicleSchedule);
			session.update(onwardJourneyPassengerToRouteID);
			session.update(returnJourneyPassengerToRouteID);
			tx.commit();
			logger.debug("Vehicle schedule assigned to passenger.");
			return 1;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return -1;
		} finally {
			session.close();
		}

	}

	public boolean checkValidUserIDInPassengerToRouteIDtable(int userID) {
		Session session = factory.openSession();
		List<User> list = session
				.createQuery("from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1")
				.setParameter("temp1", userID).list();
		session.close();
		if (!list.isEmpty()) {
			logger.debug("User is present in PassengerToRouteID table");
			return true;
		} else {
			logger.debug("User is not present in PassengerToRouteID table");
			return false;
		}
	}

	public ResponseEntity<Object> updateStudent(int userID, PassengerDTO passengerDTO, String qrCodeString,
			String qrCode) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user1 = session.get(User.class, userID);
			user1.setUserFirstName(passengerDTO.getUserFirstName());
			user1.setUserMiddleName(passengerDTO.getUserMiddleName());
			user1.setUserLastName(passengerDTO.getUserLastName());
			user1.setUserPhoneNumber(passengerDTO.getUserPhoneNumber());
			user1.setUserAlternatePhoneNumber(passengerDTO.getUserAlternatePhoneNumber());
			user1.setUserAddress(passengerDTO.getUserAddress());
			user1.setUserPhoto(passengerDTO.getUserPhoto());
			user1.setUserUniqueKey(passengerDTO.getUserUniqueKey());
			user1.setUserAge(passengerDTO.getUserAge());
			user1.setUserSex(passengerDTO.getUserSex());
			user1.setUserClass(passengerDTO.getUserClass());
			user1.setPriGuardian(passengerDTO.getPriGuardian());
			user1.setSecGuardian(passengerDTO.getSecGuardian());
			user1.setEmail(passengerDTO.getEmail());
			if (qrCode != null) {
				user1.setUserQRcodeString(qrCodeString);
				user1.setUserQRcode(qrCode);
			}
			session.update(user1);
			tx.commit();
			logger.debug("Passenger data updated.");
			String output = "Student data updated successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> updateStudentStopRoute(int userID, StudentStopRouteUpdate studentStopRouteUpdate) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			int flag = 0;
			tx = session.beginTransaction();
			PassengerToRouteID onwardJourneyPassengerToRouteID = (PassengerToRouteID) session.createQuery(
					"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", userID).setParameter("temp2", 1).list().get(0);
			PassengerToRouteID returnJourneyPassengerToRouteID = (PassengerToRouteID) session.createQuery(
					"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
					.setParameter("temp1", userID).setParameter("temp2", 2).list().get(0);
			if (!((onwardJourneyPassengerToRouteID.getRoute().getRouteID() == studentStopRouteUpdate.getOnwardRouteID())
					&& (returnJourneyPassengerToRouteID.getRoute().getRouteID() == studentStopRouteUpdate
							.getReturnRouteID()))) {
				flag = 1;
			}
			onwardJourneyPassengerToRouteID
					.setRoute(session.get(Route.class, studentStopRouteUpdate.getOnwardRouteID()));
			onwardJourneyPassengerToRouteID
					.setPickupPointStop(session.get(Stop.class, studentStopRouteUpdate.getOnwardPickupStopID()));
			onwardJourneyPassengerToRouteID
					.setDropPointStop(session.get(Stop.class, studentStopRouteUpdate.getOnwardDropStopID()));
			returnJourneyPassengerToRouteID
					.setRoute(session.get(Route.class, studentStopRouteUpdate.getReturnRouteID()));
			returnJourneyPassengerToRouteID
					.setPickupPointStop(session.get(Stop.class, studentStopRouteUpdate.getReturnPickupStopID()));
			returnJourneyPassengerToRouteID
					.setDropPointStop(session.get(Stop.class, studentStopRouteUpdate.getReturnDropStopID()));
			session.update(onwardJourneyPassengerToRouteID);
			session.update(returnJourneyPassengerToRouteID);
			tx.commit();
			logger.debug("Route Stop details of passenger updated.");
			String output = "Details updated successfully!!!";
			if (flag == 1) {
				output = output + "As route is changed, please assign correct vehicle Schedule to User.";
			}
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}
	}

	public List<TripDetails> getActiveRoutes(int typeOfJourney) {
		Session session = factory.openSession();
		List<TripDetails> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails from TripToStaff t where t.adminVerifiedTime is null and t.tripStaffID.tripDetails.vehicleSchedule.typeOfJourney=:temp1")
				.setParameter("temp1", typeOfJourney).list();
		logger.debug("Fetched active vehicle schedules");
		session.close();
		return list;
	}

	public int getCountOfStudentsOfGivenVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session
				.createQuery("from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();
		logger.debug("Passengers having given vehicle schedule are fetched.");
		session.close();
		return list.size();
	}

	public VehicleSchedule getVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		VehicleSchedule vehicleSchedule = session.get(VehicleSchedule.class, vehicleScheduleID);
		logger.debug("Vehicle Schedule fetched.");
		session.close();
		return vehicleSchedule;
	}

	public List<Integer> get_Picked_StudentsIDsListOfGivenTrip(int tripDetailsID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select p.tripUser.user.userID from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.userStatusCode.statusID=:temp2")
				.setParameter("temp1", tripDetailsID).setParameter("temp2", 1).list();
		logger.debug("Picked passengers details of trip are fetched.");
		session.close();
		return list;
	}

	public List<Integer> get_Dropped_StudentsIDsListOfGivenTrip(int tripDetailsID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select p.tripUser.user.userID from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.userStatusCode.statusID=:temp2")
				.setParameter("temp1", tripDetailsID).setParameter("temp2", 2).list();
		logger.debug("Dropped passengers details of trip are fetched.");
		session.close();
		return list;
	}

	public List<Integer> get_missedBus_StudentsIDsListOfGivenTrip(int tripDetailsID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select p.tripUser.user.userID from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.userStatusCode.statusID=:temp2")
				.setParameter("temp1", tripDetailsID).setParameter("temp2", 3).list();
		logger.debug("Missed bus passengers details of trip are fetched.");
		session.close();
		return list;
	}

	public List<Integer> get_scheduledLeave_StudentsIDsListOfGivenTrip(int tripDetailsID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select p.tripUser.user.userID from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.userStatusCode.statusID=:temp2")
				.setParameter("temp1", tripDetailsID).setParameter("temp2", 4).list();
		logger.debug("Scheduled leave passengers details of trip are fetched.");
		session.close();
		return list;
	}

	public List<Integer> getStudentIDsListOfGivenVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select p.userTypeOfJourneyID.user.userID from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();
		logger.debug("Given vehicle schedule mapped passengers are fetched.");
		session.close();
		return list;
	}

	public int getVehicleScheduleIDOfGivenTrip(int tripDetailsID) {
		Session session = factory.openSession();
		int vehicleScheduleID = (session.get(TripDetails.class, tripDetailsID)).getVehicleSchedule()
				.getVehicleScheduleID();
		logger.debug("Vehicle scheduleID of a trip is fetched.");
		session.close();
		return vehicleScheduleID;
	}

	public ResponseEntity<Object> fileUpload(MultipartFile multipartFile) {
		Session session = factory.openSession();
		// ......
		Transaction tx = null;
		// ........
		try {
			// ..................
			tx = session.beginTransaction();
			session.save(new FileInformation());
			List<Long> fileIDs = session.createQuery("select f.fileID from FileInformation f order by f.fileID asc")
					.list();
			int n = fileIDs.size();
			// TODO : check once uniqueness
			long i = 1;
			if (n != 0) {
				i = fileIDs.get(n - 1);
			}

			// ............
			String fileName1 = "" + i + multipartFile.getOriginalFilename();
			File convertFile = new File(fileStoragePath + fileName1);

			convertFile.createNewFile();
			FileOutputStream fout = new FileOutputStream(convertFile);
			fout.write(multipartFile.getBytes());
			fout.close();
			String output1 = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/trackgenie/common/download/")
					.path(fileName1).toUriString();
			// .........
			FileInformation f = session.get(FileInformation.class, i);
			f.setFileName(fileName1);
			f.setFileURL(output1);
			session.update(f);
			tx.commit();
			logger.debug("File uploaded.");
//.........................
			return ResponseHandler.generateResponse1(true, "File uploaded successfully!!!", HttpStatus.OK, output1);

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.OK, null);
		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> downloadFile(String f) {
		try {

			String filename = fileStoragePath + f;
			File file;
			InputStreamResource resource;
			try {
				file = new File(filename);
				resource = new InputStreamResource(new FileInputStream(file));
			} catch (FileNotFoundException e) {

				e.printStackTrace();
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				String s = errors.toString();
				logger.error("Exception =>  " + s);
				String str = "File not found.";
				return ResponseHandler.generateResponse1(false, str, HttpStatus.NOT_FOUND, null);
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", String.format("inline; filename=\"%s\"", file.getName()));
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			int n = f.length();
			ResponseEntity<Object> responseEntity = null;
			if (((f.substring(n - 4)).equalsIgnoreCase(".jpg")) || ((f.substring(n - 5)).equalsIgnoreCase(".jpeg"))) {
				responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
						.contentType(MediaType.IMAGE_JPEG).body(resource);

			} else if ((f.substring(n - 4)).equalsIgnoreCase(".pdf")) {
				responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
						.contentType(MediaType.APPLICATION_PDF).body(resource);

			} else if ((f.substring(n - 4)).equalsIgnoreCase(".png")) {
				responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
						.contentType(MediaType.IMAGE_PNG).body(resource);

			} else if ((f.substring(n - 4)).equalsIgnoreCase(".mp4")) {
				responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
						.contentType(MediaType.parseMediaType("video/mp4")).body(resource);

			}
			return responseEntity;
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public void deleteFile(String fileURL) {
		Session session = factory.openSession();
		try {
			List<String> list = session.createQuery("select f.fileName from FileInformation f where f.fileURL=:temp1")
					.setParameter("temp1", fileURL).list();
			if (list.isEmpty()) {
				logger.debug("File URL does not exist in database.");
			} else {
				String fileName = list.get(0);

				File file = new File(fileStoragePath + fileName);
				if (file.delete()) {
					Transaction tx = null;
					try {
						tx = session.beginTransaction();
						session.createQuery("delete from FileInformation f where f.fileURL=:temp1")
								.setParameter("temp1", fileURL).executeUpdate();
						tx.commit();
					} catch (Exception e) {
						if (tx != null) {
							tx.rollback();
						}
						e.printStackTrace();
						StringWriter errors = new StringWriter();
						e.printStackTrace(new PrintWriter(errors));
						String s = errors.toString();
						logger.error("Exception =>  " + s);
					}
					logger.debug("File is deleted.");
				} else {
					logger.debug("File is not deleted.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> deleteDriverWhoHasRoleInAdditionToDriver(int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			List<Role> roles = user.getRoles();
			Iterator<Role> itr2 = roles.iterator();
			while (itr2.hasNext()) {
				if (itr2.next().getRoleID() == 4) {
					itr2.remove();
				}
			}

			user.setRoles(roles);
			session.update(user);
			Role role = session.get(Role.class, 4);
			List<User> users = role.getUsers();
			if (!users.isEmpty()) {
				Iterator<User> itr = users.iterator();
				while (itr.hasNext()) {
					if (itr.next().getUserID() == userID) {
						itr.remove();
						break;
					}
				}
				role.setUsers(users);
			}
			session.update(role);
			DeletedUser deletedUser = new DeletedUser();
			deletedUser.setUserID(userID);
			deletedUser.setUserFirstName(user.getUserFirstName());
			deletedUser.setUserMiddleName(user.getUserMiddleName());
			deletedUser.setUserLastName(user.getUserLastName());
			deletedUser.setUserPhoneNumber(user.getUserPhoneNumber());
			deletedUser.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			deletedUser.setUserAddress(user.getUserAddress());
			deletedUser.setUserPhoto(user.getUserPhoto());
			deletedUser.setUserUniqueKey(user.getUserUniqueKey());
			deletedUser.setUserQRcode(user.getUserQRcode());
			deletedUser.setUserQRcodeString(user.getUserQRcodeString());
			deletedUser.setUserAge(user.getUserAge());
			deletedUser.setUserSex(user.getUserSex());
			deletedUser.setUserClass(user.getUserClass());
			deletedUser.setPriGuardian(user.getPriGuardian());
			deletedUser.setSecGuardian(user.getSecGuardian());
			deletedUser.setDrivingLicense(user.getDrivingLicense());
			deletedUser.setGovId(user.getGovId());
			deletedUser.setEmail(user.getEmail());
			deletedUser.setRole(session.get(Role.class, 4));
			deletedUser.setDate(LocalDate.now());
			session.save(deletedUser);
			tx.commit();
			logger.debug("Driver data deleted.");
			String output = "Driver data deleted succesfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> deleteAttendantWhoHasRoleInAdditionToAttendant(int userID) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			List<Role> roles = user.getRoles();
			Iterator<Role> itr2 = roles.iterator();
			while (itr2.hasNext()) {
				if (itr2.next().getRoleID() == 5) {
					itr2.remove();
				}
			}
			user.setRoles(roles);
			session.update(user);
			Role role = session.get(Role.class, 5);
			List<User> users = role.getUsers();
			if (!users.isEmpty()) {
				Iterator<User> itr = users.iterator();
				while (itr.hasNext()) {
					if (itr.next().getUserID() == userID) {
						itr.remove();
						break;
					}
				}
				role.setUsers(users);
			}
			session.update(role);
			DeletedUser deletedUser = new DeletedUser();
			deletedUser.setUserID(userID);
			deletedUser.setUserFirstName(user.getUserFirstName());
			deletedUser.setUserMiddleName(user.getUserMiddleName());
			deletedUser.setUserLastName(user.getUserLastName());
			deletedUser.setUserPhoneNumber(user.getUserPhoneNumber());
			deletedUser.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
			deletedUser.setUserAddress(user.getUserAddress());
			deletedUser.setUserPhoto(user.getUserPhoto());
			deletedUser.setUserUniqueKey(user.getUserUniqueKey());
			deletedUser.setUserQRcode(user.getUserQRcode());
			deletedUser.setUserQRcodeString(user.getUserQRcodeString());
			deletedUser.setUserAge(user.getUserAge());
			deletedUser.setUserSex(user.getUserSex());
			deletedUser.setUserClass(user.getUserClass());
			deletedUser.setPriGuardian(user.getPriGuardian());
			deletedUser.setSecGuardian(user.getSecGuardian());
			deletedUser.setDrivingLicense(user.getDrivingLicense());
			deletedUser.setGovId(user.getGovId());
			deletedUser.setEmail(user.getEmail());
			deletedUser.setRole(session.get(Role.class, 5));
			deletedUser.setDate(LocalDate.now());
			session.save(deletedUser);
			tx.commit();
			logger.debug("Attendant data deleted.");
			String output = "Attendant data deleted succesfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
			return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<Integer> getStopIDOfRoute(int routeID) {
		Session session = factory.openSession();
		List<Integer> stopIDs = session
				.createQuery(
						"select r.stop.stopID from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder asc")
				.setParameter("temp1", routeID).list();
		logger.debug("StopIDs of route are fetched.");
		session.close();
		return stopIDs;
	}

	

	public List<RouteStopSchedule> getRouteStopSchedule(int vehicleSchduleID) {
		Session session = factory.openSession();
		List<RouteStopSchedule> list = session
				.createQuery("from RouteStopSchedule r where r.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleSchduleID).list();
		logger.debug("RouteStop schedule of given vehicleSchdule is fetched.");
		session.close();
		return list;
	}

	public List<Integer> getStopIDsOfScheduleWhoseScheduledTimesAreMapped(int vehicleSchduleID) {
		Session session = factory.openSession();
		List<Integer> list = session.createQuery(
				"select r.stop.stopID from RouteStopSchedule r where r.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleSchduleID).list();
		logger.debug("RouteStop schedule of given vehicleSchdule is fetched.");
		session.close();
		return list;
	}

	

	public List<TripDetails> fetchTripsOfGivenDateAndTypeOfJourney(LocalDate date, int typeOfJourney) {
		Session session = factory.openSession();
		LocalDateTime startDateTime = date.atStartOfDay();
		LocalDateTime endDateTime = date.plusDays(1L).atStartOfDay();
		List<TripDetails> list = session.createQuery(
				"from TripDetails t where t.vehicleSchedule.typeOfJourney=:temp1 and t.tripStart>=:temp2 and t.tripStart<:temp3")
				.setParameter("temp1", typeOfJourney).setParameter("temp2", startDateTime)
				.setParameter("temp3", endDateTime).list();
		logger.debug("Trips data of given date and typeOfJourney is fetched.");
		session.close();
		return list;
	}

	// checked
	public List<Integer> fetchTripIDsOfGivenDateAndTypeOfJourney(LocalDate date, int typeOfJourney) {
		Session session = factory.openSession();
		LocalDateTime startDateTime = date.atStartOfDay();
		LocalDateTime endDateTime = date.plusDays(1L).atStartOfDay();
		List<Integer> list = session.createQuery(
				"select t.tripDetailsID from TripDetails t where t.vehicleSchedule.typeOfJourney=:temp1 and t.tripStart>=:temp2 and t.tripStart<:temp3")
				.setParameter("temp1", typeOfJourney).setParameter("temp2", startDateTime)
				.setParameter("temp3", endDateTime).list();
		logger.debug("Trips ids of given date and typeOfJourney is fetched.");
		session.close();
		return list;
	}

	public String generateQRcode(String str) {
		Session session = factory.openSession();
		// ......
		Transaction tx = null;
		// ........
		try {
			// ..................
			tx = session.beginTransaction();
			session.save(new FileInformation());
			List<Long> fileIDs = session.createQuery("select f.fileID from FileInformation f order by f.fileID asc")
					.list();
			int n = fileIDs.size();
			long i = 1;
			if (n != 0) {
				i = fileIDs.get(n - 1);
			}

			// ............
			String fileName1 = "" + i + "QRcode" + ".png";

			// final
			File convertFile = new File(fileStoragePath + fileName1);
			String path = fileStoragePath + fileName1;
			convertFile.createNewFile();

			String charset = "UTF-8";
			Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
			hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			generateQRcode1(str, path, charset, hashMap, 200, 200);// increase or decrease height and width accodingly
			//
			String output1 = "https://trackgenie-do.mastercomglobal.com/api/"+"/trackgenie/common/download/"+
					fileName1;
			// .........
			FileInformation f = session.get(FileInformation.class, i);
			f.setFileName(fileName1);
			f.setFileURL(output1);
			session.update(f);
			tx.commit();
			logger.debug("QR code generated.");
//.........................
			return output1;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return null;
		} finally {
			session.close();
		}
	}

	public void generateQRcode1(String data, String path, String charset, Map map, int h, int w)
			throws WriterException, IOException {
		// the BitMatrix class represents the 2D matrix of bits
		// MultiFormatWriter is a factory class that finds the appropriate Writer
		// subclass for the BarcodeFormat requested and encodes the barcode with the
		// supplied contents.
		BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, w, h);
		MatrixToImageWriter.writeToPath(matrix, path.substring(path.lastIndexOf('.') + 1), (new File(path)).toPath());
	}

	public String storeTokenValue(int userID, TokenValue tokenValue) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			UserToken userToken = new UserToken(session.get(User.class, userID), tokenValue.getToken());
			session.saveOrUpdate(userToken);
			tx.commit();
			logger.debug("Token saved.");
			return "Successfully saved token.";
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return null;
		} finally {
			session.close();
		}

	}

	public List<RouteStop> getRouteStopOrderOfRoute(int routeID) {
		Session session = factory.openSession();
		List<RouteStop> stops = session
				.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder asc")
				.setParameter("temp1", routeID).list();

		session.close();
		return stops;

	}

	public int adminVerifiedVideoUploadedByGivenUser(int tripDetailsID, int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			int flag = 1;
			tx = session.beginTransaction();
			TripDetails trip = session.get(TripDetails.class, tripDetailsID);
			User user = session.get(User.class, userID);
			TripStaffID tripStaffID = new TripStaffID(trip, user);
			TripToStaff tripToStaff = session.get(TripToStaff.class, tripStaffID);
			tripToStaff.setAdminVerifiedTime(LocalDateTime.now());
			session.update(tripToStaff);
			List<Integer> allActiveTripIDs = session.createQuery(
					"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.adminVerifiedTime is null")
					.list();
			if (!(allActiveTripIDs.contains(tripDetailsID))) {
				flag = 2;
				session.createQuery("delete from CurrentLatLong c where c.tripDetails.tripDetailsID=:temp1")
						.setParameter("temp1", tripDetailsID).executeUpdate();
				session.createQuery(
						"delete from TimeRequiredToReachAtStop t where t.tripStopID.tripDetails.tripDetailsID=:temp1")
						.setParameter("temp1", tripDetailsID).executeUpdate();
			}
			tx.commit();

			logger.debug("Verification Time recorded!");
			return flag;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return (-1);

		} finally {
			session.close();
		}

	}

	public int adminClickedOnVerifyAllVideos(int tripDetailsID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.createQuery(
					"update TripToStaff t set t.adminVerifiedTime=:temp1 where t.tripStaffID.tripDetails.tripDetailsID=:temp2 and t.adminVerifiedTime is null")
					.setParameter("temp1", LocalDateTime.now()).setParameter("temp2", tripDetailsID).executeUpdate();
			session.createQuery("delete from CurrentLatLong c where c.tripDetails.tripDetailsID=:temp1")
					.setParameter("temp1", tripDetailsID).executeUpdate();
			session.createQuery(
					"delete from TimeRequiredToReachAtStop t where t.tripStopID.tripDetails.tripDetailsID=:temp1")
					.setParameter("temp1", tripDetailsID).executeUpdate();
			tx.commit();
			logger.debug("Verification Time recorded.");
			return 1;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return (-1);

		} finally {
			session.close();
		}

	}

	public ResponseEntity<Object> addNewDriver(DriverDTO driver, String tempPassword) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			User user = new User();
			user.setUserFirstName(driver.getUserFirstName());
			user.setUserMiddleName(driver.getUserMiddleName());
			user.setUserLastName(driver.getUserLastName());
			user.setUserPhoneNumber(driver.getUserPhoneNumber());
			user.setUserAlternatePhoneNumber(driver.getUserAlternatePhoneNumber());
			user.setUserAddress(driver.getUserAddress());
			user.setUserPhoto(driver.getUserPhoto());
			user.setUserUniqueKey(driver.getUserUniqueKey());
			user.setUserAge(driver.getUserAge());
			user.setUserSex(driver.getUserSex());
			user.setDrivingLicense(driver.getDrivingLicense());
			user.setGovId(driver.getGovId());
			user.setEmail(driver.getEmail());
			user.setTempPassword(tempPassword);
			user.setTempPasswordCreationTimeStamp(LocalDateTime.now());

			Role role = session.get(Role.class, 4);
			List<Role> roles = new ArrayList<>();
			roles.add(role);
			user.setRoles(roles);
			session.save(user);
			List<User> users = role.getUsers();
			users.add(user);
			role.setUsers(users);
			session.update(role);
			tx.commit();
			logger.debug("New user added.");
			String output = "New User added successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.CREATED);
		} catch (Exception e) {

			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}

	}

	public ResponseEntity<Object> addNewAttendant(AttendantDTO attendantDTO, String tempPassword) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			User user = new User();
			user.setUserFirstName(attendantDTO.getUserFirstName());
			user.setUserMiddleName(attendantDTO.getUserMiddleName());
			user.setUserLastName(attendantDTO.getUserLastName());
			user.setUserPhoneNumber(attendantDTO.getUserPhoneNumber());
			user.setUserAlternatePhoneNumber(attendantDTO.getUserAlternatePhoneNumber());
			user.setUserAddress(attendantDTO.getUserAddress());
			user.setUserPhoto(attendantDTO.getUserPhoto());
			user.setUserUniqueKey(attendantDTO.getUserUniqueKey());
			user.setUserAge(attendantDTO.getUserAge());
			user.setUserSex(attendantDTO.getUserSex());
			user.setGovId(attendantDTO.getGovId());
			user.setEmail(attendantDTO.getEmail());
			user.setTempPassword(tempPassword);
			user.setTempPasswordCreationTimeStamp(LocalDateTime.now());

			Role role = session.get(Role.class, 5);
			List<Role> roles = new ArrayList<>();
			roles.add(role);
			user.setRoles(roles);
			session.save(user);
			List<User> users = role.getUsers();
			users.add(user);
			role.setUsers(users);
			session.update(role);
			tx.commit();
			logger.debug("New user added.");
			String output = "New User added successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.CREATED);
		} catch (Exception e) {

			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}

	}

	public ResponseEntity<Object> addStudent(AddStudent addStudent, String qrCode, String qrCodeString,
			String tempPassword) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			User user = new User();
			user.setUserFirstName(addStudent.getUserFirstName());
			user.setUserMiddleName(addStudent.getUserMiddleName());
			user.setUserLastName(addStudent.getUserLastName());
			user.setUserUniqueKey(addStudent.getUserUniqueKey());
			user.setUserPhoneNumber(addStudent.getUserPhoneNumber());
			user.setUserAlternatePhoneNumber(addStudent.getUserAlternatePhoneNumber());
			user.setUserAddress(addStudent.getUserAddress());
			user.setUserPhoto(addStudent.getUserPhoto());
			user.setUserAge(addStudent.getUserAge());
			user.setUserSex(addStudent.getUserSex());
			user.setUserClass(addStudent.getUserClass());
			user.setPriGuardian(addStudent.getPriGuardian());
			user.setSecGuardian(addStudent.getSecGuardian());
			user.setEmail(addStudent.getEmail());
			user.setUserQRcode(qrCode);
			user.setUserQRcodeString(qrCodeString);
			user.setTempPassword(tempPassword);
			user.setTempPasswordCreationTimeStamp(LocalDateTime.now());

			Role role = session.get(Role.class, 3);
			List<Role> roles = new ArrayList<>();
			roles.add(role);
			user.setRoles(roles);
			session.save(user);
			List<User> users = role.getUsers();
			users.add(user);
			role.setUsers(users);
			session.update(role);

			PassengerToRouteID passengerOnwardRoute = new PassengerToRouteID();
			UserTypeOfJourneyID userJourneyOnward = new UserTypeOfJourneyID();
			userJourneyOnward.setUser(user);
			userJourneyOnward.setTypeOfJourney(1);
			passengerOnwardRoute.setUserTypeOfJourneyID(userJourneyOnward);
			passengerOnwardRoute.setRoute(session.get(Route.class, addStudent.getOnwardRouteID()));
			passengerOnwardRoute.setPickupPointStop(session.get(Stop.class, addStudent.getOnwardPickupStopID()));
			passengerOnwardRoute.setDropPointStop(session.get(Stop.class, addStudent.getOnwardDropStopID()));
			session.save(passengerOnwardRoute);

			PassengerToRouteID passengerReturnRoute = new PassengerToRouteID();
			UserTypeOfJourneyID userJourneyReturn = new UserTypeOfJourneyID();
			userJourneyReturn.setUser(user);
			userJourneyReturn.setTypeOfJourney(2);
			passengerReturnRoute.setUserTypeOfJourneyID(userJourneyReturn);
			passengerReturnRoute.setRoute(session.get(Route.class, addStudent.getReturnRouteID()));
			passengerReturnRoute.setPickupPointStop(session.get(Stop.class, addStudent.getReturnPickupStopID()));
			passengerReturnRoute.setDropPointStop(session.get(Stop.class, addStudent.getReturnDropStopID()));
			session.save(passengerReturnRoute);

			NotificationOnOff notificationONOFF = new NotificationOnOff(user, 1, 1, 1, 1, 1, 1, 1);
			session.save(notificationONOFF);

			tx.commit();
			logger.debug("New passenger added successfully with pickup and drop stops.");
			String output = "New User added successfully with pickup and drop stops!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.CREATED);
		} catch (Exception e) {

			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}

	}

	public ResponseEntity<Object> updateDriver(int userID, DriverDTO driverDTO) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user1 = session.get(User.class, userID);
			user1.setUserFirstName(driverDTO.getUserFirstName());
			user1.setUserMiddleName(driverDTO.getUserMiddleName());
			user1.setUserLastName(driverDTO.getUserLastName());
			user1.setUserPhoneNumber(driverDTO.getUserPhoneNumber());
			user1.setUserAlternatePhoneNumber(driverDTO.getUserAlternatePhoneNumber());
			user1.setUserAddress(driverDTO.getUserAddress());
			user1.setUserPhoto(driverDTO.getUserPhoto());
			user1.setUserUniqueKey(driverDTO.getUserUniqueKey());
			user1.setUserAge(driverDTO.getUserAge());
			user1.setUserSex(driverDTO.getUserSex());
			user1.setDrivingLicense(driverDTO.getDrivingLicense());
			user1.setGovId(driverDTO.getGovId());
			user1.setEmail(driverDTO.getEmail());
			session.update(user1);
			tx.commit();
			logger.debug("User details updated.");
			String output = "User details updated successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public ResponseEntity<Object> updateAttendant(int userID, AttendantDTO attendantDTO) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user1 = session.get(User.class, userID);
			user1.setUserFirstName(attendantDTO.getUserFirstName());
			user1.setUserMiddleName(attendantDTO.getUserMiddleName());
			user1.setUserLastName(attendantDTO.getUserLastName());
			user1.setUserPhoneNumber(attendantDTO.getUserPhoneNumber());
			user1.setUserAlternatePhoneNumber(attendantDTO.getUserAlternatePhoneNumber());
			user1.setUserAddress(attendantDTO.getUserAddress());
			user1.setUserPhoto(attendantDTO.getUserPhoto());
			user1.setUserUniqueKey(attendantDTO.getUserUniqueKey());
			user1.setUserAge(attendantDTO.getUserAge());
			user1.setUserSex(attendantDTO.getUserSex());
			user1.setGovId(attendantDTO.getGovId());
			user1.setEmail(attendantDTO.getEmail());
			session.update(user1);
			tx.commit();
			logger.debug("User details updated.");
			String output = "User details updated successfully!!!";
			return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<PassengerToRouteID> getPassengerToRouteIDDetails(int userID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> data = session
				.createQuery("from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1")
				.setParameter("temp1", userID).list();
		session.close();
		return data;
	}

	public List<User> getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates(int roleID,
			int vehicleScheduleID) {
		Session session = factory.openSession();
		logger.debug("Fetching data.");
		List<User> list = session.createQuery(
				"select distinct s.vehicleScheduleDateStaffID.staff from StaffToVehicleScheduleMultiStaff s where s.staffType.roleID=:temp1 and s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp2 and s.vehicleScheduleDateStaffID.date>=:temp3")
				.setParameter("temp1", roleID).setParameter("temp2", vehicleScheduleID)
				.setParameter("temp3", LocalDate.now()).list();
		session.close();
		return list;
	}

	public int updateOTP(int userID, int otp) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setOtp(otp);
			session.update(user);
			tx.commit();
			return 1;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return (-1);

		} finally {
			session.close();
		}

	}

	public int storeTokenValueAndRemoveOTP(int userID, TokenOTP tokenOTP) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setOtp(null);
			session.update(user);
			UserToken userToken = session.get(UserToken.class, userID);
			userToken.setToken(tokenOTP.getToken());
			session.update(userToken);
			tx.commit();
			return 1;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);

			return (-1);

		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> updateProfilePicture(int userID, FileURL fileURL) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setUserPhoto(fileURL.getUrl());
			session.update(user);
			tx.commit();
			logger.debug("Profile picture updated.");
			return ResponseHandler.generateResponse2(true, "Profile picture updated!!!", HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}

	}

	

	public List<Notification> getNotifications(int userID, int roleID) {
		Session session = factory.openSession();
		List<Notification> list = session
				.createQuery("from Notification n where n.user.userID=:temp1 and n.role.roleID=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", roleID).list();
		logger.debug("Notifications fetched.");
		session.close();
		return list;
	}

	public Notification getNotification(int id) {
		Session session = factory.openSession();
		Notification notification = session.get(Notification.class, id);
		logger.debug("Notification data fetched.");
		session.close();
		return notification;
	}

	public ResponseEntity<Object> deleteNotification(int id) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Notification notification = session.get(Notification.class, id);
			session.delete(notification);
			tx.commit();
			logger.debug("Notification deleted.");
			return ResponseHandler.generateResponse2(true, "Notification deleted.", HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}
	}

	public int storeNotification(int userID, int roleID, Note note) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			Role role = session.get(Role.class, roleID);
			Notification notification = new Notification();
			notification.setUser(user);
			notification.setRole(role);
			notification.setSubject(note.getSubject());
			notification.setContent(note.getContent());
			notification.setDateTime(LocalDateTime.now());
			session.save(notification);
			tx.commit();
			logger.debug("Notification stored in DB");
			return 1;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return (-1);
		} finally {
			session.close();
		}

	}

	public List<String> getTokensOfVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<String> list = session.createQuery(
				"select t.token from UserToken t inner join PassengerToRouteID p with t.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();
		session.close();
		return list;
	}

	public List<String> getTokensOfPassengersWhoEnabledNotificationOfTripVerifiedByAdmin(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<String> list = session.createQuery(
				"select t.token from NotificationOnOff n inner join PassengerToRouteID p with n.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1 and n.tripVerifiedByAdmin=1 inner join UserToken t with n.user.userID=t.user.userID")
				.setParameter("temp1", vehicleScheduleID).list();
		session.close();
		return list;
	}

//public List<Integer> getidsOfPassengersWhoEnabledNotificationOfTripVerifiedByAdmin(int vehicleScheduleID){
//	Session session=factory.openSession();
//	System.err.println("//////");
//	List<Integer> list=session.createQuery("select t.user.userID from NotificationOnOff n inner join PassengerToRouteID p with n.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1 and n.tripVerifiedByAdmin=1 inner join UserToken t with n.user.userID=t.user.userID").setParameter("temp1", vehicleScheduleID).list();
//	//List<Integer> list=session.createQuery("select n.user.userID from NotificationOnOff n inner join n.user ").list();
//	System.err.println("//////");
//	session.close();
//	return list;
//}

	public List<User> getUsersHavingUserIDs(List<Integer> userIDs) {
		Session session = factory.openSession();
		List<User> list = session.createQuery("from User u where u.userID in :temp1").setParameterList("temp1", userIDs)
				.list();
		session.close();
		return list;
	}

	public User getUserHavingGivenUniqueKey(String userUniqueKey) {
		Session session = factory.openSession();
		List<User> users = session.createQuery("from User u where u.userUniqueKey=:temp1")
				.setParameter("temp1", userUniqueKey).list();
		logger.debug("Fetched");
		session.close();
		return users.isEmpty() ? null : users.get(0);
	}

	

	

	public ResponseEntity<Object> addRouteAndItsStops(RouteStopsDetails routeStopsDetails) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Route route = new Route();
			route.setRouteID(routeStopsDetails.getRouteID());
			route.setRouteName(routeStopsDetails.getRouteName());
			session.save(route);
			List<StopDetailsWithStopOrder> stopsWithStopOrder = routeStopsDetails.getStopsWithStopOrder();

			for (StopDetailsWithStopOrder obj : stopsWithStopOrder) {
				Stop stop = new Stop();
				stop.setStopName(obj.getStopName());
				stop.setStopAddress(obj.getStopAddress());
				stop.setStopLatitude(obj.getStopLatitude());
				stop.setStopLongitude(obj.getStopLongitude());
				session.save(stop);
				RouteStop routeStop = new RouteStop();
				routeStop.setStop(stop);
				routeStop.setRoute(route);
				routeStop.setStopOrder(obj.getStopOrder());
				session.save(routeStop);

			}

			tx.commit();
			logger.debug("Route and its stops created successfully!!!");
			return ResponseHandler.generateResponse2(true, "Route and its stops created successfully!!!",
					HttpStatus.CREATED);

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}
	}

	public ResponseEntity<Object> addSchedule(Schedule schedule, StopSchedule startStopSchedule) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Route route = session.get(Route.class, schedule.getRouteID());
			VehicleSchedule vehicleSchedule = new VehicleSchedule();
			vehicleSchedule.setVehicleScheduleName(schedule.getVehicleScheduleName());
			vehicleSchedule.setTypeOfJourney(schedule.getTypeOfJourney());
			vehicleSchedule.setRoute(route);
			vehicleSchedule.setBlockingTimeInMinutes(schedule.getBlockingTimeInMinutes());
			List<StopSchedule> stopSchedules = schedule.getStopSchedules();
			if (startStopSchedule != null) {
				vehicleSchedule.setScheduledArrivalTime(LocalTime.parse(startStopSchedule.getScheduledArrivalTime()));
				vehicleSchedule
						.setScheduledDepartureTime(LocalTime.parse(startStopSchedule.getScheduledDepartureTime()));
			}
			session.save(vehicleSchedule);

			for (StopSchedule stopSchedule : stopSchedules) {
				RouteStopSchedule routeStopSchedule = new RouteStopSchedule();
				routeStopSchedule.setVehicleSchedule(vehicleSchedule);
				routeStopSchedule.setRoute(route);
				Stop stop = session.get(Stop.class, stopSchedule.getStopID());
				routeStopSchedule.setStop(stop);
				routeStopSchedule.setScheduledArrivalTime(LocalTime.parse(stopSchedule.getScheduledArrivalTime()));
				routeStopSchedule.setScheduledDepartureTime(LocalTime.parse(stopSchedule.getScheduledDepartureTime()));
				session.save(routeStopSchedule);
			}
			tx.commit();
			logger.debug("Schedule created successfully!!!");
			return ResponseHandler.generateResponse1(true, "Schedule created successfully!!!", HttpStatus.CREATED,
					vehicleSchedule.getVehicleScheduleID());

		} catch (org.hibernate.exception.ConstraintViolationException e1) {
			if (tx != null) {
				tx.rollback();
			}
			List<String> scheduleNames = session.createQuery("select v.vehicleScheduleName from VehicleSchedule v")
					.list();
			if (scheduleNames.contains(schedule.getVehicleScheduleName())) {
				logger.debug("Duplicate schedule name. Please try with another schedule name.");
				return ResponseHandler.generateResponse1(false,
						"Duplicate schedule name. Please try with another schedule name.", HttpStatus.OK, null);
			}
			e1.printStackTrace();
			StringWriter errors = new StringWriter();
			e1.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);

		} finally {
			session.close();
		}

	}

	// checked
	public List<PassengerStatus> getPassengerStatusOfTrips(List<Integer> tripsIDs) {
		Session session = factory.openSession();
		List<PassengerStatus> list = session.createQuery(
				"from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID in :temp1 order by p.tripUser.tripDetails.tripDetailsID asc")
				.setParameter("temp1", tripsIDs).list();
		session.close();
		logger.debug("Passenger data fetched for given trips.");
		return list;
	}

	public List<PassengerStatus> getPassengerStatusOfTrip(int tripID) {
		Session session = factory.openSession();
		List<PassengerStatus> list = session
				.createQuery("from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1")
				.setParameter("temp1", tripID).list();
		session.close();
		return list;
	}

	public List<TripToStaff> getStaffDataOfTrips(List<Integer> activeTripsIDs) {
		Session session = factory.openSession();
		List<TripToStaff> list = session
				.createQuery("from TripToStaff t where t.tripStaffID.tripDetails.tripDetailsID in :temp1")
				.setParameter("temp1", activeTripsIDs).list();
		session.close();
		return list;
	}

	public HashMap<Integer, HashMap<LocalDate, List<StaffToVehicleScheduleMultiStaff>>> getStaffOfVehicleSchedulesForGivenDates(
			HashMap<Integer, List<LocalDate>> vehicleSchedulesDates) {
		HashMap<Integer, HashMap<LocalDate, List<StaffToVehicleScheduleMultiStaff>>> vehicleScheduleDateStaffMapping = new HashMap<>();

		Session session = factory.openSession();
		Set<Integer> keys = vehicleSchedulesDates.keySet();
		for (Integer key : keys) {
			HashMap<LocalDate, List<StaffToVehicleScheduleMultiStaff>> staffDataOfGivenScheduleForGivenDates = new HashMap<>();
			List<LocalDate> dates = vehicleSchedulesDates.get(key);
			for (LocalDate date : dates) {
				List<StaffToVehicleScheduleMultiStaff> list = session.createQuery(
						"from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2")
						.setParameter("temp1", key).setParameter("temp2", date).list();
				staffDataOfGivenScheduleForGivenDates.put(date, list);
			}
			vehicleScheduleDateStaffMapping.put(key, staffDataOfGivenScheduleForGivenDates);
		}

		session.close();
		return vehicleScheduleDateStaffMapping;
	}

	public List<StaffToVehicleScheduleMultiStaff> getStaffOfVehicleSchedule(int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		List<StaffToVehicleScheduleMultiStaff> list = session.createQuery(
				"from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", date).list();
		session.close();
		return list;
	}

	public List<PassengerToRouteID> getAllPassengersIDsOfGivenVehicleSchedules(
			HashSet<Integer> activeVehicleScheduleIDs) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session
				.createQuery("from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID in :temp1")
				.setParameter("temp1", activeVehicleScheduleIDs).list();
		session.close();
		return list;
	}

	public boolean saveTokensAndDeviceID(int userID, String jwtToken, String fcmToken, String deviceID,
			boolean showPasswordSettingScreen) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setJwtToken(jwtToken);
			user.setDeviceID(deviceID);
			user.setOtp(null);
			if (showPasswordSettingScreen) {
				user.setTempPassword(null);
			}
			session.update(user);
			UserToken userToken = session.get(UserToken.class, userID);
			if (userToken != null) {
				userToken.setToken(fcmToken);
				session.update(userToken);
			} else {
				UserToken userTokenObj = new UserToken();
				userTokenObj.setUser(user);
				userTokenObj.setToken(fcmToken);
				session.save(userTokenObj);
			}

			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return false;
		} finally {
			session.close();
		}
	}

	public boolean saveOTP(Integer userID, String otp) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setOtp(Integer.valueOf(otp));
			user.setOtpGenerationDateTime(LocalDateTime.now());
			session.update(user);
			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return false;
		} finally {
			session.close();
		}
	}

	public List<PassengerToRouteID> passengerAssignedToStops(List<Integer> stopIDs) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session.createQuery(
				"from PassengerToRouteID p where p.pickupPointStop.stopID in (:temp1) or p.dropPointStop.stopID in (:temp1)")
				.setParameterList("temp1", stopIDs).list();
		session.close();
		return list;
	}

	public ResponseEntity<Object> updateRouteAndItsStops(int routeID, RouteStopUpdate routeStopUpdate) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Route route = session.get(Route.class, routeID);
			if (routeStopUpdate.getRouteName() != null) {
				route.setRouteName(routeStopUpdate.getRouteName());
				session.update(route);
			}
			List<Integer> tobeDeletedStopIDs = routeStopUpdate.getTobeDeletedStopIDs();
			session.createQuery("delete from RouteStop r where r.stop.stopID in (:temp1)")
					.setParameterList("temp1", tobeDeletedStopIDs).executeUpdate();
			session.createQuery("delete from RouteStopSchedule r where r.stop.stopID in (:temp1)")
					.setParameterList("temp1", tobeDeletedStopIDs).executeUpdate();
			session.createQuery("delete from Stop s where s.stopID in (:temp1)")
					.setParameterList("temp1", tobeDeletedStopIDs).executeUpdate();
			List<StopOrderDetails> newStopsToBeAdded = routeStopUpdate.getNewStopsToBeAdded();
			for (StopOrderDetails stopOrderDetails : newStopsToBeAdded) {
				Stop newStop = new Stop();
				newStop.setStopName(stopOrderDetails.getStopName());
				newStop.setStopAddress(stopOrderDetails.getStopAddress());
				newStop.setStopLatitude(stopOrderDetails.getStopLatitude());
				newStop.setStopLongitude(stopOrderDetails.getStopLongitude());
				session.save(newStop);
				RouteStop newRouteStop = new RouteStop();
				newRouteStop.setRoute(route);
				newRouteStop.setStop(newStop);
				newRouteStop.setStopOrder(stopOrderDetails.getStopOrder());
				session.save(newRouteStop);
			}
			List<Object[]> stopIDStopOrderToBeUpdated = routeStopUpdate.getStopIDStopOrderToBeUpdated();
			for (Object[] obj : stopIDStopOrderToBeUpdated) {
				session.createQuery(
						"update RouteStop r set r.stopOrder=:temp1 where r.stop.stopID=:temp2 and r.route.routeID=:temp3")
						.setParameter("temp1", obj[1]).setParameter("temp2", obj[0]).setParameter("temp3", routeID)
						.executeUpdate();
			}
			List<StopOrderDetails> stopDetailsToBeUpdated = routeStopUpdate.getStopDetailsToBeUpdated();
			for (StopOrderDetails obj : stopDetailsToBeUpdated) {
				Stop existingStop = session.get(Stop.class, obj.getStopID());
				existingStop.setStopName(obj.getStopName());
				existingStop.setStopAddress(obj.getStopAddress());
				existingStop.setStopLatitude(obj.getStopLatitude());
				existingStop.setStopLongitude(obj.getStopLongitude());
				session.update(existingStop);
			}
			HashMap<Integer, String> scheduleNamesToBeUpdated = routeStopUpdate.getScheduleNamesToBeUpdated();
			Set<Integer> scheduleIDsToBeUpdated = scheduleNamesToBeUpdated.keySet();
			for (Integer scheduleID : scheduleIDsToBeUpdated) {
				VehicleSchedule schedule = session.get(VehicleSchedule.class, scheduleID);
				schedule.setVehicleScheduleName(scheduleNamesToBeUpdated.get(scheduleID));
				session.update(schedule);
			}
			tx.commit();
			logger.debug("Route and its stops updated successfully!!!");
			if (!(getShedulesOfRoute(routeID).isEmpty())) {
				return ResponseHandler.generateResponse2(true,
						"Route and its stops updated successfully!!! Please do the changes in scheduled arrival and departure times at stops, if required, by editing corresponding schedules of given route.",
						HttpStatus.OK);
			} else {
				return ResponseHandler.generateResponse2(true, "Route and its stops updated successfully!!!",
						HttpStatus.OK);
			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<Object[]> getRouteDetailsAndStopsCountOfAllRoutes() {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery("select r.route, count(r) from RouteStop r group by r.route.routeID")
				.list();
		session.close();
		logger.debug("Route details and stops count of all routes fetched.");
		return list;
	}

	public List<Object[]> getFirstStopNameOfAllRoutes() {
		Session session = factory.openSession();
		List<Object[]> list = session
				.createQuery("select r.route.routeID, r.stop.stopName from RouteStop r where r.stopOrder=1").list();
		session.close();
		logger.debug("First stop names of all routes fetched.");
		return list;
	}

	public ResponseEntity<Object> updateScheduledTimeAtStopsOfSchedule(int scheduleID,
			List<StopSchedule> stopSchedulesToBeUpdated, List<StopSchedule> stopSchedulesToBeInserted,
			StopSchedule updatedStopScheduleAtFirstStop, String updatedScheduleName, Integer blockingTimeInMinutes) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			boolean vehicleScheduleToBeChanged = false;
			VehicleSchedule schedule = session.get(VehicleSchedule.class, scheduleID);
			if (updatedStopScheduleAtFirstStop != null) {
				vehicleScheduleToBeChanged = true;
				schedule.setScheduledArrivalTime(
						LocalTime.parse(updatedStopScheduleAtFirstStop.getScheduledArrivalTime()));
				schedule.setScheduledDepartureTime(
						LocalTime.parse(updatedStopScheduleAtFirstStop.getScheduledDepartureTime()));
				schedule.setVehicleScheduleName(updatedScheduleName);

			}
			if (blockingTimeInMinutes != null) {
				vehicleScheduleToBeChanged = true;
				schedule.setBlockingTimeInMinutes(blockingTimeInMinutes);
			}
			if (vehicleScheduleToBeChanged) {
				session.update(schedule);
			}
			for (StopSchedule stopSchedule : stopSchedulesToBeUpdated) {
				session.createQuery(
						"update RouteStopSchedule r set r.scheduledDepartureTime=:temp1, r.scheduledArrivalTime=:temp2 where r.vehicleSchedule.vehicleScheduleID=:temp3 and r.stop.stopID=:temp4")
						.setParameter("temp1", LocalTime.parse(stopSchedule.getScheduledDepartureTime()))
						.setParameter("temp2", LocalTime.parse(stopSchedule.getScheduledArrivalTime()))
						.setParameter("temp3", scheduleID).setParameter("temp4", stopSchedule.getStopID())
						.executeUpdate();
			}
			if (!stopSchedulesToBeInserted.isEmpty()) {

				Route route = schedule.getRoute();
				for (StopSchedule stopSchedule : stopSchedulesToBeInserted) {
					RouteStopSchedule routeStopSchedule = new RouteStopSchedule();
					routeStopSchedule.setVehicleSchedule(schedule);
					routeStopSchedule.setRoute(route);
					Stop stop = session.get(Stop.class, stopSchedule.getStopID());
					routeStopSchedule.setStop(stop);
					routeStopSchedule.setScheduledArrivalTime(LocalTime.parse(stopSchedule.getScheduledArrivalTime()));
					routeStopSchedule
							.setScheduledDepartureTime(LocalTime.parse(stopSchedule.getScheduledDepartureTime()));
					session.save(routeStopSchedule);
				}
			}
			tx.commit();
			logger.debug("Scheduled time at stops updated successfullly!!!");
			return ResponseHandler.generateResponse2(true, "Scheduled time at stops updated successfullly!!!",
					HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<Object[]> getRouteStopScheduleDataAsPerStopsOrderOfSchedule(VehicleSchedule vehicleSchedule) {
		int routeID = vehicleSchedule.getRoute().getRouteID();
		Session session = factory.openSession();
		List<Object[]> list;
		if (vehicleSchedule.getTypeOfJourney() == 1) {
			list = session.createQuery(
					"select routeStop.stop.stopID, routeStopSchedule.scheduledDepartureTime, routeStopSchedule.scheduledArrivalTime from RouteStop routeStop left join RouteStopSchedule routeStopSchedule with routeStop.stop.stopID=routeStopSchedule.stop.stopID and routeStopSchedule.vehicleSchedule.vehicleScheduleID=:temp1 where routeStop.route.routeID=:temp2 order by routeStop.stopOrder ASC")
					.setParameter("temp1", vehicleSchedule.getVehicleScheduleID()).setParameter("temp2", routeID)
					.list();

		} else {

			list = session.createQuery(
					"select routeStop.stop.stopID, routeStopSchedule.scheduledDepartureTime, routeStopSchedule.scheduledArrivalTime from RouteStop routeStop left join RouteStopSchedule routeStopSchedule with routeStop.stop.stopID=routeStopSchedule.stop.stopID and routeStopSchedule.vehicleSchedule.vehicleScheduleID=:temp1 where routeStop.route.routeID=:temp2 order by routeStop.stopOrder desc")
					.setParameter("temp1", vehicleSchedule.getVehicleScheduleID()).setParameter("temp2", routeID)
					.list();
		}

		logger.debug("Data fetched.");
		session.close();
		return list;
	}

	public List<Object[]> getRouteStopScheduleDetailsAsPerStopsOrderOfSchedule(VehicleSchedule vehicleSchedule) {
		int routeID = vehicleSchedule.getRoute().getRouteID();
		Session session = factory.openSession();
		List<Object[]> list;
		if (vehicleSchedule.getTypeOfJourney() == 1) {
			list = session.createQuery(
					"select routeStop.stop, routeStop.stopOrder, routeStopSchedule.scheduledDepartureTime, routeStopSchedule.scheduledArrivalTime from RouteStop routeStop left join RouteStopSchedule routeStopSchedule with routeStop.stop.stopID=routeStopSchedule.stop.stopID and routeStopSchedule.vehicleSchedule.vehicleScheduleID=:temp1 where routeStop.route.routeID=:temp2 order by routeStop.stopOrder ASC")
					.setParameter("temp1", vehicleSchedule.getVehicleScheduleID()).setParameter("temp2", routeID)
					.list();

		} else {

			list = session.createQuery(
					"select routeStop.stop, routeStop.stopOrder, routeStopSchedule.scheduledDepartureTime, routeStopSchedule.scheduledArrivalTime from RouteStop routeStop left join RouteStopSchedule routeStopSchedule with routeStop.stop.stopID=routeStopSchedule.stop.stopID and routeStopSchedule.vehicleSchedule.vehicleScheduleID=:temp1 where routeStop.route.routeID=:temp2 order by routeStop.stopOrder desc")
					.setParameter("temp1", vehicleSchedule.getVehicleScheduleID()).setParameter("temp2", routeID)
					.list();
		}

		logger.debug("Data fetched.");
		session.close();
		return list;
	}

	public ResponseEntity<Object> assignNewResourcesToSchedule(
			List<StaffToVehicleScheduleMultiStaff> staffToVehicleScheduleMultiStaffListToBeSaved,
			List<VehicleToScheduleAssignment> vehicleToScheduleAssignmentListToBeSaved) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (StaffToVehicleScheduleMultiStaff staffToVehicleScheduleMultiStaff : staffToVehicleScheduleMultiStaffListToBeSaved) {
				session.save(staffToVehicleScheduleMultiStaff);
			}
			for (VehicleToScheduleAssignment vehicleToScheduleAssignment : vehicleToScheduleAssignmentListToBeSaved) {
				session.save(vehicleToScheduleAssignment);
			}
			tx.commit();
			logger.debug("Resources assigned to schedule successfully.");
			return ResponseHandler.generateResponse2(true, "Resources assigned to schedule successfully!!!",
					HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}

	public List<RouteStop> getStopsOrderForGivenRouteAndTypeOfJourney(int routeID, int typeOfJourney) {
		Session session = factory.openSession();
		List<RouteStop> list;
		if (typeOfJourney == ONWARD_JOURNEY) {
			list = session.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder ASC")
					.setParameter("temp1", routeID).list();
		} else {
			list = session.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder desc")
					.setParameter("temp1", routeID).list();
		}
		session.close();
		logger.debug("Stops of route fetched.");
		return list;
	}

	public List<PassengerToRouteID> getPassengerRouteDetailsListOfGivenVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session
				.createQuery("from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();
		// logger.debug("Given vehicle schedule mapped passengers are fetched.");
		session.close();
		return list;
	}

	public List<VehicleDetails> getAllVehicles() {
		Session session = factory.openSession();
		List<VehicleDetails> list = session.createQuery("from VehicleDetails").list();
		session.close();
		return list;
	}

	public VehicleDetails getVehicleOfScheduleforGivenDate(int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		List<VehicleDetails> list = session.createQuery(
				"select v.vehicleDetails from VehicleToScheduleAssignment v where v.scheduleDateID.vehicleSchedule.vehicleScheduleID=:temp1 and v.scheduleDateID.date=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", date).list();
		session.close();
		return list.isEmpty() ? null : list.get(0);
	}

	public String logout(Integer userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setJwtToken(null);
			session.update(user);
			UserToken userToken = session.get(UserToken.class, userID);
			String fcmToken = userToken.getToken();
			session.delete(userToken);
			tx.commit();
			logger.debug("Logout success.");
			return fcmToken;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return null;
		} finally {
			session.close();
		}
	}

	public boolean setTemporaryPassword(Integer userID, String temporaryPassword) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setTempPassword(temporaryPassword);
			user.setTempPasswordCreationTimeStamp(LocalDateTime.now());
			user.setPassword(null);
			session.update(user);
			tx.commit();
			logger.debug("Temporary password generated");
			return true;
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return false;
		} finally {
			session.close();
		}

	}

	public Object tryQuery() {
		return getUnmappedResourcesScheduleIDs();
	}

	public List<ArrivalTimeAtStopOfSchedule> getArrivalTimeAtAllStopsOfGivenSchedules(int scheduleID,
			Set<Integer> vehicleScheduleIDs) {
		
		List<Integer> vehicleScheduleIDsList = new ArrayList<>();
		vehicleScheduleIDsList.addAll(vehicleScheduleIDs);
		vehicleScheduleIDsList.add(scheduleID);
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select v.vehicleScheduleID, v.vehicleScheduleName, v.typeOfJourney, r.stop.stopID, r.stopOrder,routeStopSchedule.scheduledArrivalTime, v.blockingTimeInMinutes from VehicleSchedule v left join RouteStop r with v.route.routeID=r.route.routeID left join RouteStopSchedule routeStopSchedule with routeStopSchedule.vehicleSchedule.vehicleScheduleID=v.vehicleScheduleID and r.stop.stopID=routeStopSchedule.stop.stopID where v.vehicleScheduleID in :temp1 order by v.vehicleScheduleID, r.stopOrder")
				.setParameterList("temp1", vehicleScheduleIDsList).list();
		session.close();

		List<ArrivalTimeAtStopOfSchedule> arrivalTimeAtStopOfScheduleList = new ArrayList<>();

		for (Object[] obj : list) {

			arrivalTimeAtStopOfScheduleList.add(
					new ArrivalTimeAtStopOfSchedule((int) (obj[0]), (String) (obj[1]), (int) (obj[2]), (int) (obj[3]),
							(int) (obj[4]), (obj[5] != null) ? ((LocalTime) (obj[5])) : null, (int) (obj[6])));
		}
		logger.debug("Arrival time at all stops of given schedules fetched");
		return arrivalTimeAtStopOfScheduleList;
	}

	// TODO : 11sept 15:01
	public List<Object[]> getScheduleAndVehicleStaffIDsForGivenDatesInterval(LocalDate startDate, LocalDate endDate) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID, s.vehicleScheduleDateStaffID.staff.userID, v.vehicleDetails.vehicleID from StaffToVehicleScheduleMultiStaff s left join VehicleToScheduleAssignment v with s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=v.scheduleDateID.vehicleSchedule.vehicleScheduleID and s.vehicleScheduleDateStaffID.date=v.scheduleDateID.date where s.vehicleScheduleDateStaffID.date>=:temp1 and s.vehicleScheduleDateStaffID.date<=:temp2")
				.setParameter("temp1", startDate).setParameter("temp2", endDate).list();
		session.close();
		logger.debug("Schedule and staff details fetched for given dates interval");
		return list;
	}

	public List<Object[]> getStaffVehicleDetailsOfShchedulesForGivenDateInterval(LocalDate startDate,
			LocalDate endDate) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID, s.staffType.roleID, v.vehicleDetails from StaffToVehicleScheduleMultiStaff s left join VehicleToScheduleAssignment v with s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=v.scheduleDateID.vehicleSchedule.vehicleScheduleID and s.vehicleScheduleDateStaffID.date=v.scheduleDateID.date where s.vehicleScheduleDateStaffID.date>= :temp1 and s.vehicleScheduleDateStaffID.date<= :temp2 order by s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID, s.vehicleScheduleDateStaffID.date")
				.setParameter("temp1", startDate).setParameter("temp2", endDate).list();
		session.close();
		logger.debug("Schedule, staff and vehicles details fetched for given dates interval");
		return list;
	}

	// coding checked
	public List<Object[]> getStaffVehicleDetailsOfGivenShcheduleForGivenDateInterval(int scheduleID,
			LocalDate startDate, LocalDate endDate) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID, s.staffType.roleID, v.vehicleDetails from StaffToVehicleScheduleMultiStaff s left join VehicleToScheduleAssignment v with s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=v.scheduleDateID.vehicleSchedule.vehicleScheduleID and s.vehicleScheduleDateStaffID.date=v.scheduleDateID.date where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp3 and s.vehicleScheduleDateStaffID.date>= :temp1 and s.vehicleScheduleDateStaffID.date<= :temp2 order by s.vehicleScheduleDateStaffID.date")
				.setParameter("temp1", startDate).setParameter("temp2", endDate).setParameter("temp3", scheduleID)
				.list();
		session.close();
		logger.debug("Schedule, staff and vehicles details fetched for given dates interval");
		return list;
	}

	public boolean updateFileInformationUUIDandHitCount(long fileID, String uuid) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			FileInformation fileInformation = session.get(FileInformation.class, fileID);
			fileInformation.setFileURL(uuid);
			fileInformation.setHitCount(0);
			session.update(fileInformation);
			tx.commit();
			return true;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return false;

		} finally {
			session.close();
		}
	}

	public boolean incrementFileHitCount(long fileID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			FileInformation fileInformation = session.get(FileInformation.class, fileID);
			int hitCount = fileInformation.getHitCount();
			++hitCount;
			fileInformation.setHitCount(hitCount);
			session.update(fileInformation);
			tx.commit();
			return true;

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return false;

		} finally {
			session.close();
		}
	}

	public FileInformation getFileInformationHavingGivenURL(String subString) {
		Session session = factory.openSession();
		List<FileInformation> list = session.createQuery("from FileInformation where fileURL=:temp1")
				.setParameter("temp1", subString).list();
		session.close();
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	// coding checked
	public List<TripToStaff> fetchTripStaffDataOfGivenTripIDs(List<Integer> tripIDs) {
		Session session = factory.openSession();
		List<TripToStaff> list = session.createQuery(
				"from TripToStaff t where t.tripStaffID.tripDetails.tripDetailsID in :temp1 order by t.tripStaffID.tripDetails.tripDetailsID asc")
				.setParameterList("temp1", tripIDs).list();
		session.close();
		logger.debug("Trips staff data fetched.");
		return list;
	}

	// coding checked
	public List<Object[]> getPassengerDetailsOfGivenSchedules(List<Integer> scheduleIDs) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID, p.userTypeOfJourneyID.user from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID in :temp1 order by p.vehicleSchedule.vehicleScheduleID asc")
				.setParameterList("temp1", scheduleIDs).list();
		session.close();
		logger.debug("Passegers data fetched of given schedule IDs/");
		return list;
	}

	public List<Object[]> getRoutesAssignedForTodayAndFutureToGivenRoleUsers(int roleID) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.staff, s.vehicleScheduleDateStaffID.vehicleSchedule.route.routeID from StaffToVehicleScheduleMultiStaff s where s.staffType.roleID=:temp1 and s.vehicleScheduleDateStaffID.date>=:temp2 order by s.vehicleScheduleDateStaffID.staff.userID")
				.setParameter("temp1", roleID).setParameter("temp2", LocalDate.now()).list();
		session.close();
		logger.debug("data fetched.");
		return list;
	}

	public List<Object[]> getUnmappedResourcesScheduleIDs() {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select v.vehicleScheduleID, v.vehicleScheduleName from VehicleSchedule v where v.vehicleScheduleID not in (select distinct s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID from StaffToVehicleScheduleMultiStaff s)")
				.list();
		session.close();
		logger.debug("data fetched.");
		return list;
	}

	public List<Object[]> getStaffVehicleDetailsOfGivenScheduleForGivenDateIntervalOrderedDatewise(int scheduleID,
			LocalDate startDate, LocalDate endDate) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.date, s.vehicleScheduleDateStaffID.staff.userID,  s.staffType.roleID, v.vehicleDetails.vehicleID from StaffToVehicleScheduleMultiStaff s left join VehicleToScheduleAssignment v with s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=v.scheduleDateID.vehicleSchedule.vehicleScheduleID and s.vehicleScheduleDateStaffID.date=v.scheduleDateID.date where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp3 and s.vehicleScheduleDateStaffID.date>= :temp1 and s.vehicleScheduleDateStaffID.date<= :temp2 order by s.vehicleScheduleDateStaffID.date")
				.setParameter("temp1", startDate).setParameter("temp2", endDate).setParameter("temp3", scheduleID)
				.list();
		session.close();
		logger.debug(" staff and vehicles details of schedule fetched for given dates interval");
		return list;
	}

	public List<TripToStaff> getTripStaffDataCorrespondingToGivenScheduleStartingOnGivenDate(int scheduleID,
			LocalDate date) {
		Session session = factory.openSession();
		LocalDateTime startTime = date.atStartOfDay();
		LocalDate nextDay = date.plusDays(1L);
		LocalDateTime lastTime = nextDay.atStartOfDay();
		logger.debug("Fetching tripstaffdata corresponding to given schedule and date.");
		List<TripToStaff> list = session.createQuery(
				"from TripToStaff t where t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID=:temp1 and t.tripStaffID.tripDetails.tripStart>=:temp2 and t.tripStaffID.tripDetails.tripStart<:temp3")
				.setParameter("temp1", scheduleID).setParameter("temp2", startTime).setParameter("temp3", lastTime)
				.list();
		session.close();
		return list;
	}

	public ResponseEntity<Object> updateResourcesAssignment(int scheduleID,
			ResourcesAssignmentChangesToBeDone resourcesAssignmentChangesToBeDone) {
	
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List<IDDate> addDrivers = resourcesAssignmentChangesToBeDone.getAddDrivers();
			List<IDDate> addAttendants = resourcesAssignmentChangesToBeDone.getAddAttendants();
			List<IDDate> addVehicles = resourcesAssignmentChangesToBeDone.getAddVehicles();
			List<IDDate> disassociateDrivers = resourcesAssignmentChangesToBeDone.getDisassociateDrivers();
			List<IDDate> disassociateAttendants = resourcesAssignmentChangesToBeDone.getDisassociateAttendants();
			List<IDDate> disassociateVehicles = resourcesAssignmentChangesToBeDone.getDisassociateVehicles();
			
			session.createQuery(
					"delete from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date in :temp2")
					.setParameter("temp1", scheduleID)
					.setParameter("temp2", resourcesAssignmentChangesToBeDone.getDeleteDates()).executeUpdate();
			session.createQuery(
					"delete from VehicleToScheduleAssignment v where v.scheduleDateID.vehicleSchedule.vehicleScheduleID=:temp1 and v.scheduleDateID.date in :temp2")
					.setParameter("temp1", scheduleID).setParameter("temp2", resourcesAssignmentChangesToBeDone.getDeleteDates())
					.executeUpdate();
			List<User> usersList = session.createQuery("from User u where u.userID in :temp1")
					.setParameterList("temp1", resourcesAssignmentChangesToBeDone.getUserIDs()).list();
			LinkedHashMap<Integer, User> usersMap = new LinkedHashMap<>();
			usersList.forEach(obj -> usersMap.put(obj.getUserID(), obj));

			List<VehicleDetails> vehiclesList = session.createQuery("from VehicleDetails v where v.vehicleID in :temp1")
					.setParameter("temp1", resourcesAssignmentChangesToBeDone.getVehicleIDs()).list();
			LinkedHashMap<Integer, VehicleDetails> vehiclesMap = new LinkedHashMap<>();
			vehiclesList.forEach(obj -> vehiclesMap.put(obj.getVehicleID(), obj));

			VehicleSchedule vehicleSchedule = session.get(VehicleSchedule.class, scheduleID);
			Role driverRole = session.get(Role.class, DRIVER_ROLE_ID);
			Role attendantRole = session.get(Role.class, ATTENDANT_ROLE_ID);

			for (IDDate i : disassociateDrivers) {
				session.createQuery(
						"delete from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.staff.userID=: temp2 and s.vehicleScheduleDateStaffID.date=:temp3 and s.staffType.roleID=:temp4")
						.setParameter("temp1", scheduleID).setParameter("temp2", i.getId())
						.setParameter("temp3", i.getDate()).setParameter("temp4", DRIVER_ROLE_ID).executeUpdate();
			}
			for (IDDate i : disassociateAttendants) {
				session.createQuery(
						"delete from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.staff.userID=: temp2 and s.vehicleScheduleDateStaffID.date=:temp3 and s.staffType.roleID=:temp4")
						.setParameter("temp1", scheduleID).setParameter("temp2", i.getId())
						.setParameter("temp3", i.getDate()).setParameter("temp4", ATTENDANT_ROLE_ID).executeUpdate();
			}
			for (IDDate i : disassociateVehicles) {
				session.createQuery(
						"delete from VehicleToScheduleAssignment v where v.scheduleDateID.vehicleSchedule.vehicleScheduleID=:temp1 and v.scheduleDateID.date=:temp2 and v.vehicleDetails.vehicleID=:temp3")
						.setParameter("temp1", scheduleID).setParameter("temp2", i.getDate())
						.setParameter("temp3", i.getId()).executeUpdate();
			}
			for (IDDate i : addDrivers) {
				session.save(new StaffToVehicleScheduleMultiStaff(
						new VehicleScheduleDateStaffID(vehicleSchedule, usersMap.get(i.getId()), i.getDate()),
						driverRole));
			}
			for (IDDate i : addAttendants) {
				session.save(new StaffToVehicleScheduleMultiStaff(
						new VehicleScheduleDateStaffID(vehicleSchedule, usersMap.get(i.getId()), i.getDate()),
						attendantRole));
			}
			for (IDDate i : addVehicles) {
				session.save(new VehicleToScheduleAssignment(new ScheduleDateID(vehicleSchedule, i.getDate()),
						vehiclesMap.get(i.getId())));
			}
			
			tx.commit();
			logger.debug("Data edited.");
			return ResponseHandler.generateResponse2(true, "Data updated successfully!!!", HttpStatus.OK);
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String s = errors.toString();
			logger.error("Exception =>  " + s);
			return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			session.close();
		}

	}
	public List<Object[]> getArrivalTimeAtAllStopsOfSchedulesInGivenDateRange(LocalDate startDate, LocalDate endDate) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select v.vehicleScheduleID, v.vehicleScheduleName, v.typeOfJourney, r.stop.stopID, r.stopOrder,routeStopSchedule.scheduledArrivalTime, v.blockingTimeInMinutes from VehicleSchedule v left join RouteStop r with v.route.routeID=r.route.routeID left join RouteStopSchedule routeStopSchedule with routeStopSchedule.vehicleSchedule.vehicleScheduleID=v.vehicleScheduleID and r.stop.stopID=routeStopSchedule.stop.stopID where v.vehicleScheduleID in (select va.scheduleDateID.vehicleSchedule.vehicleScheduleID from VehicleToScheduleAssignment va where va.scheduleDateID.date>=:temp1 and va.scheduleDateID.date<=:temp2) order by v.vehicleScheduleID, r.stopOrder")
				.setParameter("temp1", startDate)
				.setParameter("temp2", endDate)
				.list();
		session.close();
logger.debug("Arrival time fetched");
		return list;
	}
	
	public List<TripToStaff> getTripStaffDataCorrespondingToGivenSchedulesForGivenDateOrderedByTripID(Set<Integer> scheduleIDs,
			LocalDate date) {
		Session session = factory.openSession();
		LocalDateTime startTime = date.atStartOfDay();
		LocalDate nextDay = date.plusDays(1L);
		LocalDateTime lastTime = nextDay.atStartOfDay();
		logger.debug("Fetching tripstaffdata corresponding to given schedules and date.");
		List<TripToStaff> list = session.createQuery(
				"from TripToStaff t where t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID in :temp1 and t.tripStaffID.tripDetails.tripStart>=:temp2 and t.tripStaffID.tripDetails.tripStart<:temp3 order by t.tripStaffID.tripDetails.tripDetailsID")
				.setParameter("temp1", scheduleIDs).setParameter("temp2", startTime).setParameter("temp3", lastTime)
				.list();
		session.close();
		return list;
	}
	public APICount getAPICount(int tripID) {
		Session session = factory.openSession();
		APICount apiCount=session.get(APICount.class, tripID);
		session.close();
		return apiCount;
	}

}
