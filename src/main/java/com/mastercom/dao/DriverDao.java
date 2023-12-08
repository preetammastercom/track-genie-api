package com.mastercom.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.mastercom.embeddableclasses.TripStaffID;
import com.mastercom.embeddableclasses.TripUser;
import com.mastercom.entity.APICount;
import com.mastercom.entity.CurrentLatLong;
import com.mastercom.entity.NotificationOnOff;
import com.mastercom.entity.PassengerStatus;
import com.mastercom.entity.Role;
import com.mastercom.entity.RouteStop;
import com.mastercom.entity.RouteStopSchedule;
import com.mastercom.entity.StartStopTime;
import com.mastercom.entity.TimeRequiredToReachAtStop;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.User;
import com.mastercom.entity.UserStatusCode;
import com.mastercom.entity.UserToken;
import com.mastercom.entity.VehicleSchedule;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.VideoURL;

@Repository
public class DriverDao {


	@Autowired
	SessionFactory factory;

	@Autowired
	AdminDao adminDao;
	@Value("${count.updateCurrentLatLongAndTimeRequiredToReachAtOtherStopsAPICounter}")
	private boolean updateCurrentLatLongAndTimeRequiredToReachAtOtherStopsAPICounter;


	private static final String SERVER_ERROR = "Server Error!!!";

	private static final Logger logger = LogManager.getLogger(DriverDao.class);

