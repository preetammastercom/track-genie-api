package com.mastercom.dao;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.mastercom.embeddableclasses.TripUser;
import com.mastercom.entity.CurrentLatLong;
import com.mastercom.entity.NotificationOnOff;
import com.mastercom.entity.PassengerStatus;
import com.mastercom.entity.PassengerToRouteID;
import com.mastercom.entity.PlannedCancelJourney;
import com.mastercom.entity.School;
import com.mastercom.entity.TimeRequiredToReachAtStop;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.UserStatusCode;
import com.mastercom.handler.ResponseHandler;

@Repository
public class PassengerDao {
	@Autowired
	SessionFactory factory;

	@Autowired
	DriverDao driverDao;
	
	private static final String SERVER_ERROR="Server Error!!!";
	
	private static final Logger logger = LogManager.getLogger(PassengerDao.class);

	public List<Object[]> getOnwardVehicleScheduleRouteOfUser(int userID) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID, p.route.routeID, p.route.routeName from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", 1).list();
		logger.debug("Onward vehicle schedule, route of user is fetched.");
		session.close();
		return list;
	}

	public List<Object[]> getReturnVehicleScheduleRouteOfUser(int userID) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID, p.route.routeID, p.route.routeName from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", 2).list();
		logger.debug("Return vehicle schedule, route of user is fetched.");
		session.close();
		return list;
	}

	

	public List<Object[]> getStaffOfVehicleSchedule(int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		List<Object[]> list = session.createQuery(
				"select s.staffType.roleID, s.vehicleScheduleDateStaffID.staff from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", date).list();
		logger.debug("Staff of vehicle schedule are fetched.");
		session.close();
		return list;
	}

	public int childOnboardStatusOfGivenTrip(int userID, int tripScheduleDetailsID) {
		Session session = factory.openSession();

		List<Integer> list = session.createQuery(
				"select p.userStatusCode.statusID from PassengerStatus p where p.tripUser.user.userID=:temp1 and p.tripUser.tripDetails.tripDetailsID=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", tripScheduleDetailsID).list();
		session.close();
		if (!(list.isEmpty())) {
			return list.get(0);
		} else {
			return (-1);
		}
	}

	public List<PassengerToRouteID> getPassengerToRouteID(int userID, int vehicleScheduleID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session.createQuery(
				"from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", vehicleScheduleID).list();

		session.close();
		return list;
	}
	
	public List<PassengerToRouteID> getPassengersOfVehicleSchedule(int vehicleScheduleID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session.createQuery(
				"from PassengerToRouteID p where p.vehicleSchedule.vehicleScheduleID=:temp1")
				.setParameter("temp1", vehicleScheduleID).list();

		session.close();
		return list;
	}

	public List<PassengerToRouteID> getPassengerToRouteDetails(int userID) {
		Session session = factory.openSession();
		List<PassengerToRouteID> list = session
				.createQuery("from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1")
				.setParameter("temp1", userID).list();
		logger.debug("PassengerToRoute details fetched.");
		session.close();
		return list;
	}

	public LocalTime getScheduledArrivalTime(int stopID, int vehicleScheduleID) {
		Session session = factory.openSession();
		logger.debug("Fetching arrival time at stop");
		List<LocalTime> list = session.createQuery(
				"select r.scheduledArrivalTime from RouteStopSchedule r where r.stop.stopID=:temp1 and r.vehicleSchedule.vehicleScheduleID=:temp2")
				.setParameter("temp1", stopID).setParameter("temp2", vehicleScheduleID).list();
		session.close();
		if (!list.isEmpty()) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public School getSchool() {
		Session session = factory.openSession();
		School school = session.get(School.class, 1);
		logger.debug("School details fetched.");
		session.close();
		return school;
	}

	public boolean checkUserAppliedForLeave(int userID, int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		logger.debug("Checking user applied for leave on date.");
		List<PlannedCancelJourney> list = session.createQuery(
				"from PlannedCancelJourney p where p.user.userID=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2 and p.date=:temp3")
				.setParameter("temp1", userID).setParameter("temp2", vehicleScheduleID).setParameter("temp3", date)
				.list();
		session.close();
		return (!(list.isEmpty()));
	}

	public int scheduleLeave(PassengerToRouteID details, List<LocalDate> dates, int tripID) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (LocalDate date : dates) {
				PlannedCancelJourney obj = new PlannedCancelJourney();
				obj.setUser(details.getUserTypeOfJourneyID().getUser());
				obj.setVehicleSchedule(details.getVehicleSchedule());
				obj.setDate(date);
				obj.setRoute(details.getRoute());
				obj.setTypeOfJourney(details.getUserTypeOfJourneyID().getTypeOfJourney());
				session.save(obj);
			}
			if (tripID != (-1)) {

				PassengerStatus passengerStatus = new PassengerStatus();
				TripUser tripUser = new TripUser(details.getUserTypeOfJourneyID().getUser(),
						driverDao.getTripDetails(tripID));
				passengerStatus.setTripUser(tripUser);
				passengerStatus.setUserStatusCode(session.get(UserStatusCode.class, 4));
				passengerStatus.setTypeOfJourney(details.getUserTypeOfJourneyID().getTypeOfJourney());
				passengerStatus.setUpdatedTime(LocalDateTime.now());
				session.save(passengerStatus);
			}
			tx.commit();
			logger.debug("Leave scheduled.");
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

	public int cancelScheduledLeave(int userID, int vehicleScheduleID, LocalDate date) {

		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.createQuery(
					"delete from PlannedCancelJourney p where p.user.userID=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2 and p.date=:temp3")
					.setParameter("temp1", userID).setParameter("temp2", vehicleScheduleID).setParameter("temp3", date)
					.executeUpdate();
			tx.commit();
			logger.debug("Leave canceled.");
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

	public int getVehicleScheduleIDofStudentCorrespondingToGivenTypeOfJourney(int userID, int tyeOfJourney) {
		Session session = factory.openSession();
		logger.debug("Fetching vehicle schedule of a passenger.");
		List<Integer> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1 and p.userTypeOfJourneyID.typeOfJourney=:temp2")
				.setParameter("temp1", userID).setParameter("temp2", tyeOfJourney).list();
		session.close();
		if (!list.isEmpty()) {
			if (list.get(0) == null) {
				return (-1);
			}
			return list.get(0);
		} else {
			return (-1);
		}
	}
	public List<Integer> getVehicleScheduleIDsofPassenger(int userID) {
		Session session = factory.openSession();
		logger.debug("Fetching vehicle schedule of a passenger.");
		List<Integer> list = session.createQuery(
				"select p.vehicleSchedule.vehicleScheduleID from PassengerToRouteID p where p.userTypeOfJourneyID.user.userID=:temp1")
				.setParameter("temp1", userID).list();
		session.close();
		return list;
	}

	public int getTripIDCorrespondingToGivenVehicleSchedule(int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		LocalDateTime startTime = date.atStartOfDay();
		LocalDate nextDay = date.plusDays(1L);
		LocalDateTime lastTime = nextDay.atStartOfDay();
		logger.debug("Fetching tripID corresponding to vehicle schedule.");
		List<Integer> list = session.createQuery(
				"select t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID=:temp1 and t.staffLoginTime>=:temp2 and t.staffLoginTime<:temp3")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", startTime)
				.setParameter("temp3", lastTime).list();
		session.close();
		if (list.isEmpty()) {
			return (-1);
		} else {
			return list.get(0);
		}
	}
	public TripDetails getTripCorrespondingToGivenVehicleSchedule(int vehicleScheduleID, LocalDate date) {
		Session session = factory.openSession();
		LocalDateTime startTime = date.atStartOfDay();
		LocalDate nextDay = date.plusDays(1L);
		LocalDateTime lastTime = nextDay.atStartOfDay();
		List<TripDetails> list = session.createQuery(
				"from TripDetails t where t.vehicleSchedule.vehicleScheduleID=:temp1 and t.tripStart>=:temp2 and t.tripStart<:temp3")
				.setParameter("temp1", vehicleScheduleID).setParameter("temp2", startTime)
				.setParameter("temp3", lastTime).list();
		session.close();
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public List<LocalDate> getSheduledLeaveDates(int userID, int vehicleScheduleID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<LocalDate> dates = session.createQuery(
				"select p.date from PlannedCancelJourney p where p.user.userID=:temp1 and p.vehicleSchedule.vehicleScheduleID=:temp2 and p.date>=:temp3")
				.setParameter("temp1", userID).setParameter("temp2", vehicleScheduleID).setParameter("temp3", todayDate)
				.list();
		logger.debug("Scheduled leave dates fetched.");
		session.close();
		return dates;
	}

	// from google api, time is calculated
	public TimeRequiredToReachAtStop getTimeRequiredToReachAtStop(int tripScheduleDetailsID, int stopID) {
		Session session = factory.openSession();
		logger.debug("Fetching TimeRequiredToReachAtStop");
		List<TimeRequiredToReachAtStop> list = session.createQuery(
				"from TimeRequiredToReachAtStop t where t.tripStopID.tripDetails.tripDetailsID=:temp1 and t.tripStopID.stop.stopID=:temp2")
				.setParameter("temp1", tripScheduleDetailsID).setParameter("temp2", stopID).list();
		session.close();
		if (!list.isEmpty()) {
			return (list.get(0));
		} else {
			return null;
		}
	}

	public List<PassengerStatus> getPassengerStatus(int userID, int start, int count) {
		Session session = factory.openSession();
		List<PassengerStatus> list = session
				.createQuery("from PassengerStatus p where p.tripUser.user.userID=:temp1 order by p.id desc")
				.setParameter("temp1", userID).setFirstResult(start).setMaxResults(count).list();
		logger.debug("Fetched passenger status");
		session.close();
		return list;
	}

//	1: WhenBusArrivedAtSchool;
//	2: WhenBusLeftSchool;
//	3: WhenBusArrivedAtYourHome;
//	4: WhenBusLeftYourHome;
//5: WhenBusIsNearByMyHome;

	// use in second phase
	public ResponseEntity<Object> update_On_Off_Notification(int userID, boolean check, int parameter) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			NotificationOnOff notificationOnOff = session.get(NotificationOnOff.class, userID);
			if (parameter == 1) {
				if (check) {
					notificationOnOff.setPassengerEnteredTheBusAtHome(1);
				} else {
					notificationOnOff.setPassengerEnteredTheBusAtHome(0);
				}
			} else if (parameter == 2) {
				if (check) {
					notificationOnOff.setPassengerGotDownOfTheBusAtSchool(1);
				} else {
					notificationOnOff.setPassengerGotDownOfTheBusAtSchool(0);
				}
			} else if (parameter == 3) {
				if (check) {
					notificationOnOff.setPassengerEnteredTheBusAtSchool(1);
				} else {
					notificationOnOff.setPassengerEnteredTheBusAtSchool(0);
				}
			} else if (parameter == 4) {
				if (check) {
					notificationOnOff.setPassengerGotDownOfTheBusAtHome(1);
				} else {
					notificationOnOff.setPassengerGotDownOfTheBusAtHome(0);
				}
			} else if (parameter == 5) {
				if (check) {
					notificationOnOff.setTripStarted(1);
				} else {
					notificationOnOff.setTripStarted(0);
				}
			} else if (parameter == 6) {
				if (check) {
					notificationOnOff.setTripVerifiedByAdmin(1);
				} else {
					notificationOnOff.setTripVerifiedByAdmin(0);
				}
			}
			session.update(notificationOnOff);
			tx.commit();
			logger.debug("NotificationOnOff status updated.");
			return ResponseHandler.generateResponse2(true, "Success!!! ", HttpStatus.OK);
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

	public CurrentLatLong getBusCurrentLatLong(int tripScheduleDetailsID) {
		Session session = factory.openSession();
		CurrentLatLong obj = session.get(CurrentLatLong.class, tripScheduleDetailsID);

		session.close();
		return obj;
	}
	
	
	
	
}
