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
import org.springframework.stereotype.Repository;

import com.mastercom.embeddableclasses.TripStaffID;
import com.mastercom.embeddableclasses.TripUser;
import com.mastercom.entity.PassengerStatus;
import com.mastercom.entity.Role;
import com.mastercom.entity.TripDetails;
import com.mastercom.entity.TripToStaff;
import com.mastercom.entity.User;
import com.mastercom.entity.UserStatusCode;
import com.mastercom.entity.VehicleSchedule;
import com.mastercom.dto.VideoURL;

@Repository
public class AttendantDao {

	@Autowired
	SessionFactory factory;

	@Autowired
	DriverDao driverDao;
	
	private static final String SERVER_ERROR="Server Error!!!";

	private static final Logger logger = LogManager.getLogger(AttendantDao.class);

	public List<VehicleSchedule> getVehicleSchedulesAssignedToAttendantToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<VehicleSchedule> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2 and s.staffType.roleID=5")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's vehicle schedules assigned to attendant fetched.");
		session.close();
		return list;
	}

	public boolean endTripByAttendant(int userID, int tripScheduleDetailsID, VideoURL videoURL) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			TripToStaff tripToStaff = driverDao.getTripToStaffDataOfGivenStaff(tripScheduleDetailsID, userID);
			tx = session.beginTransaction();
			tripToStaff.setStaffVerifiedTime(LocalDateTime.now());
			tripToStaff.setStaffVerifiedVideo(videoURL.getUrl());
			session.update(tripToStaff);

			tx.commit();
			logger.debug("Attendant Verification Time is recorded.");
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

	public List<Integer> getTripIDsGoingOnForAttendant(int userID) {
		Session session = factory.openSession();
		
		List<Integer> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffVerifiedTime is null and t.staffType.roleID=5")
				.setParameter("temp1", userID).list();
		
		logger.debug("Trip IDs going on for attendant, which are not verified by attendant fetched.");
		session.close();
		return list;
	}

	public List<Integer> getVehicleSchedulesIDsVerifiedByAttendantToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		LocalDateTime startTime = todayDate.atStartOfDay();
		LocalDate nextdate = todayDate.plusDays(1L);
		LocalDateTime lastTime = nextdate.atStartOfDay();
		List<Integer> list = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffVerifiedTime is not null and t.staffLoginTime>=:temp2 and t.staffLoginTime<:temp3 and t.staffType.roleID=5")
				.setParameter("temp1", userID).setParameter("temp2", startTime).setParameter("temp3", lastTime).list();

		logger.debug("Today's Vehicle schedules which are verified by attendant, are fetched.");
		session.close();
		return list;
	}

	public List<Integer> getVehicleSchedulesIDsAssignedToAttendantToday(int userID) {
		Session session = factory.openSession();
		LocalDate todayDate = LocalDate.now();
		List<Integer> list = session.createQuery(
				"select s.vehicleScheduleDateStaffID.vehicleSchedule.vehicleScheduleID from StaffToVehicleScheduleMultiStaff s where s.vehicleScheduleDateStaffID.staff.userID=:temp1 and s.vehicleScheduleDateStaffID.date=:temp2 and s.staffType.roleID=5")
				.setParameter("temp1", userID).setParameter("temp2", todayDate).list();
		logger.debug("Today's vehicle schedules of attendant , are fetched.");
		session.close();
		return list;
	}

	public List<TripDetails> getTripDataOfGivenVehicleScheduleWhereAttendantLoginTimeIsWithinGivenPeriod(
			int vehicleScheduleID, LocalDateTime startTime, LocalDateTime endTime) {
		Session session = factory.openSession();
		List<TripDetails> tlist = session.createQuery(
				"select distinct t.tripStaffID.tripDetails from TripToStaff t where t.staffLoginTime>=:temp1 and t.staffLoginTime<:temp2 and t.tripStaffID.tripDetails.vehicleSchedule.vehicleScheduleID=:temp3 and t.staffType.roleID=5")
				.setParameter("temp1", startTime).setParameter("temp2", endTime)
				.setParameter("temp3", vehicleScheduleID).list();
		session.close();
		return tlist;
	}

	public int startVehicleScheduleByAttendant(Integer vehicleScheduleID, int userID, Integer tripDetailsID) {
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
				User attendant = session.get(User.class, userID);
				TripStaffID tripStaffID = new TripStaffID(trip, attendant);
				tripToStaff.setTripStaffID(tripStaffID);
				Role role = session.get(Role.class, 5);
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
				User attendant = session.get(User.class, userID);
				TripStaffID tripStaffID = new TripStaffID(trip, attendant);
				tripToStaff.setTripStaffID(tripStaffID);
				Role role = session.get(Role.class, 5);
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

	public List<Integer> getAttendantAllTripIDs(int userID) {
		Session session = factory.openSession();
		List<Integer> attendantAllTripIDs = session.createQuery(
				"select distinct t.tripStaffID.tripDetails.tripDetailsID from TripToStaff t where t.tripStaffID.staff.userID=:temp1 and t.staffType.roleID=5")
				.setParameter("temp1", userID).list();
		session.close();
		return attendantAllTripIDs;
	}

}