	public List<VehicleSchedule> getVehicleSchedulesAssignedToDriverToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<VehicleSchedule> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2 and s.staffType.roleID=4")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's vehicle schedules assigned to driver fetched.");
		session.close();
		return list;
	}

	public List<TripDetails> getTripDataOfGivenVehicleScheduleWhereDriverLoginTimeIsWithinGivenPeriod(
			int vehicleScheduleID, LocalDateTime startTime, LocalDateTime endTime) {
		Session session = factory.openSession();
		List<TripDetails> tlist = session.createQuery(
				"select distinct t.tripStaffID.tripDetails from TripToStaff t where t.staffLoginTime>=:temp1 and t.staffLoginTime<:temp2 and t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID=:temp3 and t.staffType.roleID=4")
				.setParameter("temp1", startTime).setParameter("temp2", endTime)
				.setParameter("temp3", vehicleScheduleID).list();
		session.close();
		return tlist;
	}

	public int startVehicleScheduleByDriver(Integer vehicleScheduleID, int userID, Integer tripDetailsID) {

		Session session = factory.openSession();

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			if (tripDetailsID == null) {
				TripDetails trip = new TripDetails();
				VehicleSchedule vehicleSchedule = session.get(VehicleSchedule.class, vehicleScheduleID);
				trip.setVehicleSchedule(vehicleSchedule);
				trip.setVehicleScheduleName(vehicleSchedule.getVehicleScheduleName());
				trip.setTripStart(LocalDateTime.now());
				session.save(trip);
				TripToStaff tripToStaff = new TripToStaff();
				User driver = session.get(User.class, userID);
				TripStaffID tripStaffID = new TripStaffID(trip, driver);
				tripToStaff.setTripStaffID(tripStaffID);
				Role role = session.get(Role.class, 4);
				tripToStaff.setStaffType(role);
				tripToStaff.setStaffLoginTime(LocalDateTime.now());
				session.save(tripToStaff);
				List<Integer> userIDs = session.createQuery(
						"select p.user.userID from PlannedCancelJourney p where p.date=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2")
						.setParameter("temp1", LocalDate.now()).setParameter("temp2", vehicleScheduleID).list();
				for (Integer id : userIDs) {
					PassengerStatus passengerStatus = new PassengerStatus();
					User userObj = session.get(User.class, id);
					TripUser tripUser = new TripUser(userObj, trip);
					passengerStatus.setTripUser(tripUser);
					passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 4));
					passengerStatus.setUpdatedTime(LocalDateTime.now());
					passengerStatus.setTypeOfJourney(vehicleSchedule.getTypeOfJourney());
					session.save(passengerStatus);
				}
			} else {
				TripDetails trip = session.get(TripDetails.class, tripDetailsID);
				TripToStaff tripToStaff = new TripToStaff();
				User driver = session.get(User.class, userID);
				TripStaffID tripStaffID = new TripStaffID(trip, driver);
				tripToStaff.setTripStaffID(tripStaffID);
				Role role = session.get(Role.class, 4);
				tripToStaff.setStaffType(role);
				tripToStaff.setStaffLoginTime(LocalDateTime.now());
				session.save(tripToStaff);
			}

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

	public TripDetails getTripDetails(int tripDetailsID) {
		Session session = factory.openSession();
		TripDetails trip = session.get(TripDetails.class, tripDetailsID);

		session.close();
		return trip;
	}

	public boolean endTripByDriver(int userID, int tripScheduleDetailsID, VideoURL videoURL) {
		Session session = factory.openSession();

		TripToStaff tripToStaff = getTripToStaffDataOfGivenStaff(tripScheduleDetailsID, userID);

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			tripToStaff.setStaffVerifiedTime(LocalDateTime.now());
			tripToStaff.setStaffVerifiedVideo(videoURL.getUrl());
			session.update(tripToStaff);

			tx.commit();
			logger.debug("Driver verification time recorded.");
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

	public List<Integer> getUserIDsOfGivenPickUpStopOfOnwardJourneyVehicleSchedule(int stopID, int vehicleScheduleID) {
		Session session = factory.openSession();
		List<Integer> userIDs = session.createQuery(
				"select p.userTypeOfJourneyID.user.userID from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1 and p.pickupPointStop.stopID=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", stopID).list();
		logger.debug("Passengers of given pickUp stop of onward journey vehicleSchedule fetched.");
		session.close();
		return userIDs;
	}

	public List<Integer> getUserIDsOfGivenDropStopOfReturnJourneyVehicleSchedule(int stopID, int vehicleScheduleID) {
		Session session = factory.openSession();
		List<Integer> userIDs = session.createQuery(
				"select p.userTypeOfJourneyID.user.userID from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1 and p.dropPointStop.stopID=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", stopID).list();
		logger.debug("Passengers of given drop stop of return journey vehicleSchedule fetched.");
		session.close();
		return userIDs;
	}

	public List<RouteStop> getStopsWithStopOrderOfVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		VehicleSchedule vehicleSchedule = session.get(VehicleSchedule.class, vehicleScheduleID);
		int routeID = vehicleSchedule.getRoute().getRouteID();

		if (vehicleSchedule.getTypeOfJourney() == 1) {
			List<RouteStop> stops = session
					.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder asc")
					.setParameter("temp1", routeID).list();

			session.close();
			return stops;
		} else {
			List<RouteStop> stops = session
					.createQuery("from RouteStop r where r.route.routeID=:temp1 order by r.stopOrder desc")
					.setParameter("temp1", routeID).list();

			session.close();
			return stops;
		}

	}

	public List<RouteStopSchedule> getRouteStopScheduleOfGivenStop(int stopID, int vehicleScheduleID) {
		Session session = factory.openSession();
		List<RouteStopSchedule> routeStopScheduleList = session.createQuery(
				"from RouteStopSchedule r where r.vehicleSchedule.vehicleScheduleID=:temp1 and r.stop.stopID=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", stopID).list();

		session.close();
		return routeStopScheduleList;
	}

	public List<Integer> getStopIDsOfGivenVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		int routeID = session.get(VehicleSchedule.class, vehicleScheduleID).getRoute().getRouteID();
		List<Integer> stopIDs = session
				.createQuery("select r.stop.stopID from RouteStop r where r.route.routeID=:temp1")
				.setParameter("temp1", routeID).list();
		logger.debug("Fetched stops of route");
		session.close();
		return stopIDs;
	}

	public boolean checkWhetherGivenStudentBelongsToGivenVehicleSchedule(int userID, int vehicleScheduleID) {
		Session session = factory.openSession();
		List<User> list = session.createQuery(
				"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", vehicleScheduleID).list();
		session.close();
		if (!(list.isEmpty())) {
			logger.debug("Passenger belongs to given vehicle schedule");
			return true;
		} else {
			logger.debug("Passenger does not belong to given vehicle schedule");
			return false;
		}
	}

	public Integer checkWhetherStudentStatusRecordedAndGetStudentStatusID(int tripScheduleDetailsID, int userID) {
		Session session = factory.openSession();
		logger.debug("Fetching passenger status for a trip");
		List<Integer> list = session.createQuery(
				"select p.userStatusCode.statusID from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.tripUser.user.userID=:temp2")
				.setParameter("temp1", tripScheduleDetailsID).setParameter("temp2", userID).list();
		session.close();
		if (!(list.isEmpty())) {
			return list.get(0);
		} else {
			return 0;
		}
	}

	public int missedBus(int tripScheduleDetailsID, int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			PassengerStatus passengerStatus = new PassengerStatus();
			User userObj = session.get(User.class, userID);
			TripDetails tripObj = session.get(TripDetails.class, tripScheduleDetailsID);
			TripUser tripUser = new TripUser(userObj, tripObj);
			passengerStatus.setTripUser(tripUser);
			passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 3));
			passengerStatus.setTypeOfJourney(
					(session.get(TripDetails.class, tripScheduleDetailsID)).getVehicleSchedule().getTypeOfJourney());
			passengerStatus.setUpdatedTime(LocalDateTime.now());
			session.save(passengerStatus);
			tx.commit();
			logger.debug("Missed bus status recorded.");
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

	public int setPickupStatus(int tripScheduleDetailsID, int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			PassengerStatus passengerStatus = new PassengerStatus();
			User userObj = session.get(User.class, userID);
			TripDetails trip = session.get(TripDetails.class, tripScheduleDetailsID);
			TripUser tripUser = new TripUser(userObj, trip);
			passengerStatus.setTripUser(tripUser);
			passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 1));
			passengerStatus.setPassengerPickedUpTime(LocalDateTime.now());
			passengerStatus.setTypeOfJourney(trip.getVehicleSchedule().getTypeOfJourney());
			session.save(passengerStatus);

			tx.commit();
			logger.debug("Pickup status recorded.");
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

	public int setPickupStatusForMissedBus(int tripScheduleDetailsID, int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			PassengerStatus passengerStatus = (PassengerStatus) session.createQuery(
					"from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.tripUser.user.userID=:temp2")
					.setParameter("temp1", tripScheduleDetailsID).setParameter("temp2", userID).list().get(0);

			passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 1));
			passengerStatus.setPassengerPickedUpTime(LocalDateTime.now());
			session.update(passengerStatus);
			tx.commit();
			logger.debug("Pickup status recorded.");
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

	public int setDropStatus(int tripScheduleDetailsID, int userID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			PassengerStatus passengerStatus = (PassengerStatus) session.createQuery(
					"from PassengerStatus p where p.tripUser.tripDetails.tripDetailsID=:temp1 and p.tripUser.user.userID=:temp2")
					.setParameter("temp1", tripScheduleDetailsID).setParameter("temp2", userID).list().get(0);
			passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 2));
			passengerStatus.setPassengerDropTime(LocalDateTime.now());
			session.update(passengerStatus);

			tx.commit();
			logger.debug("Drop status recorded.");
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

	public boolean checkVehicleReachedAtGivenStopOfGivenTrip(int tripScheduleDetailsID, int stopID) {
		Session session = factory.openSession();
		logger.debug("Checking vehicle reached at given stop.");
		List<StartStopTime> list = session
				.createQuery("from StartStopTime s where s.tripDetails.tripDetailsID=:temp1 and s.stop.stopID=:temp2")
				.setParameter("temp1", tripScheduleDetailsID).setParameter("temp2", stopID).list();
		session.close();
		return (!(list.isEmpty()));
	}

	public List<Integer> getTripIDsGoingOnForDriver(int userID) {
		Session session = factory.openSession();

		List<Integer> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffVerifiedTime is null and t.staffType.roleID=4")
				.setParameter("temp1", userID).list();

		logger.debug("Trip IDs going on for driver, which are not verified by driver fetched.");
		session.close();
		return list;
	}

	public List<TripDetails> getTripsGoingOnForStaffAndNotVerifiedByThatStaff(int userID) {
		Session session = factory.openSession();

		List<TripDetails> list = session.createQuery(
				"select t.tripStaffID.tripDetails from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffVerifiedTime is null")
				.setParameter("temp1", userID).list();

		logger.debug("Trip IDs going on for staff, which are not verified by that staff fetched.");
		session.close();
		return list;
	}

	public List<Integer> getVehicleSchedulesIDsVerifiedByDriverToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		LocalDateTime startTime = todayDate.atStartOfDay();
		LocalDate nextdate = todayDate.plusDays(1L);
		LocalDateTime lastTime = nextdate.atStartOfDay();
		List<Integer> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffVerifiedTime is not null and t.staffLoginTime>=:temp2 and t.staffLoginTime<:temp3 and t.staffType.roleID=4")
				.setParameter("temp1", userID).setParameter("temp2", startTime).setParameter("temp3", lastTime).list();

		logger.debug("Today's Vehicle schedules which are verified by attendant, are fetched.");
		session.close();
		return list;
	}

	public List<Integer> getVehicleSchedulesIDsAssignedToDriverToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<Integer> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2 and s.staffType.roleID=4")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's vehicle schedules of driver are fetched.");
		session.close();
		return list;
	}

	public boolean updateCurrentLatLongAndTimeRequiredToReachAtOtherStops(
			TimeRequiredToReachAtStop timeRequiredToReachAtStop, CurrentLatLong currentLatLong) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(currentLatLong);
			session.saveOrUpdate(timeRequiredToReachAtStop);
			if(updateCurrentLatLongAndTimeRequiredToReachAtOtherStopsAPICounter) {
				APICount apiCount=session.get(APICount.class, currentLatLong.getTripDetails().getTripDetailsID());
				if(apiCount==null) {
					APICount apiCountNewObj=new APICount();
					apiCountNewObj.setTripDetails(currentLatLong.getTripDetails());
					apiCountNewObj.setUpdateCurrentLatLongAndTimeToReachAtStopAPICount(1L);
					session.save(apiCountNewObj);
				}
				else {
					apiCount.setUpdateCurrentLatLongAndTimeToReachAtStopAPICount(apiCount.getUpdateCurrentLatLongAndTimeToReachAtStopAPICount()+1);
					session.update(apiCount);
				}
			}
			tx.commit();
			logger.debug("updated.");

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

	public User getStudentDetailsHavingQRcodeString(String userQRcodeString) {
		Session session = factory.openSession();
		logger.debug("Fetching user details having given QR code String");
		List<User> list = session.createQuery("from User u where u.userQRcodeString=:temp1")
				.setParameter("temp1", userQRcodeString).list();
		session.close();
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public UserToken getUserToken(int userID) {
		Session session = factory.openSession();
		UserToken userToken = session.get(UserToken.class, userID);
		logger.debug("User token is fetched.");
		session.close();
		return userToken;
	}

	public NotificationOnOff getNotificationOnOff(int userID) {
		Session session = factory.openSession();
		NotificationOnOff notificationOnOff = session.get(NotificationOnOff.class, userID);
		logger.debug("NotificationOnOff status of user is fetched.");
		session.close();
		return notificationOnOff;
	}

	public List<TripToStaff> getTripToStaffData(int tripDetailsID) {
		Session session = factory.openSession();
		List<TripToStaff> tripToStaffData = session
				.createQuery("from TripToStaff t where t.tripStaffID.tripDetails.tripDetailsID=:temp1")
				.setParameter("temp1", tripDetailsID).list();
		session.close();
		return tripToStaffData;
	}

	public List<Integer> getStaffIDsOfTrip(int tripDetailsID) {
		Session session = factory.openSession();
		List<Integer> staffIDsOfTrip = session.createQuery(
				"select t.tripStaffID.staff.userID from TripToStaff t where t.tripStaffID.tripDetails.tripDetailsID=:temp1")
				.setParameter("temp1", tripDetailsID).list();
		session.close();
		return staffIDsOfTrip;
	}

	public TripToStaff getTripToStaffDataOfGivenStaff(int tripDetailsID, int staffID) {
		Session session = factory.openSession();
		List<TripToStaff> list = session.createQuery(
				"from TripToStaff t where t.tripStaffID.tripDetails.tripDetailsID=:temp1 and t.tripStaffID.staff.userID=:temp2")
				.setParameter("temp1", tripDetailsID).setParameter("temp2", staffID).list();
		session.close();
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public List<Integer> getAllActiveTripIDs() {
		Session session = factory.openSession();
		List<Integer> allActiveTripIDs = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.adminVerifiedTime is null")
				.list();
		session.close();
		return allActiveTripIDs;
	}

	public List<Integer> getDriverAllTripIDs(int userID) {
		Session session = factory.openSession();
		List<Integer> driverAllTripIDs = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffType.roleID=4")
				.setParameter("temp1", userID).list();
		session.close();
		return driverAllTripIDs;
	}

	public List<Integer> getStaffAllTripIDs(int userID) {
		Session session = factory.openSession();
		List<Integer> staffAllTripIDs = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.staff.userID=:temp1")
				.setParameter("temp1", userID).list();
		session.close();
		return staffAllTripIDs;
	}

	public SessionFactory getSessionFactory() {
		return factory;
	}

	public int updateBusReachedDestinationTime(int tripID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			TripDetails trip = session.get(TripDetails.class, tripID);
			trip.setBusReachedDestination(LocalDateTime.now());
			session.update(trip);
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

	public List<String> getTokensOfPassengersWhoEnabledNotificationOfTripStarted(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<String> list = session.createQuery(
				"select t.token from NotificationOnOff n inner join PassengerToRouteID p with n.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1 and n.tripStarted=1 inner join UserToken t with n.user.userID=t.user.userID")
				.setParameter("temp1", vehicleScheduleID).list();
		session.close();
		return list;
	}

	public ResponseEntity<Object> setPassword(Integer userID, String password) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = session.get(User.class, userID);
			user.setPassword(password);
			session.update(user);
			tx.commit();
			logger.debug("Password set successfully!");
			return ResponseHandler.generateResponse2(true, "Password set successfully!", HttpStatus.OK);
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

	public ResponseEntity<Object> incrementDirectionAPICount(TripDetails trip) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			APICount apiCount=session.get(APICount.class, trip.getTripDetailsID());
			if(apiCount==null) {
				APICount apiCountNewObj=new APICount();
				apiCountNewObj.setTripDetails(trip);
				apiCountNewObj.setDirectionAPICount(1L);
				session.save(apiCountNewObj);
			}
			else {
				apiCount.setDirectionAPICount(apiCount.getDirectionAPICount()+1);
				session.update(apiCount);
			}
			tx.commit();
			return ResponseHandler.generateResponse2(true, "Direction API count incremented", HttpStatus.OK);
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

//	public List<Integer> getidsOfPassengersWhoEnabledNotificationOfTripStarted(int vehicleScheduleID){
//		Session session=factory.openSession();
//		System.err.println("//////");
//		//List<Integer> list=session.createQuery("select t.user.userID from ((NotificationOnOff n inner join PassengerToRouteID p with n.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1 and n.tripStarted=1) inner join UserToken t with n.user.userID=t.user.userID)").setParameter("temp1", vehicleScheduleID).list();
//		List<Integer> list=session.createQuery("select t.user.userID from NotificationOnOff n inner join PassengerToRouteID p with n.user.userID = p.userTypeOfJourneyID.user.userID and p.vehicleSchedule.vehicleScheduleID=:temp1 and n.tripStarted=1 inner join UserToken t with n.user.userID=t.user.userID").setParameter("temp1", vehicleScheduleID).list();
//System.err.println("//////");
//		session.close();
//		return list;
//	}

}
