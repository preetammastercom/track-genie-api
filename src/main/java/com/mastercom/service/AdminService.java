package com.mastercom.service;

import com.mastercom.client.GoogleMapAPIHelper;
import com.mastercom.comparator.TripIDComparator;
import com.mastercom.config.ConfigurableParameters;
import com.mastercom.dao.AdminDao;
import com.mastercom.dao.DriverDao;
import com.mastercom.dao.PassengerDao;
import com.mastercom.dto.*;
import com.mastercom.embeddableclasses.ScheduleDateID;
import com.mastercom.embeddableclasses.VehicleScheduleDateStaffID;
import com.mastercom.entity.*;
import com.mastercom.fcm.FirebaseMessagingService;
import com.mastercom.fcm.Note;
import com.mastercom.handler.InvalidData;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.repository.UserRepository;
import com.mastercom.util.EncryptionUtil;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ValueRange;
import java.util.*;
import java.util.stream.Collectors;

import static com.mastercom.constant.ApplicationConstant.*;

@Service
public class AdminService {
    @Autowired
    private GoogleMapAPIHelper googleMapAPIHelper;
    @Autowired
    private EncryptionUtil encryptionUtil;
    @Autowired
    AdminDao adminDao;
    @Autowired
    DriverDao driverDao;

    @Autowired
    UserRepository userRepository;


    @Value("${fileStoragePath}")
    private String fileStoragePath;
    @Autowired
    PassengerDao passengerDao;

    @Autowired
    FirebaseMessagingService firebaseService;

    @Autowired
    DriverService driverService;

    @Autowired
    SMSService smsService;

    @Autowired
    ConfigurableParameters configurableParameters;

    @Autowired
    private JwtService jwtService;

    private static final String SERVER_ERROR = "Server Error!!!";
    private static final String DATA_FOUND = "Data found.";
    private static final String NO_DATA_FOUND = "Data not found.";
    private static final String InBus = "In Bus";
    private static final String Picked = "Picked";

    private static final Logger logger = LogManager.getLogger(AdminService.class);

    public User getUserDetails(int userID, int roleID) {

        User user = adminDao.getUser(userID);
        if (user != null) {
            List<Role> roles = user.getRoles();
            logger.debug("Traverse roles of user");
            for (Role role : roles) {
                if (role.getRoleID() == roleID) {
                    return user;
                }
            }
            logger.debug("User has no mentioned role");
            return null;
        } else {
            logger.debug("Invalid userID");
            return null;
        }

    }

    public ResponseEntity<Object> addStudent(AddStudent addStudent) {
        try {

            int onwardRouteID = addStudent.getOnwardRouteID();
            int onwardPickupStopID = addStudent.getOnwardPickupStopID();
            int onwardDropStopID = addStudent.getOnwardDropStopID();
            int returnRouteID = addStudent.getReturnRouteID();
            int returnPickupStopID = addStudent.getReturnPickupStopID();
            int returnDropStopID = addStudent.getReturnDropStopID();

            if (((adminDao.getRoute(onwardRouteID)) == null) || ((adminDao.getRoute(returnRouteID)) == null)
                    || ((adminDao.getStop(onwardPickupStopID)) == null)
                    || ((adminDao.getStop(onwardDropStopID)) == null)
                    || ((adminDao.getStop(returnPickupStopID)) == null)
                    || ((adminDao.getStop(returnDropStopID)) == null)) {
                String output = "Invalid route or stop ID";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);

            }
            // getStopIDOfRoute
            if ((!(adminDao.getStopIDOfRoute(onwardRouteID).contains(onwardPickupStopID)))
                    || (!(adminDao.getStopIDOfRoute(onwardRouteID).contains(onwardDropStopID)))
                    || (!(adminDao.getStopIDOfRoute(returnRouteID).contains(returnPickupStopID)))
                    || (!(adminDao.getStopIDOfRoute(returnRouteID).contains(returnDropStopID)))) {
                String output = "Invalid stop ID for selcted route.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }
            String userUniqueKey = addStudent.getUserUniqueKey();

            User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(userUniqueKey);
            if ((userHavingGivenUniqueKey) != null) {
                String output = "Duplicate User Unique Key!!!   User: " + userHavingGivenUniqueKey.getUserFirstName()
                        + " " + userHavingGivenUniqueKey.getUserMiddleName() + " "
                        + userHavingGivenUniqueKey.getUserLastName() + " having School unique key "
                        + userHavingGivenUniqueKey.getUserUniqueKey()
                        + " is present in database. Entered data cannot be added in database.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }

            String str = addStudent.getUserFirstName() + addStudent.getUserMiddleName() + addStudent.getUserLastName()
                    + addStudent.getUserUniqueKey();

            String qrCode = adminDao.generateQRcode(str);
            if (qrCode == null) {
                return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String tempPassword = getAutogeneratedPassword();
            return adminDao.addStudent(addStudent, qrCode, str, encryptionUtil.encrypt(tempPassword));
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> addVehicle(VehicleDetailsDTO vehicleDetailsDTO) {
        try {

            if (adminDao.checkVehicleRegNo(vehicleDetailsDTO.getRegisterationNumber())) {
                logger.debug("Duplicate vehicle number");
                String output = "Duplicate Vehicle Number!";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);

            }

            return adminDao.addVehicle(vehicleDetailsDTO);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostConstruct
    public void populateQRs() {
        var users = userRepository.findAll();
        logger.info("populateQRs users size {}",users.size() );

        var students = users.stream().
                filter(u -> u.getRoles().stream().anyMatch(role -> Objects.equals(role.getRoleID(), PASSENGER_ROLE_ID)))
                .collect(Collectors.toList());
        logger.info("populateQRs users filtered {}",students.size() );
        students.forEach(
                passengerDTO -> {
                    String qrCodeString = passengerDTO.getUserFirstName() + passengerDTO.getUserMiddleName()
                            + passengerDTO.getUserLastName() + passengerDTO.getUserUniqueKey();

                    String qrCode = adminDao.generateQRcode(qrCodeString);
                    passengerDTO.setUserQRcode(qrCode);
                    passengerDTO.setUserQRcodeString(qrCodeString);
                    userRepository.save(passengerDTO);
                });

    }



    public ResponseEntity<Object> deleteRoute(int routeID) {
        try {
            Route route1 = adminDao.getRoute(routeID);
            if (route1 == null) {
                return InvalidData.invalidRouteID1();
            }
            List<User> users = adminDao.usersHavingGivenRoute(routeID);
            logger.debug("Fetched users of given route");
            List<String> vehicleScheduleNames = adminDao.vehicleSchedulesHavingRoute(routeID);
            logger.debug("Fetched vehiclescedules of given routeID");
            int m = users.size();
            int n = vehicleScheduleNames.size();
            int i = 1;
            if ((m != 0) && (n != 0)) {
                StringBuilder sb = new StringBuilder("Failed!!! Given route is assigned to following users:");
                for (User user : users) {
                    sb.append("     ");
                    sb.append(i);
                    sb.append(")  Unique Key: ");
                    sb.append(user.getUserUniqueKey());
                    sb.append("  Name:");
                    sb.append(user.getUserFirstName());
                    sb.append(" ");
                    sb.append(user.getUserMiddleName());
                    sb.append(" ");
                    sb.append(user.getUserLastName());
                    sb.append(". Role: ");
                    List<Role> r1 = user.getRoles();
                    for (Role role : r1) {
                        sb.append(role.getRoleName());
                    }
                    i++;
                }
                i = 1;
                sb.append(".    Given route is assigned to following vehicle schedules:");
                for (String v : vehicleScheduleNames) {
                    sb.append("     ");
                    sb.append(i);
                    sb.append(") ");
                    sb.append(v);
                    i++;
                }
                logger.debug("Failed! Route is assigned to users and schedules, and so cannot be deleted.");
                return ResponseHandler.generateResponse1(false,
                        "Failed! Route is assigned to users and schedules, and so cannot be deleted.", HttpStatus.OK,
                        sb.toString());

            } else if ((m == 0) && (n == 0)) {
                return adminDao.deleteRoute(routeID);
            } else if ((m != 0) && (n == 0)) {
                StringBuilder sb = new StringBuilder("Failed!!! Given route is assigned to following users:");

                for (User user : users) {
                    sb.append("     ");
                    sb.append(i);
                    sb.append(")  Unique Key: ");
                    sb.append(user.getUserUniqueKey());
                    sb.append("  Name:");
                    sb.append(user.getUserFirstName());
                    sb.append(" ");
                    sb.append(user.getUserMiddleName());
                    sb.append(" ");
                    sb.append(user.getUserLastName());
                    sb.append(". Role: ");
                    List<Role> r1 = user.getRoles();
                    for (Role role : r1) {
                        sb.append(role.getRoleName());
                    }
                    i++;
                }
                logger.debug("Failed! Route is assigned to users, and so cannot be deleted.");
                return ResponseHandler.generateResponse1(false,
                        "Failed!!! Route is assigned to users, and so cannot be deleted.", HttpStatus.OK,
                        sb.toString());

            } else {
                StringBuilder sb = new StringBuilder(
                        "Failed!!! Given route is assigned to following vehicle schedules:");
                for (String v : vehicleScheduleNames) {
                    sb.append("     ");
                    sb.append(i);
                    sb.append(") ");
                    sb.append(v);
                    i++;
                }
                logger.debug("Failed! Route is assigned to schedules, and so cannot be deleted.");
                return ResponseHandler.generateResponse1(false,
                        "Failed! Route is assigned to schedules, and so cannot be deleted.", HttpStatus.OK,
                        sb.toString());

            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> deleteStudent(int userID) {

        try {
            User user = adminDao.getUser(userID);
            if (user == null) {
                logger.debug("Invalid userID");
                return InvalidData.invalidUserID2();

            }
            List<Role> roles = user.getRoles();
            for (Role role : roles) {
                if (role.getRoleID() == 3) {
                    logger.debug("valid user");
                    return adminDao.deleteStudent(userID);
                }
            }
            logger.debug("Invalid user");
            String output = "Invalid User ID";
            return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> deleteDriver(int userID) {
        try {
            User user = adminDao.getUser(userID);
            if (user == null) {
                return InvalidData.invalidUserID2();

            }
            List<Role> roles = user.getRoles();
            List<Integer> roleIDs = roles.stream().map(role -> role.getRoleID()).collect(Collectors.toList());
            if (!(roleIDs.contains(4))) {
                return InvalidData.invalidUserID2();
            }
            logger.debug("valid user");
            List<Object[]> list = adminDao.vehicleSchedulesAssignedToDriverForTodayAndFutureDates(userID);
            logger.debug("Vehicle schedules assigned to driver are fetched.");
            if (!list.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "Driver data cannot be deleted, as driver is assigned to below Vehicle schedules:");
                for (Object[] v : list) {
                    sb.append("Date:");
                    sb.append(((LocalDate) v[1]));
                    sb.append("  ");
                    sb.append(" Name:'");
                    sb.append(((VehicleSchedule) v[0]).getVehicleScheduleName());
                    sb.append("'.    ");
                }
                sb.append(
                        "For deleting driver data, please assign these vehicle schedules to other driver, and then delete given driver data.");

                return ResponseHandler.generateResponse2(false, sb.toString(), HttpStatus.OK);
            } else {
                if (roles.size() > 1) {
                    logger.debug("Driver has other role also in addition to driver.");
                    return adminDao.deleteDriverWhoHasRoleInAdditionToDriver(userID);
                } else {
                    return adminDao.deleteDriver(userID);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> deleteAttendant(int userID) {

        try {
            User user = adminDao.getUser(userID);
            if (user == null) {
                return InvalidData.invalidUserID2();

            }
            List<Role> roles = user.getRoles();
            List<Integer> roleIDs = roles.stream().map(role -> role.getRoleID()).collect(Collectors.toList());
            if (!(roleIDs.contains(5))) {
                return InvalidData.invalidUserID2();
            }
            List<Object[]> list = adminDao.vehicleSchedulesAssignedToAttendantForTodayAndFutureDates(userID);
            logger.debug("vehicle schedules assigned to attendant are fetched.");
            if (!list.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "Attendant data cannot be deleted, as Attendant is assigned to below Vehicle schedules:");
                for (Object[] v : list) {
                    sb.append("Date:");
                    sb.append(((LocalDate) v[1]));
                    sb.append("  ");
                    sb.append(" Name:'");
                    sb.append(((VehicleSchedule) v[0]).getVehicleScheduleName());
                    sb.append("'.    ");
                }
                sb.append(
                        "For deleting attendant data, please assign these vehicle schedules to other attendant, and then delete given attendant data.");

                return ResponseHandler.generateResponse2(false, sb.toString(), HttpStatus.OK);
            } else {
                if (roles.size() > 1) {
                    logger.debug("Attendant has other roles also in addition to Attendant.");
                    return adminDao.deleteAttendantWhoHasRoleInAdditionToAttendant(userID);
                } else {
                    return adminDao.deleteAttendant(userID);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deleteVehicleSchedule(int vehicleScheduleID) {
        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
            if ((vehicleSchedule) == null) {
                return InvalidData.invalidVehicleScheduleID2();
            }
            List<Integer> userIDs = adminDao.vehicleSchedulesAssignedToUsers(vehicleScheduleID);
            logger.debug("passengers which are assigned given vehicle schedule are fetched.");
            if (!userIDs.isEmpty()) {
                String output = "Failed!!! As Vehicle Schedule is already assigned to student/students, it cannot be deleted.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }
            List<Integer> driverAttendantList = adminDao
                    .getDriverAttendantListOfVehicleScheduleForCurrentAndFutureDates(vehicleScheduleID);
            HashMap<String, String> map = new HashMap<>();
            map.put("vehicleScheduleName", vehicleSchedule.getVehicleScheduleName());
            map.put("routeName", vehicleSchedule.getRoute().getRouteName());
            int success = adminDao.deleteVehicleSchedule(vehicleScheduleID);
            if (success == 1) {
                for (Integer userID : driverAttendantList) {
                    driverService.notifyDriverAttendant(userID, 1, map);
                }
                String output = "Vehicle Schedule deleted successfully!!!";
                return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
            } else {
                String output = "Server Error!!! Because of 'Foreign Key Constraint' or 'network issue' or 'some other server error'!!!";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> updateVehicle(int vehicleID, VehicleDetailsDTO vehicleDetailsDTO) {
        try {

            if ((adminDao.getVehicle(vehicleID)) == null) {
                return InvalidData.invalidVehicleID2();
            }
            logger.debug("fetching Vehicle details having given RegisterationNumber is fetched.");
            List<Integer> list = adminDao.getVehicleIDHavingRegNo(vehicleDetailsDTO.getRegisterationNumber());

            if (list.size() == 1) {
                if (list.contains(vehicleID)) {
                    return adminDao.updateVehicle(vehicleID, vehicleDetailsDTO);
                } else {
                    logger.debug("Duplicate Vehicle Number");
                    String output = "Duplicate Vehicle Number!";
                    return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);

                }
            }

            return adminDao.updateVehicle(vehicleID, vehicleDetailsDTO);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> deleteVehicle(int vehicleID) {
        try {

            if ((adminDao.getVehicle(vehicleID)) == null) {
                return InvalidData.invalidVehicleID2();

            }
            List<String> vehicleSheduleNames = adminDao.getVehicleShedulesHavingGivenVehicle(vehicleID);
            logger.debug("schedules names having given vehicle mapped for current or future date, are fetched.");

            if (!vehicleSheduleNames.isEmpty()) {
                StringBuilder sb = new StringBuilder(
                        "Failed!!! Vehicle details cannot be deleted, as given vehicle is assigned to following schedules for some of the current or future dates:");

                int i = 1;
                for (String vehicleSheduleName : vehicleSheduleNames) {
                    sb.append(i);
                    sb.append(")");
                    sb.append(vehicleSheduleName);
                    sb.append("    ");
                    i++;
                }
                return ResponseHandler.generateResponse2(false, sb.toString(), HttpStatus.OK);
            }

            return adminDao.deleteVehicle(vehicleID);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getVehicleStatusList() {
        try {
            List<VehicleStatus> vehicleStatusList = new ArrayList<>();
            List<VehicleDetails> vehicles = adminDao.getAllVehicles();
            logger.debug("Vehicles are fetched.");
            if (vehicles.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            List<TripDetails> activeTrips = adminDao.getActiveTrips();
            logger.debug("Active trips fetched.");
            Set<Integer> activeVehicleIDs = new HashSet<>();
            for (TripDetails activeTrip : activeTrips) {
                activeVehicleIDs.add(adminDao
                        .getVehicleOfScheduleforGivenDate(activeTrip.getVehicleSchedule().getVehicleScheduleID(),
                                activeTrip.getTripStart().toLocalDate())
                        .getVehicleID());
            }
            for (VehicleDetails vehicle : vehicles) {
                if (activeVehicleIDs.contains(vehicle.getVehicleID())) {
                    vehicleStatusList.add(new VehicleStatus(vehicle, "On Trip"));
                } else {
                    vehicleStatusList.add(new VehicleStatus(vehicle, "Inactive"));
                }
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, vehicleStatusList);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> assignVehicleScheduleToStudent(int userID,
                                                                 OnwardReturnVehicleScheduleID onward_Return_VehicleScheduleID) {

        try {
            if (!(adminDao.checkValidUserIDInPassengerToRouteIDtable(userID))) {
                return InvalidData.invalidPassengerID2();

            }
            int onwardID = onward_Return_VehicleScheduleID.getOnwardVehicleScheduleID();
            int returnID = onward_Return_VehicleScheduleID.getReturnVehicleScheduleID();
            VehicleSchedule onwardVehicleSchedule = adminDao.getVehicleSchedule(onwardID);
            VehicleSchedule returnVehicleSchedule = adminDao.getVehicleSchedule(returnID);
            int onwardRouteID = 0;
            int returnRouteID = 0;
            List<PassengerToRouteID> passengerToRouteIDDetails = adminDao.getPassengerToRouteIDDetails(userID);
            List<Integer> previousScheduleIDs = new ArrayList<>();
            for (PassengerToRouteID obj : passengerToRouteIDDetails) {
                if (obj.getVehicleSchedule() != null) {
                    previousScheduleIDs.add(obj.getVehicleSchedule().getVehicleScheduleID());
                }
            }
            for (PassengerToRouteID p : passengerToRouteIDDetails) {
                if (p.getUserTypeOfJourneyID().getTypeOfJourney() == 1) {
                    onwardRouteID = p.getRoute().getRouteID();
                } else {
                    returnRouteID = p.getRoute().getRouteID();
                }
            }
            if (((onwardVehicleSchedule) == null) || ((returnVehicleSchedule) == null)
                    || (onwardVehicleSchedule.getRoute().getRouteID() != onwardRouteID)
                    || (returnVehicleSchedule.getRoute().getRouteID() != returnRouteID)
                    || (onwardVehicleSchedule.getTypeOfJourney() != 1)
                    || (returnVehicleSchedule.getTypeOfJourney() != 2)) {
                return InvalidData.invalidVehicleScheduleID2();

            }

            int status = adminDao.assignVehicleScheduleToStudent(userID, onward_Return_VehicleScheduleID);
            if (status == 1) {
                UserToken userToken = driverDao.getUserToken(userID);
                if (userToken != null) {
                    for (Integer previousScheduleID : previousScheduleIDs) {
                        firebaseService.unsubscribeFromPassengerScheduleTopic(previousScheduleID, userToken.getToken());
                    }
                    firebaseService.subscribeToPassengerScheduleTopic(onwardID, userToken.getToken());
                    firebaseService.subscribeToPassengerScheduleTopic(returnID, userToken.getToken());
                }
                return ResponseHandler.generateResponse2(true, "Vehicle schedules assigned to student successfully!!!",
                        HttpStatus.OK);
            } else {
                String output = "Server Error!!!";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> updateStudent(int userID, PassengerDTO passengerDTO) {
        try {
            User user = adminDao.getUser(userID);
            if (user == null || !user.getRoles().stream()
                    .anyMatch(role -> Objects.equals(role.getRoleID(), PASSENGER_ROLE_ID))) {
                return InvalidData.invalidUserID2();
            }
            String previousUniqueKey = user.getUserUniqueKey();
            String updatedUniqueKey = passengerDTO.getUserUniqueKey();
            if (!(previousUniqueKey.equals(updatedUniqueKey))) {
                User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(updatedUniqueKey);
                if ((userHavingGivenUniqueKey) != null) {
                    logger.debug("duplicate unique key");
                    return ResponseHandler.generateResponse2(false, "Failed!!! Duplicate Unique Key.", HttpStatus.OK);
                }
            }
            if ((!(user.getUserFirstName().equals(passengerDTO.getUserFirstName())))
                    || (!(user.getUserMiddleName().equals(passengerDTO.getUserMiddleName())))
                    || (!(user.getUserLastName().equals(passengerDTO.getUserLastName())))
                    || (!(user.getUserUniqueKey().equals(passengerDTO.getUserUniqueKey())))) {
                String qrCodeString = passengerDTO.getUserFirstName() + passengerDTO.getUserMiddleName()
                        + passengerDTO.getUserLastName() + passengerDTO.getUserUniqueKey();

                String qrCode = adminDao.generateQRcode(qrCodeString);
                if (qrCode == null) {
                    return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return adminDao.updateStudent(userID, passengerDTO, qrCodeString, qrCode);
            } else {
                return adminDao.updateStudent(userID, passengerDTO, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> updateStudentStopRoute(int userID, StudentStopRouteUpdate studentStopRouteUpdate) {
        try {
            if (!(adminDao.checkValidUserIDInPassengerToRouteIDtable(userID))) {
                return InvalidData.invalidPassengerID2();
            }

            if (((adminDao.getRoute(studentStopRouteUpdate.getOnwardRouteID())) == null)
                    || ((adminDao.getRoute(studentStopRouteUpdate.getReturnRouteID())) == null)
                    || ((adminDao.getStop(studentStopRouteUpdate.getOnwardPickupStopID())) == null)
                    || ((adminDao.getStop(studentStopRouteUpdate.getOnwardDropStopID())) == null)
                    || ((adminDao.getStop(studentStopRouteUpdate.getReturnPickupStopID())) == null)
                    || ((adminDao.getStop(studentStopRouteUpdate.getReturnDropStopID())) == null)) {
                logger.debug("Invalid Stop or Route ID.");
                String output = "Invalid Stop or Route ID";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.NOT_FOUND);

            }
            return adminDao.updateStudentStopRoute(userID, studentStopRouteUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getStudentStatusListOfGivenTrip(int tripScheduleDetailsID, int userIDHitAPI) {
        try {
            TripDetails trip = driverDao.getTripDetails(tripScheduleDetailsID);
            if ((trip == null) || ((!(adminDao.getUser(userIDHitAPI).getRoles().stream().map(i -> i.getRoleID())
                    .toList().contains(ADMIN_ROLE_ID)))
                    && (!(driverDao.getStaffIDsOfTrip(tripScheduleDetailsID).contains(userIDHitAPI))))) {
                logger.error("Invalid tripID");
                return ResponseHandler.generateResponse5(false, "Invalid tripID", HttpStatus.NOT_FOUND, null, null);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            int pickedCount = 0;
            int droppedCount = 0;
            int missedBusCount = 0;
            int leaveCount = 0;
            List<PassengerStatus> passengerStatusOfTrip = adminDao.getPassengerStatusOfTrip(tripScheduleDetailsID);
            List<Integer> passengerIDsOfTrip = passengerStatusOfTrip.stream()
                    .map(i -> i.getTripUser().getUser().getUserID()).collect(Collectors.toList());
            List<StudentIDNameStatus> studentIDNameStatusList = new ArrayList<>();
            for (PassengerStatus passengerStatus : passengerStatusOfTrip) {
                StudentIDNameStatus obj = new StudentIDNameStatus();
                User user = passengerStatus.getTripUser().getUser();
                obj.setUserID(user.getUserID());
                obj.setUserName(
                        user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
                obj.setUserUniqueKey(user.getUserUniqueKey());
                obj.setUserPhoto(user.getUserPhoto());
                switch (passengerStatus.getUserStatusCode().getStatusID()) {
                    case 1: {
                        pickedCount++;
                        obj.setPickedTime(passengerStatus.getPassengerPickedUpTime().format(formatter));
                        obj.setUserStatus("Picked");
                        break;
                    }
                    case 2: {
                        droppedCount++;
                        obj.setPickedTime(passengerStatus.getPassengerPickedUpTime().format(formatter));
                        obj.setDroppedTime(passengerStatus.getPassengerDropTime().format(formatter));
                        obj.setUserStatus("Dropped");
                        break;
                    }

                    case 3: {
                        missedBusCount++;
                        obj.setMissedBusTime(passengerStatus.getUpdatedTime().format(formatter));
                        obj.setUserStatus("Missed Bus");
                        break;
                    }
                    case 4: {
                        leaveCount++;
                        obj.setUserStatus("Scheduled Leave");
                        break;
                    }
                }

                studentIDNameStatusList.add(obj);
            }
            List<PassengerToRouteID> passengersOfVehicleSchedule = passengerDao
                    .getPassengersOfVehicleSchedule(trip.getVehicleSchedule().getVehicleScheduleID());
            List<PassengerToRouteID> passengersStatusNotRecordedYet = passengersOfVehicleSchedule.stream()
                    .filter(i -> !(passengerIDsOfTrip.contains(i.getUserTypeOfJourneyID().getUser().getUserID())))
                    .collect(Collectors.toList());
            for (PassengerToRouteID passengerToRouteID : passengersStatusNotRecordedYet) {
                StudentIDNameStatus obj = new StudentIDNameStatus();
                User user = passengerToRouteID.getUserTypeOfJourneyID().getUser();
                obj.setUserID(user.getUserID());
                obj.setUserName(
                        user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
                obj.setUserUniqueKey(user.getUserUniqueKey());
                obj.setUserPhoto(user.getUserPhoto());
                obj.setUserStatus("Yet To Be Picked");

                studentIDNameStatusList.add(obj);
            }
            ////////////////////////

            VehicleSchedule vehicleSchedule = trip.getVehicleSchedule();
            TripData object = new TripData();
            object.setVehicleScheduleID(vehicleSchedule.getVehicleScheduleID());
            object.setVehicleScheduleName(vehicleSchedule.getVehicleScheduleName());
            object.setRouteID(vehicleSchedule.getRoute().getRouteID());
            object.setRouteName(vehicleSchedule.getRoute().getRouteName());
            object.setPickedPassengersCount(pickedCount);
            object.setTotalCount(passengersOfVehicleSchedule.size());
            object.setMissedBusCount(missedBusCount);
            object.setLeaveCount(leaveCount);
            object.setTotalPassengersExcludingLeave((passengersOfVehicleSchedule.size()) - leaveCount);
            object.setVehicleRegistrationNumber(
                    adminDao.getVehicleOfScheduleforGivenDate(vehicleSchedule.getVehicleScheduleID(),
                            trip.getTripStart().toLocalDate()).getRegisterationNumber());
            List<TripToStaff> tripStaffs = driverDao.getTripToStaffData(tripScheduleDetailsID);
            List<Integer> tripStaffIDs = tripStaffs.stream().map(i -> i.getTripStaffID().getStaff().getUserID())
                    .collect(Collectors.toList());
            List<StaffToVehicleScheduleMultiStaff> staffOfVehicleSchedule = adminDao.getStaffOfVehicleSchedule(
                    vehicleSchedule.getVehicleScheduleID(), trip.getTripStart().toLocalDate());
            List<StaffToVehicleScheduleMultiStaff> staffNotLoggedin = staffOfVehicleSchedule.stream()
                    .filter(i -> !(tripStaffIDs.contains(i.getVehicleScheduleDateStaffID().getStaff().getUserID())))
                    .collect(Collectors.toList());
            object.setDriversLoggedin(tripStaffs.stream().filter(i -> i.getStaffType().getRoleID() == 4)
                    .map(i -> i.getTripStaffID().getStaff().getUserFirstName() + " "
                            + i.getTripStaffID().getStaff().getUserMiddleName() + " "
                            + i.getTripStaffID().getStaff().getUserLastName())
                    .collect(Collectors.toList()));
            object.setAttendantsLoggedin(tripStaffs.stream().filter(i -> i.getStaffType().getRoleID() == 5)
                    .map(i -> i.getTripStaffID().getStaff().getUserFirstName() + " "
                            + i.getTripStaffID().getStaff().getUserMiddleName() + " "
                            + i.getTripStaffID().getStaff().getUserLastName())
                    .collect(Collectors.toList()));
            object.setDriversNotLoggedin(staffNotLoggedin.stream().filter(i -> i.getStaffType().getRoleID() == 4)
                    .map(i -> i.getVehicleScheduleDateStaffID().getStaff().getUserFirstName() + " "
                            + i.getVehicleScheduleDateStaffID().getStaff().getUserMiddleName() + " "
                            + i.getVehicleScheduleDateStaffID().getStaff().getUserLastName())
                    .collect(Collectors.toList()));
            object.setAttendantsNotLoggedin(staffNotLoggedin.stream().filter(i -> i.getStaffType().getRoleID() == 5)
                    .map(i -> i.getVehicleScheduleDateStaffID().getStaff().getUserFirstName() + " "
                            + i.getVehicleScheduleDateStaffID().getStaff().getUserMiddleName() + " "
                            + i.getVehicleScheduleDateStaffID().getStaff().getUserLastName())
                    .collect(Collectors.toList()));
            if (droppedCount > 0) {
                object.setPassengerStatusMessage(InBus);
            } else {
                object.setPassengerStatusMessage(Picked);
            }

            object.setTripStartTime(trip.getTripStart().format(formatter));
            CurrentLatLong currentLatLong = passengerDao.getBusCurrentLatLong(tripScheduleDetailsID);
            if (currentLatLong != null) {
                object.setBusCurrentLat(currentLatLong.getBusCurrentLat());
                object.setBusCurrentLong(currentLatLong.getBusCurrentLong());
            }

            return ResponseHandler.generateResponse5(true, DATA_FOUND, HttpStatus.OK, studentIDNameStatusList, object);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse5(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public List<ActiveRouteData> getActiveRouteList(int typeOfJourney) {
        List<ActiveRouteData> list = new ArrayList<>();
        List<TripDetails> activeTrips = adminDao.getActiveRoutes(typeOfJourney);
        Collections.sort(activeTrips, new TripIDComparator());
        logger.debug("Fetched data of active routes.");
        List<Integer> activeTripsIDs = activeTrips.stream().map(obj -> obj.getTripDetailsID())
                .collect(Collectors.toList());
        List<Integer> activeVehicleScheduleIDsList = activeTrips.stream()
                .map(obj -> obj.getVehicleSchedule().getVehicleScheduleID()).collect(Collectors.toList());
        HashSet<Integer> activeVehicleScheduleIDs = new HashSet<>();
        activeVehicleScheduleIDs.addAll(activeVehicleScheduleIDsList);
        List<PassengerStatus> activePassengers = adminDao.getPassengerStatusOfTrips(activeTripsIDs);
        List<TripToStaff> staffOfActiveTrips = adminDao.getStaffDataOfTrips(activeTripsIDs);

        List<PassengerToRouteID> allPassengersOfVehicleSchedules = adminDao
                .getAllPassengersIDsOfGivenVehicleSchedules(activeVehicleScheduleIDs);
        LinkedHashMap<Integer, List<PassengerStatus>> passengersMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, List<TripToStaff>> tripStaffMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, Integer> totalPassengersCountMap = new LinkedHashMap<>();
        HashMap<Integer, List<LocalDate>> vehicleSchedulesDates = new HashMap<>();
        for (TripDetails trip : activeTrips) {
            int vehicleScheduleID = trip.getVehicleSchedule().getVehicleScheduleID();
            if (vehicleSchedulesDates.containsKey(vehicleScheduleID)) {
                List<LocalDate> dates = vehicleSchedulesDates.get(vehicleScheduleID);
                dates.add(trip.getTripStart().toLocalDate());
                vehicleSchedulesDates.put(vehicleScheduleID, dates);
            } else {
                List<LocalDate> dates = new ArrayList<>();
                dates.add(trip.getTripStart().toLocalDate());
                vehicleSchedulesDates.put(vehicleScheduleID, dates);
            }
        }
        HashMap<Integer, HashMap<LocalDate, List<StaffToVehicleScheduleMultiStaff>>> vehicleScheduleDateStaffMapping = adminDao
                .getStaffOfVehicleSchedulesForGivenDates(vehicleSchedulesDates);
        for (Integer i : activeTripsIDs) {
            passengersMap.put(i, new ArrayList<>());
            tripStaffMap.put(i, new ArrayList<>());
        }
        for (Integer i : activeVehicleScheduleIDs) {
            totalPassengersCountMap.put(i, 0);
        }
        for (PassengerToRouteID obj : allPassengersOfVehicleSchedules) {
            int vehicleScheduleID = obj.getVehicleSchedule().getVehicleScheduleID();
            int value = totalPassengersCountMap.get(vehicleScheduleID);
            totalPassengersCountMap.put(vehicleScheduleID, value + 1);
        }
        for (TripToStaff obj : staffOfActiveTrips) {
            int tripID = obj.getTripStaffID().getTripDetails().getTripDetailsID();
            List<TripToStaff> value = tripStaffMap.get(tripID);
            value.add(obj);
            tripStaffMap.put(tripID, value);
        }

        for (PassengerStatus obj : activePassengers) {
            int tripID = obj.getTripUser().getTripDetails().getTripDetailsID();
            List<PassengerStatus> value = passengersMap.get(tripID);
            value.add(obj);
            passengersMap.put(tripID, value);
        }

        LinkedHashMap<Integer, List<StaffToVehicleScheduleMultiStaff>> tripStaffNotLoggedinMap = new LinkedHashMap<>();

        for (TripDetails trip : activeTrips) {
            List<Integer> staffIDsLoggedin = tripStaffMap.get(trip.getTripDetailsID()).stream()
                    .map(obj -> obj.getTripStaffID().getStaff().getUserID()).collect(Collectors.toList());
            int vehicleScheduleID = tripStaffMap.get(trip.getTripDetailsID()).get(0).getTripStaffID().getTripDetails()
                    .getVehicleSchedule().getVehicleScheduleID();
            List<StaffToVehicleScheduleMultiStaff> staffNotLoggedin = vehicleScheduleDateStaffMapping
                    .get(vehicleScheduleID).get(trip.getTripStart().toLocalDate()).stream()
                    .filter(obj -> !(staffIDsLoggedin
                            .contains(obj.getVehicleScheduleDateStaffID().getStaff().getUserID())))
                    .collect(Collectors.toList());
            tripStaffNotLoggedinMap.put(trip.getTripDetailsID(), staffNotLoggedin);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        for (TripDetails trip : activeTrips) {
            ActiveRouteData activeRouteData = new ActiveRouteData();
            int tripDetailsID = trip.getTripDetailsID();
            int pickedCount = 0;
            int missedBusCount = 0;
            int leaveCount = 0;
            int droppedCount = 0;
            List<PassengerStatus> passengerStatusOfTrip = passengersMap.get(tripDetailsID);
            for (PassengerStatus i : passengerStatusOfTrip) {
                switch (i.getUserStatusCode().getStatusID()) {
                    case 1: {
                        pickedCount++;
                        break;

                    }
                    case 2: {
                        droppedCount++;
                        break;
                    }
                    case 3: {
                        missedBusCount++;
                        break;
                    }
                    case 4: {
                        leaveCount++;
                        break;
                    }
                }
            }
            int vehicleScheduleID = trip.getVehicleSchedule().getVehicleScheduleID();
            StudentCount studentCount = new StudentCount();
            int totalCount = totalPassengersCountMap.get(vehicleScheduleID);
            studentCount.setTotalStudents(totalCount);
            studentCount.setPickedStudentsCount(pickedCount);
            studentCount.setMissedBusCount(missedBusCount);
            studentCount.setLeaveCount(leaveCount);
            studentCount.setTotalStudentsExcludingStudentsOnScheduldeLeave(totalCount - leaveCount);
            if (droppedCount > 0) {
                studentCount.setPassengerStatusMessage(InBus);
            } else {
                studentCount.setPassengerStatusMessage(Picked);
            }
            List<StaffUploadedVideo> drivers = new ArrayList<>();
            List<StaffUploadedVideo> attendants = new ArrayList<>();
            List<TripToStaff> tripToStaffs = tripStaffMap.get(tripDetailsID);
            for (TripToStaff tripToStaff : tripToStaffs) {

                User staff = tripToStaff.getTripStaffID().getStaff();
                StaffUploadedVideo staffUploadedVideo = new StaffUploadedVideo();
                staffUploadedVideo.setUserID(staff.getUserID());
                staffUploadedVideo.setUserName(
                        staff.getUserFirstName() + " " + staff.getUserMiddleName() + " " + staff.getUserLastName());
                staffUploadedVideo.setStaffClickedTripStart(tripToStaff.getStaffLoginTime().format(formatter));
                String videoURL = tripToStaff.getStaffVerifiedVideo();
                if (videoURL != null) {
                    staffUploadedVideo.setVideoURL(videoURL);
                    if ((tripToStaff.getAdminVerifiedTime()) != null) {
                        staffUploadedVideo.setAdminVerifiedVideo(true);
                    }
                }
                if (tripToStaff.getStaffType().getRoleID() == 4) {
                    drivers.add(staffUploadedVideo);
                } else if (tripToStaff.getStaffType().getRoleID() == 5) {
                    attendants.add(staffUploadedVideo);
                }
            }
            activeRouteData.setTripStartTime(trip.getTripStart().format(formatter));
            List<StaffToVehicleScheduleMultiStaff> staffsNotLoggedin = tripStaffNotLoggedinMap.get(tripDetailsID);
            List<User> driversNotLoggedin = staffsNotLoggedin.stream().filter(i -> i.getStaffType().getRoleID() == 4)
                    .map(i -> i.getVehicleScheduleDateStaffID().getStaff()).collect(Collectors.toList());
            List<String> driverNamesNotLoggedin = driversNotLoggedin.stream()
                    .map(i -> i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())
                    .collect(Collectors.toList());
            List<User> attendantsNotLoggedin = staffsNotLoggedin.stream().filter(i -> i.getStaffType().getRoleID() == 5)
                    .map(i -> i.getVehicleScheduleDateStaffID().getStaff()).collect(Collectors.toList());
            List<String> attendantNamesNotLoggedin = attendantsNotLoggedin.stream()
                    .map(i -> i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())
                    .collect(Collectors.toList());
            activeRouteData.setDriversNotLoggedin(driverNamesNotLoggedin);
            activeRouteData.setAttendantsNotLoggedin(attendantNamesNotLoggedin);

            activeRouteData.setTripDetailsID(tripDetailsID);
            activeRouteData.setRouteName(trip.getVehicleSchedule().getRoute().getRouteName());
            activeRouteData.setStudentCount(studentCount);
            activeRouteData.setDrivers(drivers);
            activeRouteData.setAttendants(attendants);
            activeRouteData.setVehicleRegisterationNumber(
                    adminDao.getVehicleOfScheduleforGivenDate(trip.getVehicleSchedule().getVehicleScheduleID(),
                            trip.getTripStart().toLocalDate()).getRegisterationNumber());
            list.add(activeRouteData);
        }
        return list;
    }

    public ResponseEntity<Object> fileUpload(MultipartFile multipartFile) {
        try {
            logger.debug("start of method");
            if (!((multipartFile.getContentType().equals("image/jpeg"))
                    || (multipartFile.getContentType().equals("application/pdf"))
                    || (multipartFile.getContentType().equals("image/png")))) {
                return ResponseHandler.generateResponse1(false, "Only .jpeg, .jpg, .png, .pdf file is allowed",
                        HttpStatus.OK, null);
            }
            return adminDao.fileUpload(multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getReport(String dateInFormOfString, int typeOfJourney) {
        try {
            LocalDate date = LocalDate.parse(dateInFormOfString);

            if (date.isAfter(LocalDate.now())) {
                logger.debug("Future Date is selected. Please select current or past date.");
                String output = "Future Date is selected. Please select current or past date.";
                return ResponseHandler.generateResponse1(false, output, HttpStatus.OK, null);

            }

            if (!((typeOfJourney == 1) || (typeOfJourney == 2))) {
                return InvalidData.invalidTypeOfJourney1();

            }
            List<TripDetails> trips = adminDao.fetchTripsOfGivenDateAndTypeOfJourney(date, typeOfJourney);
            logger.debug("Trips fetched.");
            if (trips.isEmpty()) {

                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);

            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            List<Report1> list = new ArrayList<>();
            for (TripDetails trip : trips) {
                Report1 obj = new Report1();
                Route route = trip.getVehicleSchedule().getRoute();
                obj.setRouteID(route.getRouteID());
                obj.setRouteName(route.getRouteName());
                obj.setVehicleScheduleID(trip.getVehicleSchedule().getVehicleScheduleID());
                obj.setVehicleScheduleName(trip.getVehicleScheduleName());
                if (trip.getBusReachedDestination() != null) {
                    obj.setTripDestinationTime(trip.getBusReachedDestination().format(formatter));
                } else {
                    obj.setTripDestinationTime("-");
                }
                List<TripStaff> drivers = new ArrayList<>();
                List<TripStaff> attendants = new ArrayList<>();
                List<TripToStaff> tripStaffData = driverDao.getTripToStaffData(trip.getTripDetailsID());
                // LocalDateTime tripStartTime = tripStaffData.get(0).getStaffLoginTime();

                for (TripToStaff tripToStaff : tripStaffData) {

                    TripStaff tripStaff = new TripStaff();
                    User user = tripToStaff.getTripStaffID().getStaff();
                    tripStaff.setStaffName(
                            user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName());
                    tripStaff.setStaffUniqueKey(user.getUserUniqueKey());
                    tripStaff.setStaffLoginTime(tripToStaff.getStaffLoginTime().format(formatter));
                    if (tripToStaff.getStaffVerifiedTime() != null) {
                        tripStaff.setStaffVerifiedTime(tripToStaff.getStaffVerifiedTime().format(formatter));
                    } else {
                        tripStaff.setStaffVerifiedTime("Not Verified.");
                    }
                    if (tripToStaff.getAdminVerifiedTime() != null) {
                        tripStaff.setAdminVerifiedTime(tripToStaff.getAdminVerifiedTime().format(formatter));
                    } else {
                        tripStaff.setAdminVerifiedTime("Not Verified.");
                    }
                    if (tripToStaff.getStaffVerifiedVideo() != null) {
                        tripStaff.setStaffVerifiedVideo(tripToStaff.getStaffVerifiedVideo());
                    } else {
                        tripStaff.setStaffVerifiedVideo("Video Unavailable");
                    }
                    if (tripToStaff.getStaffType().getRoleID() == 4) {
                        drivers.add(tripStaff);
                    } else {
                        attendants.add(tripStaff);
                    }
                }
                obj.setTripStartTime(trip.getTripStart().format(formatter));
                obj.setDrivers(drivers);
                obj.setAttendants(attendants);
                list.add(obj);
            }
            logger.debug(DATA_FOUND);
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);

        } catch (Exception e) {
            e.printStackTrace();
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
                logger.debug("Valid Passenger");
                return true;
            }
        }
        logger.debug("User is not passenger.");
        return false;

    }

    public ResponseEntity<Object> getOnwardActiveRouteList() {
        try {
            List<ActiveRouteData> list = getActiveRouteList(1);
            logger.debug("Onward active route list data is fetched.");
            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            } else {
                return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);

            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);

        }
    }

    public ResponseEntity<Object> getReturnActiveRouteList() {
        try {
            List<ActiveRouteData> list = getActiveRouteList(2);
            logger.debug("Return active route list data is fetched.");
            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            } else {
                return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);

            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getDriverList() {
        try {
            List<UserRoutes> userRoutes = getUsersRoutesListOfGivenRole(DRIVER_ROLE_ID);
            if (userRoutes.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            logger.debug("All Driver details with routes are fetched.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, userRoutes);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    private List<UserRoutes> getUsersRoutesListOfGivenRole(Integer roleID) {
        List<Object[]> usersHavingRoutes = adminDao.getRoutesAssignedForTodayAndFutureToGivenRoleUsers(roleID);
        List<UserRoutes> output = new ArrayList<>();
        User tempUser = (User) (usersHavingRoutes.get(0)[0]);
        HashSet<Integer> routeIDs = new HashSet<>();
        for (Object[] obj : usersHavingRoutes) {
            User currentUser = (User) obj[0];
            int currentRouteID = (int) obj[1];
            if (tempUser.getUserID() == currentUser.getUserID()) {
                routeIDs.add(currentRouteID);
            } else {
                output.add(getDataInFormOfUserRoutes(tempUser, routeIDs));
                tempUser = currentUser;
                routeIDs = new HashSet<>();
                routeIDs.add(currentRouteID);
            }
        }
        output.add(getDataInFormOfUserRoutes(tempUser, routeIDs));
        List<Integer> userIDsHavingRoutes = output.stream().map(obj -> obj.getUserID()).collect(Collectors.toList());
        output.addAll(adminDao.getUserListOfGivenRole(roleID).stream()
                .filter(obj -> !userIDsHavingRoutes.contains(obj.getUserID()))
                .map(obj -> getDataInFormOfUserRoutes(obj, null)).collect(Collectors.toList()));
        if (roleID == ATTENDANT_ROLE_ID) {
            output.forEach(obj -> obj.setDrivingLicense(null));
        }
        return output;
    }

    private UserRoutes getDataInFormOfUserRoutes(User user, HashSet<Integer> routeIDs) {
        UserRoutes obj = new UserRoutes();
        BeanUtils.copyProperties(user, obj);
        obj.setRoutes(routeIDs);
        return obj;
    }

    public ResponseEntity<Object> getAttendantList() {
        try {
            List<UserRoutes> userRoutes = getUsersRoutesListOfGivenRole(ATTENDANT_ROLE_ID);
            if (userRoutes.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            logger.debug("All attendant details with routes are fetched.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, userRoutes);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> getRouteList() {
        try {
            List<Route> routes = adminDao.getRouteList();
            logger.debug("All Route details are fetched.");
            if (routes.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, routes);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getStopsofRoute(int routeID) {
        try {
            List<Stop> stops = adminDao.getStopsofRoute(routeID);
            logger.debug("Stops of Route are fetched.");
            if (stops.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, stops);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getRouteListWithSourceAndDestination() {
        try {
            List<RouteWithSourceDestination> routesS_D = adminDao.getRouteListWithSourceAndDestination();
            logger.debug("Route list with source and destination is Fetched.");
            if (routesS_D.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, routesS_D);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getStudentList() {
        try {
            List<StudentRouteStop> students = adminDao.getStudentList();
            logger.debug("Students details are fetched.");
            if (students.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, students);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getVehicleScheduleList() {
        try {
            List<VehicleSchedule> vehicleSchedules = adminDao.getVehicleScheduleList();
            logger.debug("vehicleSchedules are fetched.");
            if (vehicleSchedules.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, vehicleSchedules);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getVehicle_ID_RegisterationNumberList() {
        try {
            List<VehicleIDRegisterationNumber> list = adminDao.getVehicle_ID_RegisterationNumberList();
            logger.debug("Vehicle details are fetched");
            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            } else {
                return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getVehicleShedulesOfRoute(int routeID) {
        try {
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID1();

            }
            List<VehicleScheduleIDName> list = adminDao.getVehicleShedulesOfRoute(routeID);
            logger.debug("Vehicle schedules of route are fetched.");
            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);

            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getVehicleSheduleIDNameWithPassengerCountOfGivenRouteForGivenTypeOfJourney(
            int routeID, int typeOfJourney) {

        try {
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID1();

            }
            List<Object[]> list = adminDao
                    .getVehicleShedulesWithPassengersCountOfGivenRouteForGivenTypeOfJourney(routeID, typeOfJourney);
            logger.debug(" Vehicle schedules of route for given type of journey are fetched.");

            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);

            }
            List<VehicleScheduleIDNamePassengersCount> VehicleScheduleIDNamePassengersCountList = new ArrayList<>();
            for (Object[] obj : list) {
                VehicleScheduleIDNamePassengersCountList
                        .add(new VehicleScheduleIDNamePassengersCount((int) obj[0], (String) obj[1], (long) obj[2]));
            }
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
                    VehicleScheduleIDNamePassengersCountList);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> downloadFile(String f) {
        try {
            return adminDao.downloadFile(f);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> generateVehicleScheduleName(
            VehicleScheduleNameGeneration vehicleScheduleNameGeneration) {
        try {
            int routeID = vehicleScheduleNameGeneration.getRouteID();
            if ((adminDao.getRoute(routeID)) == null) {
                return ResponseHandler.generateResponse1(false, "Invalid routeID.", HttpStatus.NOT_FOUND, null);
            }
            int typeOfJourney = vehicleScheduleNameGeneration.getTypeOfJourney();
            if (!((typeOfJourney == 1) || (typeOfJourney == 2))) {
                return ResponseHandler.generateResponse1(false, "Invalid type of journey.", HttpStatus.NOT_FOUND, null);
            }
            logger.debug("Fetching stops");
            List<RouteStop> stopsWithStopOrderOfRoute = adminDao.getRouteStopOrderOfRoute(routeID);

            int size = stopsWithStopOrderOfRoute.size();
            if (size != 0) {
                String vehicleScheduleName;
                if (typeOfJourney == 1) {
                    String startStopName = stopsWithStopOrderOfRoute.get(0).getStop().getStopName();
                    String destinationStopName = stopsWithStopOrderOfRoute.get(size - 1).getStop().getStopName();
                    String departureTime = vehicleScheduleNameGeneration.getScheduledDepartureTime();
                    vehicleScheduleName = startStopName + "_" + destinationStopName + "@" + departureTime;

                } else {
                    String startStopName = stopsWithStopOrderOfRoute.get(size - 1).getStop().getStopName();
                    String destinationStopName = stopsWithStopOrderOfRoute.get(0).getStop().getStopName();
                    String departureTime = vehicleScheduleNameGeneration.getScheduledDepartureTime();
                    vehicleScheduleName = startStopName + "_" + destinationStopName + "@" + departureTime;

                }
                List<String> vehicleScheduleNamesList = adminDao.getVehicleScheduleNamesList();
                String name = vehicleScheduleName;
                for (int i = 2; vehicleScheduleNamesList.contains(vehicleScheduleName); i++) {
                    vehicleScheduleName = name + "_" + i;
                }
                return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, vehicleScheduleName);
            } else {
                logger.debug("Stops are not mapped to route, hence schedule name can't be generated.");
                return ResponseHandler.generateResponse1(false,
                        "Please map stops to selected route, and then schedule name can be generated.", HttpStatus.OK,
                        null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> getCurrentAndStartEndLatLongOfActiveRoutes(int typeOfJourney) {
        try {
            if (!((typeOfJourney == 1) || (typeOfJourney == 2))) {
                return InvalidData.invalidTypeOfJourney1();
            }
            List<TripDetails> list = adminDao.getActiveRoutes(typeOfJourney);
            if (list.isEmpty()) {
                logger.debug("No active route");
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            List<CurrentLatLongVehicleScheduleName> listOfCurrentLatLongOfActiveTrips = new ArrayList<>();
            for (TripDetails trip : list) {
                CurrentLatLongVehicleScheduleName currentLatLong_VehicleScheduleName = new CurrentLatLongVehicleScheduleName();

                int routeID = trip.getVehicleSchedule().getRoute().getRouteID();
                List<RouteStop> stopsWithStopOrder = adminDao.getRouteStopOrderOfRoute(routeID);
                int size = stopsWithStopOrder.size();
                if (size != 0) {
                    if ((trip.getVehicleSchedule().getTypeOfJourney()) == 1) {
                        currentLatLong_VehicleScheduleName
                                .setSourceLat(stopsWithStopOrder.get(0).getStop().getStopLatitude());
                        currentLatLong_VehicleScheduleName
                                .setSourceLong(stopsWithStopOrder.get(0).getStop().getStopLongitude());
                        currentLatLong_VehicleScheduleName
                                .setDestLat(stopsWithStopOrder.get(size - 1).getStop().getStopLatitude());
                        currentLatLong_VehicleScheduleName
                                .setDestLong(stopsWithStopOrder.get(size - 1).getStop().getStopLongitude());
                    } else {
                        currentLatLong_VehicleScheduleName
                                .setSourceLat(stopsWithStopOrder.get(size - 1).getStop().getStopLatitude());
                        currentLatLong_VehicleScheduleName
                                .setSourceLong(stopsWithStopOrder.get(size - 1).getStop().getStopLongitude());
                        currentLatLong_VehicleScheduleName
                                .setDestLat(stopsWithStopOrder.get(0).getStop().getStopLatitude());
                        currentLatLong_VehicleScheduleName
                                .setDestLong(stopsWithStopOrder.get(0).getStop().getStopLongitude());
                    }
                }
                currentLatLong_VehicleScheduleName.setTripID(trip.getTripDetailsID());
                currentLatLong_VehicleScheduleName.setVehicleScheduleName(trip.getVehicleScheduleName());
                CurrentLatLong currentLatLong = passengerDao.getBusCurrentLatLong(trip.getTripDetailsID());
                if (currentLatLong != null) {
                    currentLatLong_VehicleScheduleName.setBusCurrentLat(currentLatLong.getBusCurrentLat());
                    currentLatLong_VehicleScheduleName.setBusCurrentLong(currentLatLong.getBusCurrentLong());
                }
                listOfCurrentLatLongOfActiveTrips.add(currentLatLong_VehicleScheduleName);
            }
            logger.debug(DATA_FOUND);
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
                    listOfCurrentLatLongOfActiveTrips);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> adminVerifiedVideoUploadedByGivenUser(int tripDetailsID, int userID) {
        try {
            TripDetails trip = driverDao.getTripDetails(tripDetailsID);
            if (trip == null) {
                return InvalidData.invalidTripID2();
            }
            User user = adminDao.getUser(userID);
            if (user == null) {
                return InvalidData.invalidUserID2();
            }
            TripToStaff tripToStaff = driverDao.getTripToStaffDataOfGivenStaff(tripDetailsID, userID);
            if (tripToStaff == null) {
                logger.debug("Invalid user ID for a given trip.");
                return ResponseHandler.generateResponse2(false, "Invalid user ID for a given trip.",
                        HttpStatus.NOT_FOUND);
            }
            if (tripToStaff.getAdminVerifiedTime() != null) {
                logger.debug("Failed! Verification time is already recorded.");
                return ResponseHandler.generateResponse2(false, "Failed! Verification time is already recorded.",
                        HttpStatus.OK);
            }
            int flag = adminDao.adminVerifiedVideoUploadedByGivenUser(tripDetailsID, userID);
            switch (flag) {
                case 1: {
                    firebaseService.sendRefreshMessageToAdminTopic();
                    return ResponseHandler.generateResponse2(true, "Verification Time recorded!", HttpStatus.OK);
                }
                case 2: {
                    firebaseService.sendMulticastAsync(
                            new Note("Trip Verified.",
                                    "Trip of vehicle schedule '" + trip.getVehicleScheduleName()
                                            + "' is verified by admin."),
                            adminDao.getTokensOfPassengersWhoEnabledNotificationOfTripVerifiedByAdmin(
                                    trip.getVehicleSchedule().getVehicleScheduleID()));

                    firebaseService.sendRefreshMessageToAdminTopic();
                    return ResponseHandler.generateResponse2(true, "Verification Time recorded!", HttpStatus.OK);
                }
                default: {
                    return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> adminClickedOnVerifyAllVideos(int tripDetailsID) {
        TripDetails trip = driverDao.getTripDetails(tripDetailsID);
        if (trip == null) {
            return InvalidData.invalidTripID2();
        }
        if ((driverDao.getTripToStaffData(tripDetailsID).stream().filter(i -> i.getStaffVerifiedTime() == null).count()) != 0) {
            logger.debug("Failed! All staff of trip has not uploaded videos yet.");
            return ResponseHandler.generateResponse2(false, "Failed! All staff of trip has not uploaded videos yet.", HttpStatus.OK);
        }
        int flag = adminDao.adminClickedOnVerifyAllVideos(tripDetailsID);
        if (flag == 1) {
            firebaseService
                    .sendMulticastAsync(
                            new Note("Trip Verified.",
                                    "Trip of vehicle schedule '" + trip.getVehicleScheduleName()
                                            + "' is verified by admin."),
                            adminDao.getTokensOfPassengersWhoEnabledNotificationOfTripVerifiedByAdmin(
                                    trip.getVehicleSchedule().getVehicleScheduleID()));

            firebaseService.sendRefreshMessageToAdminTopic();
            return ResponseHandler.generateResponse2(true, "Verification Time recorded!!!", HttpStatus.OK);
        } else {
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> addDriver(DriverDTO driverDTO) {
        try {
            String userUniqueKey = driverDTO.getUserUniqueKey();
            User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(userUniqueKey);
            if ((userHavingGivenUniqueKey) != null) {
                String output = "Duplicate User Unique Key!!!   User: " + userHavingGivenUniqueKey.getUserFirstName()
                        + " " + userHavingGivenUniqueKey.getUserMiddleName() + " "
                        + userHavingGivenUniqueKey.getUserLastName() + " having School unique key "
                        + userHavingGivenUniqueKey.getUserUniqueKey()
                        + " is present in database. Entered data cannot be added in database.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }
            return adminDao.addNewDriver(driverDTO, encryptionUtil.encrypt(getAutogeneratedPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> addAttendant(AttendantDTO attendantDTO) {
        try {
            String userUniqueKey = attendantDTO.getUserUniqueKey();
            User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(userUniqueKey);
            if ((userHavingGivenUniqueKey) != null) {
                String output = "Duplicate User Unique Key!!!   User: " + userHavingGivenUniqueKey.getUserFirstName()
                        + " " + userHavingGivenUniqueKey.getUserMiddleName() + " "
                        + userHavingGivenUniqueKey.getUserLastName() + " having School unique key "
                        + userHavingGivenUniqueKey.getUserUniqueKey()
                        + " is present in database. Entered data cannot be added in database.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }
            return adminDao.addNewAttendant(attendantDTO, encryptionUtil.encrypt(getAutogeneratedPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> updateDriver(int userID, DriverDTO driverDTO) {
        try {
            User user = adminDao.getUser(userID);
            if (user == null
                    || !user.getRoles().stream().anyMatch(role -> Objects.equals(role.getRoleID(), DRIVER_ROLE_ID))) {
                return InvalidData.invalidUserID2();
            }
            String updatedUniquekey = driverDTO.getUserUniqueKey();
            if (!(user.getUserUniqueKey().equals(updatedUniquekey))) {
                User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(updatedUniquekey);
                if ((userHavingGivenUniqueKey) != null) {
                    logger.debug("Duplicate unique key in user list of role");
                    String output = "Failed!!! Duplicate user unique key.";
                    return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
                }
            }
            return adminDao.updateDriver(userID, driverDTO);

        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> updateAttendant(int userID, AttendantDTO attendantDTO) {
        try {
            User user = adminDao.getUser(userID);
            if (user == null || !user.getRoles().stream()
                    .anyMatch(role -> Objects.equals(role.getRoleID(), ATTENDANT_ROLE_ID))) {
                return InvalidData.invalidUserID2();
            }
            String updatedUniqueKey = attendantDTO.getUserUniqueKey();
            if (!(user.getUserUniqueKey().equals(updatedUniqueKey))) {
                User userHavingGivenUniqueKey = adminDao.getUserHavingGivenUniqueKey(updatedUniqueKey);
                if ((userHavingGivenUniqueKey) != null) {
                    logger.debug("Duplicate unique key in user list of role");
                    String output = "Failed!!! Duplicate user unique key.";
                    return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
                }
            }
            return adminDao.updateAttendant(userID, attendantDTO);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates(int roleID,
                                                                                                      int vehicleScheduleID) {
        try {
            if (!((roleID == 4) || (roleID == 5))) {
                return InvalidData.invalidRoleID1();
            }
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID1();
            }
            List<User> list = adminDao.getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates(roleID,
                    vehicleScheduleID);
            if (list.isEmpty()) {
                logger.debug(NO_DATA_FOUND);
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            ArrayList<UserIDName> data = new ArrayList<UserIDName>();
            for (User user : list) {
                UserIDName object = new UserIDName();
                object.setUserID(user.getUserID());
                String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
                object.setUserName(name);
                data.add(object);
            }
            logger.debug(DATA_FOUND);
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, data);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> getConfigurableParameters() {
        try {
            LinkedHashMap<String, Integer> hm = configurableParameters.getAllConfigurableParameters();
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, hm);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> setTimeOfNotifyingStaffForBusNotVerified(int notifyingStaffForBusNotVerified) {
        try {
            configurableParameters.setNotifyingStaffForBusNotVerified(notifyingStaffForBusNotVerified);
            return ResponseHandler.generateResponse2(true, "Success!!!", HttpStatus.OK);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);

            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> setTimeBeforeWhichLeaveCanBeCanceled(int cancelLeaveBeforeTime) {
        try {
            configurableParameters.setCancelLeaveBeforeTime(cancelLeaveBeforeTime);
            return ResponseHandler.generateResponse2(true, "Success!!!", HttpStatus.OK);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> setBusDelayedTimeForNotifyingPassengers(int notifyingPassengersAboutBusDelayed) {
        try {
            configurableParameters.setNotifyingPassengersAboutBusDelayed(notifyingPassengersAboutBusDelayed);
            return ResponseHandler.generateResponse2(true, "Success!!!", HttpStatus.OK);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getNotifications(int userID, int roleID) {
        User user = adminDao.getUser(userID);
        if (user == null) {
            logger.debug("Invalid userID");
            return InvalidData.invalidUserID1();
        }
        List<Integer> userRoles = user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList());
        if (!userRoles.contains(roleID)) {
            logger.debug("Invalid roleID");
            return InvalidData.invalidRoleID1();
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<Notification> notifications = adminDao.getNotifications(userID, roleID);
        List<NotificationWithDateTime> list = new ArrayList<>();
        for (Notification obj : notifications) {
            list.add(new NotificationWithDateTime(obj.getId(), obj.getSubject(), obj.getContent(),
                    obj.getDateTime().format(dateTimeFormatter)));
        }
        if (list.isEmpty()) {
            logger.debug("Notifications data not found.");
            return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
        }
        logger.debug("Notifications data found.");
        return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);
    }

    public ResponseEntity<Object> deleteNotification(int id, int userID) {
        Notification notification = adminDao.getNotification(id);
        if ((notification == null) || (notification.getUser().getUserID() != userID)) {
            logger.debug("Invalid notification id.");
            return ResponseHandler.generateResponse2(false, "Invalid notification ID", HttpStatus.NOT_FOUND);
        }
        return adminDao.deleteNotification(id);
    }

    public ResponseEntity<Object> getAdminProfile(int userID) {

        try {
            User user = adminDao.getUser(userID);
            if (user == null) {
                logger.debug("Invalid UserID");
                return ResponseHandler.generateResponse1(false, "Invalid UserID", HttpStatus.NOT_FOUND, null);
            }
            List<Integer> userRoleIDs = user.getRoles().stream().map(role -> role.getRoleID())
                    .collect(Collectors.toList());
            if (!(userRoleIDs.contains(1))) {
                logger.debug("Invalid adminID");
                return ResponseHandler.generateResponse1(false, "Invalid adminID", HttpStatus.NOT_FOUND, null);
            }
            School school = passengerDao.getSchool();
            String name = user.getUserFirstName() + " " + user.getUserMiddleName() + " " + user.getUserLastName();
            AdminProfile admin = new AdminProfile();
            admin.setName(name);
            admin.setUserPhoneNumber(user.getUserPhoneNumber());
            admin.setUserAlternatePhoneNumber(user.getUserAlternatePhoneNumber());
            admin.setUserAddress(user.getUserAddress());
            admin.setUserPhoto(user.getUserPhoto());
            admin.setUserAge(user.getUserAge());
            admin.setEmail(user.getEmail());
            admin.setSchoolName(school.getSchoolName());
            logger.debug("Admin profile details fetched.");
            return ResponseHandler.generateResponse1(true, "Admin details fetched.", HttpStatus.OK, admin);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> addRouteAndItsStops(RouteStopsDetails routeStopsDetails) {

        try {
            Route obj = adminDao.getRoute(routeStopsDetails.getRouteID());
            if (obj != null) {
                logger.debug("Duplicate RouteID");
                String output = "Duplicate Route ID!  Route ID " + obj.getRouteID() + " having route name '"
                        + obj.getRouteName() + "' is present in database.";
                return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
            }
            if (routeStopsDetails.getStopsWithStopOrder().size() < 2) {
                logger.debug("Route must have atleast two stops");
                return ResponseHandler.generateResponse2(false, "Failed! Route must have atleast two stops.",
                        HttpStatus.OK);
            }

            Set<Integer> idealStopOrder = new HashSet<>();
            int stopsCount = routeStopsDetails.getStopsWithStopOrder().size();
            for (int i = 1; i <= stopsCount; i++) {
                idealStopOrder.add(i);
            }
            if (!((routeStopsDetails.getStopsWithStopOrder().stream().map(object -> object.getStopOrder())
                    .collect(Collectors.toList())).containsAll(idealStopOrder))) {
                logger.error("stop order is incorrect.");
                return ResponseHandler.generateResponse2(false, "Failed! Please check stop order.", HttpStatus.OK);
            }
            return adminDao.addRouteAndItsStops(routeStopsDetails);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Object> broadcastNotificationToPassengersOfVehicleSchedule(int vehicleScheduleID, int mins) {
        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID2();
            }
            List<String> tokens = adminDao.getTokensOfVehicleSchedule(vehicleScheduleID);
            if (tokens.isEmpty()) {
                logger.debug(
                        "Notification can't be sent. Users need to login to app at least once for receiving notification, so please ask users to login to app.");
                return ResponseHandler.generateResponse2(false,
                        "Notification can't be sent. Users need to login to app at least once for receiving notification, so please ask users to login to app.",
                        HttpStatus.OK);
            }
            Note note = new Note();
            note.setSubject("Trip Delay");
            int hrs = mins / 60;
            int modMin = mins % 60;
            String time = "";
            if (hrs != 0) {
                time = time + hrs + " hr ";
            }
            if (modMin != 0) {
                time = time + modMin + " mins";
            }
            note.setContent("Trip will be delayed by " + time + ".");
            // TODO : 1) is it to be async 2) what message to be sent if none any token is present
            String output = firebaseService.notifyMultiple2(note, tokens);
            logger.debug(output);
            return ResponseHandler.generateResponse2(true, output, HttpStatus.OK);
        } catch (Exception e) {

            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    public ResponseEntity<Object> addSchedule(Schedule schedule) {
        try {
            int typeOfJourney = schedule.getTypeOfJourney();
            int routeID = schedule.getRouteID();
            if ((typeOfJourney != 1) && (typeOfJourney != 2)) {
                return InvalidData.invalidTypeOfJourney1();
            }
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID1();
            }

            List<Integer> stopIDsOfRoute = adminDao.getStopIDOfRoute(routeID);
            List<Integer> stopIDsOfStopsSchedule = schedule.getStopSchedules().stream().map(obj -> obj.getStopID())
                    .collect(Collectors.toList());

            if (!(stopIDsOfStopsSchedule.containsAll(stopIDsOfRoute))) {
                logger.debug("Failed! Invalid stop IDs OR Some stop IDs are missing.");
                return ResponseHandler.generateResponse1(false,
                        "Failed! Invalid stop IDs OR Some stop IDs are missing.", HttpStatus.NOT_FOUND, null);
            }
            if ((stopIDsOfRoute.size()) != (stopIDsOfStopsSchedule.size())) {
                return InvalidData.invalidStopID1();
            }
            List<String> scheduleNames = adminDao.getVehicleScheduleNamesList();
            if (scheduleNames.contains(schedule.getVehicleScheduleName())) {
                logger.debug("Duplicate schedule name. Please try with another schedule name.");
                return ResponseHandler.generateResponse1(false,
                        "Duplicate schedule name. Please try with another schedule name.", HttpStatus.OK, null);
            }
            List<StopSchedule> stopSchedules = schedule.getStopSchedules();
            LocalTime tempTime = LocalTime.parse(stopSchedules.get(0).getScheduledArrivalTime());

            StopSchedule i = stopSchedules.get(0);
            if (!(tempTime.isAfter(LocalTime.parse(i.getScheduledDepartureTime())))) {
                tempTime = LocalTime.parse(i.getScheduledDepartureTime());
            } else {
                return ResponseHandler.generateResponse1(false, "Failed! Please select time appropriately.",
                        HttpStatus.OK, null);
            }
            for (int j = 1; j < stopSchedules.size(); j++) {
                i = stopSchedules.get(j);

                if (tempTime.isBefore(LocalTime.parse(i.getScheduledArrivalTime()))) {
                    tempTime = LocalTime.parse(i.getScheduledArrivalTime());
                } else {
                    return ResponseHandler.generateResponse1(false, "Failed! Please select time appropriately.",
                            HttpStatus.OK, null);
                }

                if (!(tempTime.isAfter(LocalTime.parse(i.getScheduledDepartureTime())))) {
                    tempTime = LocalTime.parse(i.getScheduledDepartureTime());
                } else {
                    return ResponseHandler.generateResponse1(false, "Failed! Please select time appropriately.",
                            HttpStatus.OK, null);
                }

            }
            StopSchedule startStopSchedule = null;
            if (!(stopIDsOfRoute.isEmpty())) {
                int startStopID;
                if (typeOfJourney == 1) {
                    startStopID = stopIDsOfRoute.get(0);
                } else {
                    startStopID = stopIDsOfRoute.get((stopIDsOfRoute.size()) - 1);
                }
                startStopSchedule = schedule.getStopSchedules().stream().filter(obj -> obj.getStopID() == startStopID)
                        .collect(Collectors.toList()).get(0);
            }
            return adminDao.addSchedule(schedule, startStopSchedule);
        } catch (Exception e) {

            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);

        }
    }

    public boolean saveTokensAndDeviceID(User user, String jwtToken, String fcmToken, String deviceID,
                                         boolean showPasswordSettingScreen) {
        if (!(user.getRoles().stream()
                .filter(i -> ((i.getRoleID() == DRIVER_ROLE_ID) || (i.getRoleID() == ATTENDANT_ROLE_ID)))
                .collect(Collectors.toList()).isEmpty())) {
            return saveTokensAndDeviceIDForDriverOrAttendant(user.getUserID(), jwtToken, fcmToken, deviceID,
                    showPasswordSettingScreen);
        } else if (!(user.getRoles().stream().filter(i -> (i.getRoleID() == PASSENGER_ROLE_ID))
                .collect(Collectors.toList()).isEmpty())) {
            return saveTokensAndDeviceIDForPassenger(user.getUserID(), jwtToken, fcmToken, deviceID,
                    showPasswordSettingScreen);
        } else if (!(user.getRoles().stream().filter(i -> (i.getRoleID() == ADMIN_ROLE_ID)).collect(Collectors.toList())
                .isEmpty())) {
            return saveTokensAndDeviceIDForAdmin(user.getUserID(), jwtToken, fcmToken, deviceID,
                    showPasswordSettingScreen);

        } else {
            return false;
        }

    }

    private boolean saveTokensAndDeviceIDForAdmin(Integer userID, String jwtToken, String fcmToken, String deviceID,
                                                  boolean showPasswordSettingScreen) {
        UserToken previousUserToken = driverDao.getUserToken(userID);
        boolean isSuccess = adminDao.saveTokensAndDeviceID(userID, jwtToken, fcmToken, deviceID,
                showPasswordSettingScreen);
        if (isSuccess) {
            if (previousUserToken != null) {
                firebaseService.unsubscribeFromAdminTopic(previousUserToken.getToken());

            }
            firebaseService.subscribeToAdminTopic(fcmToken);
            return true;
        } else {
            return false;
        }
    }

    private boolean saveTokensAndDeviceIDForPassenger(Integer userID, String jwtToken, String fcmToken, String deviceID,
                                                      boolean showPasswordSettingScreen) {
        List<PassengerToRouteID> passengerToRouteDetails = passengerDao.getPassengerToRouteDetails(userID);
        List<Integer> vehicleScheduleIDs = passengerToRouteDetails.stream()
                .filter(obj -> obj.getVehicleSchedule() != null)
                .map(obj -> obj.getVehicleSchedule().getVehicleScheduleID()).collect(Collectors.toList());
        if (!vehicleScheduleIDs.isEmpty()) {
            UserToken previousUserToken = driverDao.getUserToken(userID);
            boolean isSuccess = adminDao.saveTokensAndDeviceID(userID, jwtToken, fcmToken, deviceID,
                    showPasswordSettingScreen);
            if (isSuccess) {

                for (Integer vehicleScheduleID : vehicleScheduleIDs) {
                    if (previousUserToken != null) {
                        firebaseService.unsubscribeFromPassengerScheduleTopic(vehicleScheduleID,
                                previousUserToken.getToken());
                    }
                    firebaseService.subscribeToPassengerScheduleTopic(vehicleScheduleID, fcmToken);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return adminDao.saveTokensAndDeviceID(userID, jwtToken, fcmToken, deviceID, showPasswordSettingScreen);
        }

    }

    private boolean saveTokensAndDeviceIDForDriverOrAttendant(Integer userID, String jwtToken, String fcmToken,
                                                              String deviceID, boolean showPasswordSettingScreen) {
        List<TripDetails> ongoingTripsOfStaff = driverDao.getTripsGoingOnForStaffAndNotVerifiedByThatStaff(userID);
        if (!(ongoingTripsOfStaff.isEmpty())) {
            UserToken previousUserToken = driverDao.getUserToken(userID);
            boolean isSaved = adminDao.saveTokensAndDeviceID(userID, jwtToken, fcmToken, deviceID,
                    showPasswordSettingScreen);
            if (isSaved) {
                if (previousUserToken != null) {
                    firebaseService.unsubscribeFromScheduleTopic(
                            ongoingTripsOfStaff.get(0).getVehicleSchedule().getVehicleScheduleID(),
                            previousUserToken.getToken());
                }
                firebaseService.subscribeToScheduleTopic(
                        ongoingTripsOfStaff.get(0).getVehicleSchedule().getVehicleScheduleID(), fcmToken);
                return true;
            } else {
                return false;
            }
        } else {
            return adminDao.saveTokensAndDeviceID(userID, jwtToken, fcmToken, deviceID, showPasswordSettingScreen);
        }

    }

    public ResponseEntity<Object> updateRouteAndItsStops(int routeID, RouteStops updatedRouteStops) {
        try {
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID2();
            }
            if (updatedRouteStops.getStopsWithStopOrderDetails().size() < 2) {
                logger.debug("Route must have atleast two stops.");
                return ResponseHandler.generateResponse2(false, "Failed! Route must have atleast two stops.",
                        HttpStatus.OK);
            }
            List<RouteStop> stopsWithStopOrderofRoute = adminDao.getStopsWithStopOrderofRoute(routeID);
            HashMap<Integer, Stop> existingStopIDsMap = new HashMap<>();
            HashMap<Integer, Integer> existingStopIDOrderMap = new HashMap<>();
            stopsWithStopOrderofRoute.forEach(obj -> {
                existingStopIDsMap.put(obj.getStop().getStopID(), obj.getStop());
                existingStopIDOrderMap.put(obj.getStop().getStopID(), obj.getStopOrder());
            });

            List<Integer> stopIDsMentionedInUpdatedJSONofRouteStops = updatedRouteStops.getStopsWithStopOrderDetails()
                    .stream().filter(obj -> obj.getStopID() != null).map(obj -> obj.getStopID())
                    .collect(Collectors.toList());
            if (!existingStopIDsMap.keySet().containsAll(stopIDsMentionedInUpdatedJSONofRouteStops)) {
                return InvalidData.invalidStopID2();
            }
            HashSet<Integer> stopIDsMentionedInUpdatedJSONofRouteStopsSet = new HashSet<>();
            stopIDsMentionedInUpdatedJSONofRouteStopsSet.addAll(stopIDsMentionedInUpdatedJSONofRouteStops);
            if (stopIDsMentionedInUpdatedJSONofRouteStopsSet.size() != stopIDsMentionedInUpdatedJSONofRouteStops
                    .size()) {
                logger.error("Duplicate StopID");
                return ResponseHandler.generateResponse2(false, "Duplicate StopID", HttpStatus.OK);
            }

            Set<Integer> idealStopOrder = new HashSet<>();
            int updateStopsCount = updatedRouteStops.getStopsWithStopOrderDetails().size();
            for (int i = 1; i <= updateStopsCount; i++) {
                idealStopOrder.add(i);
            }
            if (!((updatedRouteStops.getStopsWithStopOrderDetails().stream().map(obj -> obj.getStopOrder())
                    .collect(Collectors.toList())).containsAll(idealStopOrder))) {
                logger.error("stop order is incorrect.");
                return ResponseHandler.generateResponse2(false, "Failed! Please check stop order.", HttpStatus.OK);
            }

            List<Integer> tobeDeletedStopIDs = new ArrayList<>();
            tobeDeletedStopIDs.addAll(existingStopIDsMap.keySet());
            tobeDeletedStopIDs.removeAll(stopIDsMentionedInUpdatedJSONofRouteStops);
            if (!tobeDeletedStopIDs.isEmpty()) {
                List<PassengerToRouteID> passengerAssignedToStop = adminDao
                        .passengerAssignedToStops(tobeDeletedStopIDs);
                Set<Integer> stopIDs = new HashSet<>();
                passengerAssignedToStop.forEach(obj -> {
                    stopIDs.add(obj.getPickupPointStop().getStopID());
                    stopIDs.add(obj.getDropPointStop().getStopID());
                });
                stopIDs.retainAll(tobeDeletedStopIDs);
                List<String> stopNamesCannotBeDeleted = new ArrayList<>();
                for (Integer id : stopIDs) {
                    stopNamesCannotBeDeleted.add(existingStopIDsMap.get(id).getStopName());
                }
                if (!stopNamesCannotBeDeleted.isEmpty()) {
                    logger.debug("stops cannot be deleted as students are assigned to those stops");
                    return ResponseHandler.generateResponse2(false,
                            "Failed! Following stops cannot be deleted as students are assigned to those stops:"
                                    + stopNamesCannotBeDeleted,
                            HttpStatus.OK);
                }
            }

            List<StopOrderDetails> newStopsToBeAdded = updatedRouteStops.getStopsWithStopOrderDetails().stream()
                    .filter(obj -> obj.getStopID() == null).collect(Collectors.toList());
            List<StopOrderDetails> stopsInUpdatedJSONhavingStopIDs = updatedRouteStops.getStopsWithStopOrderDetails()
                    .stream().filter(obj -> obj.getStopID() != null).collect(Collectors.toList());
            List<Object[]> stopIDStopOrderToBeUpdated = new ArrayList<>();
            List<StopOrderDetails> stopDetailsToBeUpdated = new ArrayList<>();
            for (StopOrderDetails obj : stopsInUpdatedJSONhavingStopIDs) {
                Stop existingStopHavingID = existingStopIDsMap.get(obj.getStopID());
                String existing = existingStopHavingID.getStopName() + existingStopHavingID.getStopAddress()
                        + existingStopHavingID.getStopLatitude() + existingStopHavingID.getStopLongitude();
                String updated = obj.getStopName() + obj.getStopAddress() + obj.getStopLatitude()
                        + obj.getStopLongitude();
                if (!existing.equals(updated)) {
                    stopDetailsToBeUpdated.add(obj);
                }
                if (obj.getStopOrder() != existingStopIDOrderMap.get(obj.getStopID())) {
                    Object[] arr = new Object[]{obj.getStopID(), obj.getStopOrder()};
                    stopIDStopOrderToBeUpdated.add(arr);
                }
            }

            RouteStopUpdate routeStopUpdate = new RouteStopUpdate();
            if (!(route.getRouteName().equals(updatedRouteStops.getRouteName()))) {
                routeStopUpdate.setRouteName(updatedRouteStops.getRouteName());
            }
            routeStopUpdate.setTobeDeletedStopIDs(tobeDeletedStopIDs);
            routeStopUpdate.setNewStopsToBeAdded(newStopsToBeAdded);
            routeStopUpdate.setStopDetailsToBeUpdated(stopDetailsToBeUpdated);
            routeStopUpdate.setStopIDStopOrderToBeUpdated(stopIDStopOrderToBeUpdated);
            routeStopUpdate.setScheduleNamesToBeUpdated(
                    getScheduleNamesToBeUpdated(routeID, updatedRouteStops, stopsWithStopOrderofRoute));
            return adminDao.updateRouteAndItsStops(routeID, routeStopUpdate);
        } catch (Exception e) {

            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    private HashMap<Integer, String> getScheduleNamesToBeUpdated(int routeID, RouteStops updatedRouteStops,
                                                                 List<RouteStop> stopsWithStopOrderofRoute) {
        StopOrderDetails firstStopToBeUpdated = updatedRouteStops.getStopsWithStopOrderDetails().stream()
                .min(Comparator.comparing(StopOrderDetails::getStopOrder)).get();
        StopOrderDetails lastStopToBeUpdated = updatedRouteStops.getStopsWithStopOrderDetails().stream()
                .max(Comparator.comparing(StopOrderDetails::getStopOrder)).get();
        Stop currentFirstStop = stopsWithStopOrderofRoute.get(0).getStop();
        Stop currentLastStop = stopsWithStopOrderofRoute.get(stopsWithStopOrderofRoute.size() - 1).getStop();
        HashMap<Integer, String> scheduleNamesToBeUpdated = new HashMap<>();

        if ((!firstStopToBeUpdated.getStopName().equals(currentFirstStop.getStopName()))
                || (!lastStopToBeUpdated.getStopName().equals(currentLastStop.getStopName()))) {
            List<VehicleSchedule> schedulesHavingRoute = adminDao.getShedulesOfRoute(routeID);
            String onwardScheduleName = firstStopToBeUpdated.getStopName() + "_" + lastStopToBeUpdated.getStopName()
                    + "@";
            String returnScheduleName = lastStopToBeUpdated.getStopName() + "_" + firstStopToBeUpdated.getStopName()
                    + "@";
            for (VehicleSchedule schedule : schedulesHavingRoute) {
                String name;
                if (schedule.getTypeOfJourney() == 1) {
                    name = onwardScheduleName + schedule.getScheduledDepartureTime();
                } else {
                    name = returnScheduleName + schedule.getScheduledDepartureTime();
                }
                int i = 2;
                String temp = name;
                while (scheduleNamesToBeUpdated.values().contains(name)) {
                    name = temp + "_" + i;
                    i++;
                }
                scheduleNamesToBeUpdated.put(schedule.getVehicleScheduleID(), name);
            }
        }
        return scheduleNamesToBeUpdated;
    }

    public ResponseEntity<Object> getRouteListOnRouteScreen() {
        try {
            List<Object[]> routeDetailsAndStopsCountOfAllRoutes = adminDao.getRouteDetailsAndStopsCountOfAllRoutes();
            List<Object[]> firstStopNameOfAllRoutes = adminDao.getFirstStopNameOfAllRoutes();
            LinkedHashMap<Integer, String> firstStopNameOfAllRoutesMap = new LinkedHashMap<>();
            firstStopNameOfAllRoutes
                    .forEach(obj -> firstStopNameOfAllRoutesMap.put((Integer) (obj[0]), (String) (obj[1])));
            List<RouteDetails> list = new ArrayList<>();
            for (Object[] obj : routeDetailsAndStopsCountOfAllRoutes) {
                RouteDetails routeDetails = new RouteDetails();
                routeDetails.setRouteID(((Route) (obj[0])).getRouteID());
                routeDetails.setRouteName(((Route) (obj[0])).getRouteName());
                routeDetails.setStartLocation(firstStopNameOfAllRoutesMap.get(((Route) (obj[0])).getRouteID()));
                routeDetails.setStopsCount((Long) (obj[1]));
                list.add(routeDetails);
            }
            logger.debug("All Route details are fetched.");
            if (list.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            } else {
                return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> updateScheduledTimeAtStopsOfSchedule(int scheduleID, ScheduleUpdate scheduleUpdate) {
        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(scheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID2();
            }
            List<StopSchedule> updatedStopSchedules = scheduleUpdate.getUpdatedStopSchedules();
            List<Integer> stopIDsInUpdatedStopSchedules = updatedStopSchedules.stream().map(obj -> obj.getStopID())
                    .collect(Collectors.toList());
            List<Integer> stopIDsOfRoute = adminDao.getStopIDOfRoute(vehicleSchedule.getRoute().getRouteID());
            if (!((stopIDsInUpdatedStopSchedules.containsAll(stopIDsOfRoute))
                    && (stopIDsInUpdatedStopSchedules.size() == stopIDsOfRoute.size()))) {
                logger.error("Invalid stopIDs or some stops are missing.");
                return ResponseHandler.generateResponse2(false, "Invalid stopIDs or some stops are missing.",
                        HttpStatus.NOT_FOUND);
            }
            LocalTime tempTime = LocalTime.parse(updatedStopSchedules.get(0).getScheduledArrivalTime());
            StopSchedule i = updatedStopSchedules.get(0);
            if (!(tempTime.isAfter(LocalTime.parse(i.getScheduledDepartureTime())))) {
                tempTime = LocalTime.parse(i.getScheduledDepartureTime());
            } else {
                return ResponseHandler.generateResponse2(false, "Failed! Please select time appropriately.",
                        HttpStatus.OK);
            }
            for (int j = 1; j < updatedStopSchedules.size(); j++) {
                i = updatedStopSchedules.get(j);
                if (tempTime.isBefore(LocalTime.parse(i.getScheduledArrivalTime()))) {
                    tempTime = LocalTime.parse(i.getScheduledArrivalTime());
                } else {
                    return ResponseHandler.generateResponse2(false, "Failed! Please select time appropriately.",
                            HttpStatus.OK);
                }
                if (!(tempTime.isAfter(LocalTime.parse(i.getScheduledDepartureTime())))) {
                    tempTime = LocalTime.parse(i.getScheduledDepartureTime());
                } else {
                    return ResponseHandler.generateResponse2(false, "Failed! Please select time appropriately.",
                            HttpStatus.OK);
                }

            }
            tempTime = null;

            ////////////////////////////////////
            int firstStopID;
            if (vehicleSchedule.getTypeOfJourney() == 1) {
                firstStopID = stopIDsOfRoute.get(0);
            } else {
                firstStopID = stopIDsOfRoute.get(stopIDsOfRoute.size() - 1);
            }

            List<RouteStopSchedule> existingRouteStopScheduleOfGivenSchedule = adminDao
                    .getRouteStopSchedule(scheduleID);
            LinkedHashMap<Integer, RouteStopSchedule> existingRouteStopScheduleOfGivenScheduleMap = new LinkedHashMap<>();
            existingRouteStopScheduleOfGivenSchedule
                    .forEach(obj -> existingRouteStopScheduleOfGivenScheduleMap.put(obj.getStop().getStopID(), obj));
            List<StopSchedule> stopSchedulesToBeUpdated = new ArrayList<>();
            List<StopSchedule> stopSchedulesToBeInserted = new ArrayList<>();
            for (StopSchedule obj : updatedStopSchedules) {
                if (existingRouteStopScheduleOfGivenScheduleMap.containsKey(obj.getStopID())) {
                    RouteStopSchedule existingRouteStopSchedule = existingRouteStopScheduleOfGivenScheduleMap
                            .get(obj.getStopID());
                    if (!(((existingRouteStopSchedule.getScheduledArrivalTime()
                            .compareTo(LocalTime.parse(obj.getScheduledArrivalTime()))) == 0)
                            && ((existingRouteStopSchedule.getScheduledDepartureTime()
                            .compareTo(LocalTime.parse(obj.getScheduledDepartureTime()))) == 0))) {
                        stopSchedulesToBeUpdated.add(obj);
                    }
                } else {
                    stopSchedulesToBeInserted.add(obj);
                }
            }

            if ((stopSchedulesToBeUpdated.stream().map(obj -> obj.getStopID()).collect(Collectors.toList()))
                    .contains(firstStopID)) {
                StopSchedule updatedStopScheduleAtFirstStop = updatedStopSchedules.stream()
                        .filter(obj -> obj.getStopID() == firstStopID).collect(Collectors.toList()).get(0);
                if (vehicleSchedule.getBlockingTimeInMinutes() == scheduleUpdate.getBlockingTimeInMinutes()) {
                    return adminDao.updateScheduledTimeAtStopsOfSchedule(scheduleID, stopSchedulesToBeUpdated,
                            stopSchedulesToBeInserted, updatedStopScheduleAtFirstStop, scheduleUpdate.getScheduleName(),
                            null);
                } else {
                    return adminDao.updateScheduledTimeAtStopsOfSchedule(scheduleID, stopSchedulesToBeUpdated,
                            stopSchedulesToBeInserted, updatedStopScheduleAtFirstStop, scheduleUpdate.getScheduleName(),
                            scheduleUpdate.getBlockingTimeInMinutes());
                }

            } else {
                if (vehicleSchedule.getBlockingTimeInMinutes() == scheduleUpdate.getBlockingTimeInMinutes()) {
                    return adminDao.updateScheduledTimeAtStopsOfSchedule(scheduleID, stopSchedulesToBeUpdated,
                            stopSchedulesToBeInserted, null, null, null);
                } else {
                    return adminDao.updateScheduledTimeAtStopsOfSchedule(scheduleID, stopSchedulesToBeUpdated,
                            stopSchedulesToBeInserted, null, null, scheduleUpdate.getBlockingTimeInMinutes());
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    public ResponseEntity<Object> getRouteDetails(int routeID) {

        try {
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID1();
            }
            List<RouteStop> stopsListWithOrder = adminDao.getStopsWithStopOrderofRoute(routeID);
            logger.debug("Stops of Route are fetched.");
            RouteStopsData routeStopsData = new RouteStopsData();
            routeStopsData.setRouteName(route.getRouteName());
            List<StopDataWithOrder> list = new ArrayList<>();
            stopsListWithOrder.forEach(obj -> list.add(new StopDataWithOrder(obj.getStop(), obj.getStopOrder())));
            routeStopsData.setStopsListWithOrder(list);
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, routeStopsData);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public ResponseEntity<Object> getScheduleDetails(int vehicleScheduleID) {

        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID1();
            }
            List<Object[]> list = adminDao.getRouteStopScheduleDataAsPerStopsOrderOfSchedule(vehicleSchedule);
            List<StopSchedule> stopSchedules = new ArrayList<>();
            DateTimeFormatter timeColonFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            list.forEach(obj -> stopSchedules.add(new StopSchedule((int) obj[0],
                    (((LocalTime) obj[1]) != null) ? (timeColonFormatter.format((LocalTime) obj[1])) : "",
                    (((LocalTime) obj[2]) != null) ? (timeColonFormatter.format((LocalTime) obj[2])) : "")));
            ScheduleDetailsWithScheduledTimeAtStopsData scheduleDetailsWithScheduledTimeAtStopsData = new ScheduleDetailsWithScheduledTimeAtStopsData();
            scheduleDetailsWithScheduledTimeAtStopsData
                    .setVehicleScheduleName(vehicleSchedule.getVehicleScheduleName());
            scheduleDetailsWithScheduledTimeAtStopsData.setRouteName(vehicleSchedule.getRoute().getRouteName());
            scheduleDetailsWithScheduledTimeAtStopsData.setTypeOfJourney(vehicleSchedule.getTypeOfJourney());
            scheduleDetailsWithScheduledTimeAtStopsData.setStopSchedules(stopSchedules);
            scheduleDetailsWithScheduledTimeAtStopsData.setBlockingTime(vehicleSchedule.getBlockingTimeInMinutes());
            logger.debug("Schedule details found.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
                    scheduleDetailsWithScheduledTimeAtStopsData);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    private LinkedHashMap<Integer, User> getAllUsersMapOfGivenRole(int roleID) {
        LinkedHashMap<Integer, User> map = new LinkedHashMap<>();
        List<User> users = adminDao.getUserListOfGivenRole(roleID);
        users.forEach(obj -> map.put(obj.getUserID(), obj));
        return map;
    }

    private LinkedHashMap<Integer, VehicleDetails> getAllVehiclesMap() {
        LinkedHashMap<Integer, VehicleDetails> map = new LinkedHashMap<>();
        List<VehicleDetails> allVehicles = adminDao.getAllVehicles();
        allVehicles.forEach(obj -> map.put(obj.getVehicleID(), obj));
        return map;
    }

    public ResponseEntity<Object> assignNewResources(int scheduleID, List<ResourcesAssignmentEntry> entries,
                                                     LocalDate start, LocalDate end) {
        try {
            List<LocalDate> considerDates = start.datesUntil(end.plusDays(1)).toList();
            VehicleSchedule schedule = adminDao.getVehicleSchedule(scheduleID);
            LinkedHashMap<Integer, User> allDriversMap = getAllUsersMapOfGivenRole(DRIVER_ROLE_ID);
            LinkedHashMap<Integer, User> allAttendantsMap = getAllUsersMapOfGivenRole(ATTENDANT_ROLE_ID);
            LinkedHashMap<Integer, VehicleDetails> allVehiclesMap = getAllVehiclesMap();
            List<StaffToVehicleScheduleMultiStaff> staffToVehicleScheduleMultiStaffListToBeSaved = new ArrayList<>();
            List<VehicleToScheduleAssignment> vehicleToScheduleAssignmentListToBeSaved = new ArrayList<>();
            Role driverRole = adminDao.getRole(DRIVER_ROLE_ID);
            Role attendantRole = adminDao.getRole(ATTENDANT_ROLE_ID);
            for (ResourcesAssignmentEntry entry : entries) {
                List<Integer> driverIDs = entry.getDriverIDs();
                List<Integer> attendantIDs = entry.getAttendantIDs();
                List<LocalDate> dates = entry.getStartDate().datesUntil(entry.getEndDate().plusDays(1))
                        .collect(Collectors.toList());
                dates.retainAll(considerDates);
                for (LocalDate date : dates) {
                    vehicleToScheduleAssignmentListToBeSaved.add(new VehicleToScheduleAssignment(
                            new ScheduleDateID(schedule, date), allVehiclesMap.get(entry.getVehicleID())));
                    for (Integer driverID : driverIDs) {
                        staffToVehicleScheduleMultiStaffListToBeSaved.add(new StaffToVehicleScheduleMultiStaff(
                                new VehicleScheduleDateStaffID(schedule, allDriversMap.get(driverID), date),
                                driverRole));
                    }
                    for (Integer attendantID : attendantIDs) {
                        staffToVehicleScheduleMultiStaffListToBeSaved.add(new StaffToVehicleScheduleMultiStaff(
                                new VehicleScheduleDateStaffID(schedule, allAttendantsMap.get(attendantID), date),
                                attendantRole));
                    }
                }
            }
            return adminDao.assignNewResourcesToSchedule(staffToVehicleScheduleMultiStaffListToBeSaved,
                    vehicleToScheduleAssignmentListToBeSaved);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Object> getStopsOrderForGivenRouteAndTypeOfJourney(int routeID, int typeOfJourney) {
        try {
            Route route = adminDao.getRoute(routeID);
            if (route == null) {
                return InvalidData.invalidRouteID1();
            }
            if (!((typeOfJourney == ONWARD_JOURNEY) || (typeOfJourney == RETURN_JOURNEY))) {
                return InvalidData.invalidTypeOfJourney1();
            }
            List<RouteStop> list = adminDao.getStopsOrderForGivenRouteAndTypeOfJourney(routeID, typeOfJourney);
            List<StopIdNameOrder> stopIdNameOrderListOfRouteBasedOnTypeOfJourney = new ArrayList<>();
            list.forEach(obj -> stopIdNameOrderListOfRouteBasedOnTypeOfJourney.add(
                    new StopIdNameOrder(obj.getStop().getStopID(), obj.getStop().getStopName(), obj.getStopOrder())));
            logger.debug("data found");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK,
                    stopIdNameOrderListOfRouteBasedOnTypeOfJourney);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    private String getAutogeneratedPassword() {
        return "999999";
        // TODO : commented for now, as email part is not done.. will uncomment , once
        // emial part is ready
        // return RandomStringUtils.randomAlphanumeric(6);
    }

    public ResponseEntity<Object> generateTemporaryPassword(String userUniqueKey) {
        try {
            User user = adminDao.getUserHavingGivenUniqueKey(userUniqueKey);
            if (user == null) {
                logger.debug("Wrong User unique key!");
                return ResponseHandler.generateResponse2(false, "Wrong User unique key!", HttpStatus.NOT_FOUND);
            }
            boolean success = adminDao.setTemporaryPassword(user.getUserID(), encryptionUtil.encrypt(getAutogeneratedPassword()));
            if (success) {
                logger.debug("Success!");
                return ResponseHandler.generateResponse2(true, "Success!", HttpStatus.OK);
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

    public Object tryQuery() {
        return adminDao.tryQuery();
    }

    public LinkedHashMap<Integer, StartEndTimeConsideringBlockingTimeOfSchedule> getStartEndTimeConsideringBlockingTimeOfGivenSchedules(
            List<ArrivalTimeAtStopOfSchedule> list) {
        LinkedHashMap<Integer, StartEndTimeConsideringBlockingTimeOfSchedule> map = new LinkedHashMap<>();
        StartEndTimeConsideringBlockingTimeOfSchedule scheduleA = new StartEndTimeConsideringBlockingTimeOfSchedule();
        scheduleA.setScheduleName(list.get(0).getScheduleName());
        if (list.get(0).getTypeOfJourney() == ONWARD_JOURNEY) {
            scheduleA.setStartTime(list.get(0).getScheduledArrivalTime());
        } else {
            scheduleA.setEndTime(
                    list.get(0).getScheduledArrivalTime().plusMinutes(list.get(0).getBlockingTimeInMinutes()));
        }
        for (int i = 1; i <= list.size() - 2; i++) {
            ArrivalTimeAtStopOfSchedule obj1 = list.get(i);
            ArrivalTimeAtStopOfSchedule obj2 = list.get(i + 1);
            if (obj1.getScheduleID() != obj2.getScheduleID()) {

                if (obj1.getTypeOfJourney() == ONWARD_JOURNEY) {
                    scheduleA.setEndTime(obj1.getScheduledArrivalTime().plusMinutes(obj1.getBlockingTimeInMinutes()));
                } else {
                    scheduleA.setStartTime(obj1.getScheduledArrivalTime());
                }
                map.put(obj1.getScheduleID(), scheduleA);

                scheduleA = null;

                scheduleA = new StartEndTimeConsideringBlockingTimeOfSchedule();
                scheduleA.setScheduleName(obj2.getScheduleName());
                if (obj2.getTypeOfJourney() == ONWARD_JOURNEY) {
                    scheduleA.setStartTime(obj2.getScheduledArrivalTime());
                } else {
                    scheduleA.setEndTime(obj2.getScheduledArrivalTime().plusMinutes(obj2.getBlockingTimeInMinutes()));
                }
            }
        }
        ArrivalTimeAtStopOfSchedule obj = list.get(list.size() - 1);
        if (obj.getTypeOfJourney() == ONWARD_JOURNEY) {
            scheduleA.setEndTime(obj.getScheduledArrivalTime().plusMinutes(obj.getBlockingTimeInMinutes()));
        } else {
            scheduleA.setStartTime(obj.getScheduledArrivalTime());
        }
        map.put(obj.getScheduleID(), scheduleA);

        return map;
    }

    public List<Integer> getScheduleIDsFromGivenScheduleIDsWhichWillBeGoingOnDuringGivenSchedule(int scheduleID,
                                                                                                 LinkedHashMap<Integer, StartEndTimeConsideringBlockingTimeOfSchedule> map) {
        LocalTime startTimeOfDesiredSchedule = map.get(scheduleID).getStartTime();
        LocalTime endTimeOfDesiredSchedule = map.get(scheduleID).getEndTime();
        List<Integer> scheduleIDsGoingOnDuringGivenSchedule = new ArrayList<>();
        Set<Integer> keys = map.keySet();
        for (Integer key : keys) {
            LocalTime start = map.get(key).getStartTime();
            LocalTime end = map.get(key).getEndTime();
            int desiredStartWithStart = startTimeOfDesiredSchedule.compareTo(start);
            int desiredStartWithEnd = startTimeOfDesiredSchedule.compareTo(end);
            int desiredEndWithStart = endTimeOfDesiredSchedule.compareTo(start);
            int desiredEndWithEnd = endTimeOfDesiredSchedule.compareTo(end);
            if (((desiredStartWithStart <= 0) && (desiredEndWithStart > 0))
                    || ((desiredStartWithEnd < 0) && (desiredEndWithEnd >= 0))
                    || ((desiredStartWithStart >= 0) && (desiredEndWithEnd <= 0))) {
                scheduleIDsGoingOnDuringGivenSchedule.add(key);
            }
        }
        return scheduleIDsGoingOnDuringGivenSchedule;
    }

    LinkedHashMap<Integer, String> getUserNamesOfGivenRole(int roleID) {
        List<User> userslistOfGivenRole = adminDao.getUserListOfGivenRole(roleID);
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        userslistOfGivenRole.forEach(obj -> map.put(obj.getUserID(),
                obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName()));
        return map;
    }

    public ResponseEntity<Object> getScheduleList(int month, int year) {
        try {
            if (!ValueRange.of(1, 12).isValidValue(month)) {
                logger.error("Invalid month value");
                return ResponseHandler.generateResponse1(false, "Invalid month value", HttpStatus.BAD_REQUEST, null);
            }
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(1).minusDays(1);

            List<Object[]> list = adminDao.getStaffVehicleDetailsOfShchedulesForGivenDateInterval(startDate, endDate);
            if (list.isEmpty()) {
                logger.debug("data not found.");
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }

            List<Object[]> timeDataOfStops = adminDao.getArrivalTimeAtAllStopsOfSchedulesInGivenDateRange(startDate,
                    endDate);
            Set<Integer> scheduleIDsHavingSomeStopsTimeMissing = new HashSet<>();
            scheduleIDsHavingSomeStopsTimeMissing.addAll(
                    timeDataOfStops.stream().filter(obj -> obj[5] == null).map(obj -> (Integer) obj[0]).toList());

            List<ScheduleDateStaffsVehicle> scheduleDateStaffsVehicleList = arrangeDataInFormOfScheduleDateStaffsVehicle(
                    list);
            List<ScheduleResourcesDataOfMonth> output = getListOfScheduleResourcesDataOfMonth(startDate, endDate,
                    scheduleDateStaffsVehicleList, scheduleIDsHavingSomeStopsTimeMissing);
            logger.debug("data found.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, output);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);

        }
    }

    // coding checked
    private List<ScheduleDateStaffsVehicle> arrangeDataInFormOfScheduleDateStaffsVehicle(List<Object[]> list) {
        List<ScheduleDateStaffsVehicle> output = new ArrayList<>();
        ScheduleDateStaffsVehicle scheduleDateStaffsVehicle = getObjectOfScheduleDateStaffsVehicleForOnlyOneGivenStaff(
                list.get(0));
        for (int i = 1; i <= (list.size() - 1); i++) {
            VehicleScheduleDateStaffID vehicleScheduleDateStaffID = (VehicleScheduleDateStaffID) (list.get(i)[0]);
            if ((vehicleScheduleDateStaffID.getVehicleSchedule().getVehicleScheduleID() == scheduleDateStaffsVehicle
                    .getScheduleID())
                    && (vehicleScheduleDateStaffID.getDate().isEqual(scheduleDateStaffsVehicle.getDate()))) {
                if (((int) (list.get(i)[1])) == DRIVER_ROLE_ID) {
                    List<User> drivers = scheduleDateStaffsVehicle.getDrivers();
                    drivers.add(vehicleScheduleDateStaffID.getStaff());
                    scheduleDateStaffsVehicle.setDrivers(drivers);
                } else {
                    List<User> attendants = scheduleDateStaffsVehicle.getAttendants();
                    attendants.add(vehicleScheduleDateStaffID.getStaff());
                    scheduleDateStaffsVehicle.setAttendants(attendants);
                }
            } else {
                output.add(scheduleDateStaffsVehicle);
                scheduleDateStaffsVehicle = getObjectOfScheduleDateStaffsVehicleForOnlyOneGivenStaff(list.get(i));
            }
        }
        output.add(scheduleDateStaffsVehicle);
        return output;
    }

    // coding checked.
    private ScheduleDateStaffsVehicle getObjectOfScheduleDateStaffsVehicleForOnlyOneGivenStaff(Object[] obj) {
        ScheduleDateStaffsVehicle scheduleDateStaffsVehicle = new ScheduleDateStaffsVehicle();
        VehicleScheduleDateStaffID vehicleScheduleDateStaffID = (VehicleScheduleDateStaffID) obj[0];
        int staffTypeID = (int) obj[1];
        VehicleDetails vehicleDetails = (VehicleDetails) obj[2];
        scheduleDateStaffsVehicle.setScheduleID(vehicleScheduleDateStaffID.getVehicleSchedule().getVehicleScheduleID());
        scheduleDateStaffsVehicle
                .setScheduleName(vehicleScheduleDateStaffID.getVehicleSchedule().getVehicleScheduleName());
        scheduleDateStaffsVehicle
                .setRouteName(vehicleScheduleDateStaffID.getVehicleSchedule().getRoute().getRouteName());
        scheduleDateStaffsVehicle.setDate(vehicleScheduleDateStaffID.getDate());
        List<User> drivers = new ArrayList<>();
        List<User> attendants = new ArrayList<>();
        if (staffTypeID == DRIVER_ROLE_ID) {
            drivers.add(vehicleScheduleDateStaffID.getStaff());
        } else {
            attendants.add(vehicleScheduleDateStaffID.getStaff());
        }
        scheduleDateStaffsVehicle.setDrivers(drivers);
        scheduleDateStaffsVehicle.setAttendants(attendants);
        scheduleDateStaffsVehicle.setVehicleDetails(vehicleDetails);
        return scheduleDateStaffsVehicle;
    }

    private List<ScheduleResourcesDataOfMonth> getListOfScheduleResourcesDataOfMonth(LocalDate startDate,
                                                                                     LocalDate endDate, List<ScheduleDateStaffsVehicle> list,
                                                                                     Set<Integer> scheduleIDsHavingSomeStopsTimeMissing) {

        List<ScheduleResourcesDataOfMonth> output = new ArrayList<>();

        HashSet<Integer> scheduleIDs = new HashSet<>();
        scheduleIDs.addAll(list.stream().map(i -> i.getScheduleID()).toList());

        List<TripToStaff> tripStaffData = adminDao
                .getTripStaffDataCorrespondingToGivenSchedulesForGivenDateOrderedByTripID(scheduleIDs, LocalDate.now());

        LinkedHashMap<Integer, List<TripToStaff>> scheduleIDTripStaffDataMap = getDataInFormOfScheduleIDTripStaffDataMap(
                tripStaffData);

        ScheduleResourcesDataOfMonth scheduleResourcesDataOfMonthObject = getDataInFormOfScheduleResourcesDataOfMonth(
                list.get(0), scheduleIDsHavingSomeStopsTimeMissing);
        boolean isMissingDates = false;
        List<DateRangeDataInScheduleList> dateRangesListInScheduleResourcesDataOfMonthObject = new ArrayList<>();

        DateRangeDataInScheduleList obj = getObjectOfDateRangeDataInScheduleListForOnlyOneDateOfSchedule(list.get(0));

        if (!obj.getStartDate().isEqual(startDate)) {
            isMissingDates = true;

            dateRangesListInScheduleResourcesDataOfMonthObject
                    .addAll(getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(startDate,
                            obj.getStartDate().minusDays(1)));
        }
        for (int i = 1; i <= (list.size() - 1); i++) {
            ScheduleDateStaffsVehicle loopObj = list.get(i);
            if (scheduleResourcesDataOfMonthObject.getScheduleID() == loopObj.getScheduleID()) {

                if ((obj.getEndDate().plusDays(1).isEqual(loopObj.getDate()))
                        && isDriversAttendantsVehicleEqual(obj, loopObj)) {
                    obj.setEndDate(loopObj.getDate());
                } else {

                    dateRangesListInScheduleResourcesDataOfMonthObject
                            .addAll(getListOfDateRangeDataInScheduleListBySettingEnableDisableStatus(obj,
                                    scheduleIDTripStaffDataMap
                                            .get(scheduleResourcesDataOfMonthObject.getScheduleID())));
                    if (!obj.getEndDate().plusDays(1).isEqual(loopObj.getDate())) {
                        isMissingDates = true;
                        dateRangesListInScheduleResourcesDataOfMonthObject
                                .addAll(getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(
                                        obj.getEndDate().plusDays(1), loopObj.getDate().minusDays(1)));
                    }
                    obj = getObjectOfDateRangeDataInScheduleListForOnlyOneDateOfSchedule(list.get(i));
                }
            } else {
                dateRangesListInScheduleResourcesDataOfMonthObject
                        .addAll(getListOfDateRangeDataInScheduleListBySettingEnableDisableStatus(obj,
                                scheduleIDTripStaffDataMap.get(scheduleResourcesDataOfMonthObject.getScheduleID())));
                if (!obj.getEndDate().isEqual(endDate)) {
                    isMissingDates = true;
                    dateRangesListInScheduleResourcesDataOfMonthObject.addAll(
                            getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(obj.getEndDate().plusDays(1),
                                    endDate));
                }
                scheduleResourcesDataOfMonthObject.setShowMissingDatesStatus(isMissingDates);
                scheduleResourcesDataOfMonthObject
                        .setDateRangesList(dateRangesListInScheduleResourcesDataOfMonthObject);
                output.add(scheduleResourcesDataOfMonthObject);
                scheduleResourcesDataOfMonthObject = getDataInFormOfScheduleResourcesDataOfMonth(list.get(i),
                        scheduleIDsHavingSomeStopsTimeMissing);
                isMissingDates = false;
                dateRangesListInScheduleResourcesDataOfMonthObject = new ArrayList<>();
                obj = getObjectOfDateRangeDataInScheduleListForOnlyOneDateOfSchedule(list.get(i));
                if (!obj.getStartDate().isEqual(startDate)) {
                    isMissingDates = true;
                    dateRangesListInScheduleResourcesDataOfMonthObject
                            .addAll(getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(startDate,
                                    obj.getStartDate().minusDays(1)));
                }
            }
        }
        dateRangesListInScheduleResourcesDataOfMonthObject
                .addAll(getListOfDateRangeDataInScheduleListBySettingEnableDisableStatus(obj,
                        scheduleIDTripStaffDataMap.get(scheduleResourcesDataOfMonthObject.getScheduleID())));
        if (!obj.getEndDate().isEqual(endDate)) {
            isMissingDates = true;
            dateRangesListInScheduleResourcesDataOfMonthObject
                    .addAll(getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(obj.getEndDate().plusDays(1),
                            endDate));
        }
        scheduleResourcesDataOfMonthObject.setShowMissingDatesStatus(isMissingDates);
        scheduleResourcesDataOfMonthObject.setDateRangesList(dateRangesListInScheduleResourcesDataOfMonthObject);
        output.add(scheduleResourcesDataOfMonthObject);
        return output;
    }

    private boolean isDriversAttendantsVehicleEqual(DateRangeDataInScheduleList obj,
                                                    ScheduleDateStaffsVehicle loopObj) {
        return (obj.getVehicleID() == loopObj.getVehicleDetails().getVehicleID())
                && ((new HashSet<>(obj.getDrivers().stream().map(j -> j.getUserID()).toList()))
                .equals(new HashSet<>(loopObj.getDrivers().stream().map(k -> k.getUserID()).toList())))
                && ((new HashSet<>(obj.getAttendants().stream().map(j -> j.getUserID()).toList()))
                .equals(new HashSet<>(loopObj.getAttendants().stream().map(k -> k.getUserID()).toList())));
    }

    private Collection<? extends DateRangeDataInScheduleList> getListOfDateRangeDataInScheduleListBySettingEnableDisableStatus(
            DateRangeDataInScheduleList obj, List<TripToStaff> tripStaffData) {
        LocalDate today = LocalDate.now();
        List<DateRangeDataInScheduleList> output = new ArrayList<>();
        if (obj.getEndDate().isBefore(today)) {
            obj.setDisable(true);
            output.add(obj);
        } else if (obj.getStartDate().isAfter(today)) {
            obj.setDisable(false);
            output.add(obj);
        } else {
            output.addAll(setDisableEnableFlagForDateRangeConsistingToday(obj, tripStaffData));
        }
        return output;
    }

    private ScheduleResourcesDataOfMonth getDataInFormOfScheduleResourcesDataOfMonth(
            ScheduleDateStaffsVehicle scheduleDateStaffsVehicle, Set<Integer> scheduleIDsHavingSomeStopsTimeMissing) {
        ScheduleResourcesDataOfMonth scheduleResourcesDataOfMonthObject = new ScheduleResourcesDataOfMonth();
        scheduleResourcesDataOfMonthObject.setScheduleID(scheduleDateStaffsVehicle.getScheduleID());
        scheduleResourcesDataOfMonthObject.setScheduleName(scheduleDateStaffsVehicle.getScheduleName());
        scheduleResourcesDataOfMonthObject.setRouteName(scheduleDateStaffsVehicle.getRouteName());
        if (scheduleIDsHavingSomeStopsTimeMissing.contains(scheduleDateStaffsVehicle.getScheduleID())) {
            scheduleResourcesDataOfMonthObject.setShowStopsTimeMissingStatus(true);
        }
        return scheduleResourcesDataOfMonthObject;
    }

    private Collection<? extends DateRangeDataInScheduleList> getMissingDateRangesInScheduleListForTheDatesBetweenGivenDates(
            LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).toList();
        List<DateRangeDataInScheduleList> output = new ArrayList<>();
        if (dates.contains(today)) {
            if (startDate.isBefore(today)) {
                DateRangeDataInScheduleList o1 = new DateRangeDataInScheduleList();
                o1.setStartDate(startDate);
                o1.setEndDate(today.minusDays(1));
                o1.setShowAddResourcesOption(false);
                output.add(o1);
                DateRangeDataInScheduleList o2 = new DateRangeDataInScheduleList();
                o2.setStartDate(today);
                o2.setEndDate(endDate);
                o2.setShowAddResourcesOption(true);
                output.add(o2);
            } else {
                DateRangeDataInScheduleList o1 = new DateRangeDataInScheduleList();
                o1.setStartDate(startDate);
                o1.setEndDate(endDate);
                o1.setShowAddResourcesOption(true);
                output.add(o1);
            }
        } else {
            DateRangeDataInScheduleList o1 = new DateRangeDataInScheduleList();
            o1.setStartDate(startDate);
            o1.setEndDate(endDate);
            o1.setShowAddResourcesOption(!endDate.isBefore(today));
            output.add(o1);
        }
        return output;
    }

    private LinkedHashMap<Integer, List<TripToStaff>> getDataInFormOfScheduleIDTripStaffDataMap(
            List<TripToStaff> tripStaffData) {
        if (tripStaffData.isEmpty()) {
            return new LinkedHashMap<>();
        }
        LinkedHashMap<Integer, List<TripToStaff>> output = new LinkedHashMap<>();
        int scheduleID = tripStaffData.get(0).getTripStaffID().getTripDetails().getVehicleSchedule()
                .getVehicleScheduleID();
        int tempTripID = tripStaffData.get(0).getTripStaffID().getTripDetails().getTripDetailsID();
        List<TripToStaff> value = new ArrayList<>();
        for (TripToStaff obj : tripStaffData) {
            if (obj.getTripStaffID().getTripDetails().getTripDetailsID() == tempTripID) {
                value.add(obj);
            } else {
                output.put(scheduleID, value);
                scheduleID = obj.getTripStaffID().getTripDetails().getVehicleSchedule().getVehicleScheduleID();
                value = new ArrayList<>();
                tempTripID = obj.getTripStaffID().getTripDetails().getTripDetailsID();
                value.add(obj);
            }
        }
        output.put(scheduleID, value);
        return output;
    }

    private List<DateRangeDataInScheduleList> setDisableEnableFlagForDateRangeConsistingToday(
            DateRangeDataInScheduleList obj, List<TripToStaff> tripStaffData) {

        List<DateRangeDataInScheduleList> output = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateRangeDataInScheduleList obj1 = new DateRangeDataInScheduleList();
        DateRangeDataInScheduleList obj2 = new DateRangeDataInScheduleList();
        DateRangeDataInScheduleList obj3 = new DateRangeDataInScheduleList();
        BeanUtils.copyProperties(obj, obj1);
        BeanUtils.copyProperties(obj, obj2);
        BeanUtils.copyProperties(obj, obj3);
        obj1.setDisable(true);
        obj2.setDisable(false); // obj2 for tripgoingon today
        obj3.setDisable(false);
        if (tripStaffData == null) {
            if (obj.getStartDate().isBefore(today)) {
                obj1.setEndDate(today.minusDays(1));
                output.add(obj1);
            }
            obj2.setStartDate(today);
            obj2.setEndDate(today);
            output.add(obj2);
            if (obj.getEndDate().isAfter(today)) {
                obj3.setStartDate(today.plusDays(1));
                output.add(obj3);
            }

        } else if (tripStaffData.stream().filter(i -> i.getAdminVerifiedTime() == null).toList().isEmpty()) {
            if (obj.getEndDate().isAfter(today)) {
                obj1.setEndDate(today);
                obj3.setStartDate(today.plusDays(1));
                output.add(obj1);
                output.add(obj3);
            } else {
                output.add(obj1);
            }

        } else {
            if (obj.getStartDate().isBefore(today)) {
                obj1.setEndDate(today.minusDays(1));
                output.add(obj1);
            }
            obj2.setUsersStartedTrip(
                    tripStaffData.stream().map(i -> i.getTripStaffID().getStaff().getUserID()).toList());
            obj2.setStartDate(today);
            obj2.setEndDate(today);
            output.add(obj2);
            if (obj.getEndDate().isAfter(today)) {
                obj3.setStartDate(today.plusDays(1));
                output.add(obj3);
            }

        }

        return output;
    }

    // coding checked
    private DateRangeDataInScheduleList getObjectOfDateRangeDataInScheduleListForOnlyOneDateOfSchedule(
            ScheduleDateStaffsVehicle obj) {
        DateRangeDataInScheduleList output = new DateRangeDataInScheduleList();
        output.setStartDate(obj.getDate());
        output.setEndDate(obj.getDate());
        List<UserIDName> driversIDName = new ArrayList<>();
        List<UserIDName> attendantsIDName = new ArrayList<>();
        obj.getDrivers().forEach(i -> driversIDName.add(new UserIDName(i.getUserID(),
                i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())));
        obj.getAttendants().forEach(i -> attendantsIDName.add(new UserIDName(i.getUserID(),
                i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())));
        output.setDrivers(driversIDName);
        output.setAttendants(attendantsIDName);
        output.setVehicleID(obj.getVehicleDetails().getVehicleID());
        output.setVehicleRegistrationNumber(obj.getVehicleDetails().getRegisterationNumber());
        return output;
    }

    public ResponseEntity<Object> proceedForResourcesAssignment(int month, int year) {
        if (!ValueRange.of(1, 12).isValidValue(month)) {
            logger.error("Invalid month value");
            return ResponseHandler.generateResponse1(false, "Invalid month value", HttpStatus.BAD_REQUEST, null);
        }
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(1).minusDays(1);
        List<Object[]> list = adminDao.getArrivalTimeAtAllStopsOfSchedulesInGivenDateRange(startDate, endDate);
        List<ArrivalTimeAtStopOfSchedule> arrivalTimeAtStopsOfSchedulesList = new ArrayList<>();
        for (Object[] obj : list) {

            arrivalTimeAtStopsOfSchedulesList.add(
                    new ArrivalTimeAtStopOfSchedule((int) (obj[0]), (String) (obj[1]), (int) (obj[2]), (int) (obj[3]),
                            (int) (obj[4]), (obj[5] != null) ? ((LocalTime) (obj[5])) : null, (int) (obj[6])));
        }
        HashSet<String> setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped = new HashSet<>();
        List<String> listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped = arrivalTimeAtStopsOfSchedulesList
                .stream().filter(obj -> obj.getScheduledArrivalTime() == null).map(obj -> obj.getScheduleName())
                .collect(Collectors.toList());
        if (listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped.isEmpty()) {
            return ResponseHandler.generateResponse1(true, "Can be proceeded for resources assignment", HttpStatus.OK,
                    true);
        } else {
            setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped
                    .addAll(listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped);
            logger.debug("Failed! Scheduled Time for some stops of some schedules are not mapped");
            return ResponseHandler.generateResponse1(false,
                    "Failed! Scheduled Time for some stops of below schedules are not mapped: "
                            + setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped
                            + ". Please map them, and then assign resources to schedule ",
                    HttpStatus.OK, false);
        }
    }

    public ResponseEntity<Object> getAvailableResources(int vehicleScheduleID, LocalDate startDate, LocalDate endDate) {

        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(vehicleScheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID1();
            }
            if (startDate.isAfter(endDate)) {
                logger.debug("Start date must be before or equal to End Date");
                return ResponseHandler.generateResponse1(false, "Start date must be before or equal to End Date",
                        HttpStatus.OK, null);
            }
            List<Object[]> scheduleAndVehicleStaffIDsForGivenDatesInterval = adminDao
                    .getScheduleAndVehicleStaffIDsForGivenDatesInterval(startDate, endDate);
            Set<Integer> scheduleIDsForGivenDatesInterval = new HashSet<>();
            scheduleAndVehicleStaffIDsForGivenDatesInterval
                    .forEach(obj -> scheduleIDsForGivenDatesInterval.add((int) (obj[0])));
            List<ArrivalTimeAtStopOfSchedule> arrivalTimeAtStopsOfSchedulesList = adminDao
                    .getArrivalTimeAtAllStopsOfGivenSchedules(vehicleScheduleID, scheduleIDsForGivenDatesInterval);
            HashSet<String> setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped = new HashSet<>();
            List<String> listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped = arrivalTimeAtStopsOfSchedulesList
                    .stream().filter(obj -> obj.getScheduledArrivalTime() == null).map(obj -> obj.getScheduleName())
                    .collect(Collectors.toList());
            if (!listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped.isEmpty()) {
                setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped
                        .addAll(listOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped);
                logger.debug("Failed! Scheduled Time for some stops of some other schedules are not mapped");
                return ResponseHandler.generateResponse1(false,
                        "Failed! Scheduled Time for some stops of below schedules are not mapped: "
                                + setOfScheduleNamesWhoseScheduledTimeAtSomeStopsNotMapped
                                + ". Please map them, and then assign resources to schedule "
                                + vehicleSchedule.getVehicleScheduleName(),
                        HttpStatus.OK, null);
            }
            LinkedHashMap<Integer, StartEndTimeConsideringBlockingTimeOfSchedule> mapofStartEndTimeOfSchedules = getStartEndTimeConsideringBlockingTimeOfGivenSchedules(
                    arrivalTimeAtStopsOfSchedulesList);
            List<Integer> scheduleIDsGoingOnDuringGivenSchedule = getScheduleIDsFromGivenScheduleIDsWhichWillBeGoingOnDuringGivenSchedule(
                    vehicleScheduleID, mapofStartEndTimeOfSchedules);
            HashSet<Integer> staffIDsNotAvailable = new HashSet<>();
            HashSet<Integer> vehicleIDsNotAvailable = new HashSet<>();
            for (Object[] obj : scheduleAndVehicleStaffIDsForGivenDatesInterval) {
                if (scheduleIDsGoingOnDuringGivenSchedule.contains((int) (obj[0]))) {
                    staffIDsNotAvailable.add((int) (obj[1]));
                    vehicleIDsNotAvailable.add((int) (obj[2]));
                }
            }
            AvailableResources availableResources = new AvailableResources();
            availableResources.setAvailableDrivers(getAvailableDrivers(staffIDsNotAvailable));
            availableResources.setAvailableAttendants(getAvailableAttendants(staffIDsNotAvailable));
            availableResources
                    .setAvailableVehicles(getAvailableVehiclesGivenNotAvailableVehicles(vehicleIDsNotAvailable));

            logger.debug("Available resources found.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, availableResources);

        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    private List<UserIDName> getAvailableDrivers(HashSet<Integer> staffIDsNotAvailable) {
        LinkedHashMap<Integer, String> allDriversNames = getUserNamesOfGivenRole(DRIVER_ROLE_ID);
        List<UserIDName> availableDrivers = new ArrayList<>();
        Set<Integer> allDriversUserIDs = allDriversNames.keySet();
        for (Integer userID : allDriversUserIDs) {
            if (!staffIDsNotAvailable.contains(userID)) {
                availableDrivers.add(new UserIDName(userID, allDriversNames.get(userID)));
            }
        }
        return availableDrivers;
    }

    private List<UserIDName> getAvailableAttendants(HashSet<Integer> staffIDsNotAvailable) {
        LinkedHashMap<Integer, String> allAttendantsNames = getUserNamesOfGivenRole(ATTENDANT_ROLE_ID);
        List<UserIDName> availableAttendants = new ArrayList<>();
        Set<Integer> allAttendantsUserIDs = allAttendantsNames.keySet();
        for (Integer userID : allAttendantsUserIDs) {
            if (!staffIDsNotAvailable.contains(userID)) {
                availableAttendants.add(new UserIDName(userID, allAttendantsNames.get(userID)));
            }
        }
        return availableAttendants;
    }

    private List<VehicleIDRegisterationNumberSeatCapacity> getAvailableVehiclesGivenNotAvailableVehicles(
            HashSet<Integer> vehicleIDsNotAvailable) {
        List<VehicleDetails> allVehicles = adminDao.getAllVehicles();
        List<VehicleIDRegisterationNumberSeatCapacity> availableVehicles = new ArrayList<>();
        allVehicles.forEach(obj -> {
            if (!vehicleIDsNotAvailable.contains(obj.getVehicleID())) {
                availableVehicles.add(new VehicleIDRegisterationNumberSeatCapacity(obj.getVehicleID(),
                        obj.getRegisterationNumber(), obj.getNoOfSeats()));
            }
        });
        return availableVehicles;
    }

    public ResponseEntity<Object> getResourcesAssignment(int scheduleID, int month, int year) {

        try {
            VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(scheduleID);
            if (vehicleSchedule == null) {
                return InvalidData.invalidVehicleScheduleID1();
            }
            if (!ValueRange.of(1, 12).isValidValue(month)) {
                logger.error("Invalid month value");
                return ResponseHandler.generateResponse1(false, "Invalid month value", HttpStatus.BAD_REQUEST, null);
            }
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(1).minusDays(1);
            List<Object[]> list = adminDao.getStaffVehicleDetailsOfGivenShcheduleForGivenDateInterval(scheduleID,
                    startDate, endDate);
            if (list.isEmpty()) {
                logger.debug("data not found.");
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            List<DateStaffsVehicle> dateStaffsVehicleList = arrangeDataInFormOfDateStaffsVehicleForDataOfSameSchedule(
                    list);
            List<DatesHavingSameStaffsVehicle> output = getListOfDatesHavingSameStaffsVehicleForGivenScheduleData(
                    scheduleID, dateStaffsVehicleList);
            logger.debug("data found.");
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, output);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);

        }

    }

    private List<DatesHavingSameStaffsVehicle> getListOfDatesHavingSameStaffsVehicleForGivenScheduleData(int scheduleID,
                                                                                                         List<DateStaffsVehicle> list) {
        LocalDate today = LocalDate.now();
        List<DatesHavingSameStaffsVehicle> output = new ArrayList<>();
        DatesHavingSameStaffsVehicle obj = getObjectOfDatesHavingSameStaffsVehicleForOnlyOneDateOfSchedule(list.get(0));
        for (int i = 1; i <= (list.size() - 1); i++) {
            DateStaffsVehicle loopObj = list.get(i);
            if ((obj.getEndDate().plusDays(1).isEqual(loopObj.getDate()))
                    && (obj.getVehicleID() == loopObj.getVehicleDetails().getVehicleID())
                    && ((new HashSet<>(obj.getDrivers().stream().map(j -> j.getUserID()).toList()))
                    .equals(new HashSet<>(loopObj.getDrivers().stream().map(k -> k.getUserID()).toList())))
                    && ((new HashSet<>(obj.getAttendants().stream().map(j -> j.getUserID()).toList())).equals(
                    new HashSet<>(loopObj.getAttendants().stream().map(k -> k.getUserID()).toList())))) {
                obj.setEndDate(loopObj.getDate());
            } else {
                if (obj.getEndDate().isBefore(today)) {
                    obj.setDisable(true);
                    output.add(obj);
                } else if (obj.getStartDate().isAfter(today)) {
                    obj.setDisable(false);
                    output.add(obj);
                } else {
                    output.addAll(getListOfDatesHavingSameStaffsVehicleConsistingOfTodaysDate(obj, scheduleID));
                }

                obj = getObjectOfDatesHavingSameStaffsVehicleForOnlyOneDateOfSchedule(list.get(i));
            }
        }
        if (obj.getEndDate().isBefore(today)) {
            obj.setDisable(true);
            output.add(obj);
        } else if (obj.getStartDate().isAfter(today)) {
            obj.setDisable(false);
            output.add(obj);
        } else {
            output.addAll(getListOfDatesHavingSameStaffsVehicleConsistingOfTodaysDate(obj, scheduleID));
        }
        return output;

    }

    private List<DatesHavingSameStaffsVehicle> getListOfDatesHavingSameStaffsVehicleConsistingOfTodaysDate(
            DatesHavingSameStaffsVehicle obj, int scheduleID) {
        Set<Integer> scheduleIDs = new HashSet<>();
        scheduleIDs.add(scheduleID);
        List<TripToStaff> tripStaffData = adminDao
                .getTripStaffDataCorrespondingToGivenSchedulesForGivenDateOrderedByTripID(scheduleIDs, LocalDate.now());
        List<DatesHavingSameStaffsVehicle> output = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DatesHavingSameStaffsVehicle obj1 = new DatesHavingSameStaffsVehicle();
        DatesHavingSameStaffsVehicle obj2 = new DatesHavingSameStaffsVehicle();
        DatesHavingSameStaffsVehicle obj3 = new DatesHavingSameStaffsVehicle();
        BeanUtils.copyProperties(obj, obj1);
        BeanUtils.copyProperties(obj, obj2);
        BeanUtils.copyProperties(obj, obj3);
        obj1.setDisable(true);
        obj2.setDisable(false); // obj2 for tripgoingon today
        obj3.setDisable(false);
        if (tripStaffData == null) {
            if (obj.getStartDate().isBefore(today)) {
                obj1.setEndDate(today.minusDays(1));
                output.add(obj1);
            }
            obj2.setStartDate(today);
            obj2.setEndDate(today);
            output.add(obj2);
            if (obj.getEndDate().isAfter(today)) {
                obj3.setStartDate(today.plusDays(1));
                output.add(obj3);
            }

        } else if (tripStaffData.stream().filter(i -> i.getAdminVerifiedTime() == null).toList().isEmpty()) {
            if (obj.getEndDate().isAfter(today)) {
                obj1.setEndDate(today);
                obj3.setStartDate(today.plusDays(1));
                output.add(obj1);
                output.add(obj3);
            } else {
                output.add(obj1);
            }

        } else {
            if (obj.getStartDate().isBefore(today)) {
                obj1.setEndDate(today.minusDays(1));
                output.add(obj1);
            }
            obj2.setUsersStartedTrip(
                    tripStaffData.stream().map(i -> i.getTripStaffID().getStaff().getUserID()).toList());
            obj2.setStartDate(today);
            obj2.setEndDate(today);
            output.add(obj2);
            if (obj.getEndDate().isAfter(today)) {
                obj3.setStartDate(today.plusDays(1));
                output.add(obj3);
            }

        }

        return output;
    }

    private DatesHavingSameStaffsVehicle getObjectOfDatesHavingSameStaffsVehicleForOnlyOneDateOfSchedule(
            DateStaffsVehicle obj) {

        DatesHavingSameStaffsVehicle output = new DatesHavingSameStaffsVehicle();
        output.setStartDate(obj.getDate());
        output.setEndDate(obj.getDate());
        List<UserIDName> driversIDName = new ArrayList<>();
        List<UserIDName> attendantsIDName = new ArrayList<>();
        obj.getDrivers().forEach(i -> driversIDName.add(new UserIDName(i.getUserID(),
                i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())));
        obj.getAttendants().forEach(i -> attendantsIDName.add(new UserIDName(i.getUserID(),
                i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())));
        output.setDrivers(driversIDName);
        output.setAttendants(attendantsIDName);
        output.setVehicleID(obj.getVehicleDetails().getVehicleID());
        output.setVehicleRegistrationNumber(obj.getVehicleDetails().getRegisterationNumber());
        return output;

    }

    private List<DateStaffsVehicle> arrangeDataInFormOfDateStaffsVehicleForDataOfSameSchedule(List<Object[]> list) {

        List<DateStaffsVehicle> output = new ArrayList<>();
        DateStaffsVehicle dateStaffsVehicle = getObjectOfDateStaffsVehicleForOnlyOneGivenStaff(list.get(0));
        for (int i = 1; i <= (list.size() - 1); i++) {
            VehicleScheduleDateStaffID vehicleScheduleDateStaffID = (VehicleScheduleDateStaffID) (list.get(i)[0]);
            if (vehicleScheduleDateStaffID.getDate().isEqual(dateStaffsVehicle.getDate())) {
                if (((int) (list.get(i)[1])) == DRIVER_ROLE_ID) {
                    List<User> drivers = dateStaffsVehicle.getDrivers();
                    drivers.add(vehicleScheduleDateStaffID.getStaff());
                    dateStaffsVehicle.setDrivers(drivers);
                } else {
                    List<User> attendants = dateStaffsVehicle.getAttendants();
                    attendants.add(vehicleScheduleDateStaffID.getStaff());
                    dateStaffsVehicle.setAttendants(attendants);
                }
            } else {
                output.add(dateStaffsVehicle);
                dateStaffsVehicle = getObjectOfDateStaffsVehicleForOnlyOneGivenStaff(list.get(i));
            }
        }
        output.add(dateStaffsVehicle);
        return output;

    }

    private DateStaffsVehicle getObjectOfDateStaffsVehicleForOnlyOneGivenStaff(Object[] obj) {

        DateStaffsVehicle dateStaffsVehicle = new DateStaffsVehicle();
        VehicleScheduleDateStaffID vehicleScheduleDateStaffID = (VehicleScheduleDateStaffID) obj[0];
        int staffTypeID = (int) obj[1];
        VehicleDetails vehicleDetails = (VehicleDetails) obj[2];
        dateStaffsVehicle.setDate(vehicleScheduleDateStaffID.getDate());
        List<User> drivers = new ArrayList<>();
        List<User> attendants = new ArrayList<>();
        if (staffTypeID == DRIVER_ROLE_ID) {
            drivers.add(vehicleScheduleDateStaffID.getStaff());
        } else {
            attendants.add(vehicleScheduleDateStaffID.getStaff());
        }
        dateStaffsVehicle.setDrivers(drivers);
        dateStaffsVehicle.setAttendants(attendants);
        dateStaffsVehicle.setVehicleDetails(vehicleDetails);
        return dateStaffsVehicle;

    }

    public ResponseEntity<Object> refreshVideoURL(String previousURL) {
        int index = previousURL.indexOf("/videoplayback?v=");
        String subString = previousURL.substring(index + 17);
        FileInformation fileInformation = adminDao.getFileInformationHavingGivenURL(subString);
        if (fileInformation == null) {
            return ResponseHandler.generateResponse1(false, "Invalid data!", HttpStatus.NOT_FOUND, null);
        }
        String uuid = UUID.randomUUID().toString();
        if (adminDao.updateFileInformationUUIDandHitCount(fileInformation.getFileID(), uuid)) {
            String newURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/trackgenie/public/videoplayback").toUriString();
            newURL = newURL + "?v=" + uuid;
            return ResponseHandler.generateResponse1(true, "Success", HttpStatus.OK, newURL);
        } else {
            return ResponseHandler.generateResponse1(false, "Server Error", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    public ResponseEntity<Object> videoplayback(String v) {
        logger.debug("videoplayback API hit:" + v);
        try {
            FileInformation fileInformation = adminDao.getFileInformationHavingGivenURL(v);
            if (fileInformation == null) {
                return ResponseHandler.generateResponse1(false, "Invalid data!", HttpStatus.NOT_FOUND, null);
            }
            // TODO : for testing commented
//			if (fileInformation.getHitCount() != 0) {
//				return ResponseHandler.generateResponse1(false, "Expired!", HttpStatus.NOT_FOUND, null);
//			}
            String f = fileInformation.getFileName();
            if (!adminDao.incrementFileHitCount(fileInformation.getFileID())) {
                return ResponseHandler.generateResponse1(false, "Server error!", HttpStatus.INTERNAL_SERVER_ERROR,
                        null);

            }
            System.err.println("filename:" + f);

            // return adminDao.downloadFile(f);
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
            return ResponseHandler.generateResponse1(false, "server error!!!", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }

    }

    public void unsubscribeFcmTokenOfAdminFromTopicOnLogout(String fcmToken) {
        firebaseService.unsubscribeFromAdminTopic(fcmToken);

    }

    // checked
    public ResponseEntity<Object> getDetailReport(String dateInFormOfString, int typeOfJourney) {
        try {
            LocalDate date = LocalDate.parse(dateInFormOfString);
            if (date.isAfter(LocalDate.now())) {
                logger.debug("Future Date is selected. Please select current or past date.");
                return ResponseHandler.generateResponse1(false,
                        "Future Date is selected. Please select current or past date.", HttpStatus.OK, null);
            }
            if (!((typeOfJourney == ONWARD_JOURNEY) || (typeOfJourney == RETURN_JOURNEY))) {
                return InvalidData.invalidTypeOfJourney1();
            }

            List<Integer> tripIDs = adminDao.fetchTripIDsOfGivenDateAndTypeOfJourney(date, typeOfJourney);
            if (tripIDs.isEmpty()) {
                return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
            }
            List<TripToStaff> tripStaffsData = adminDao.fetchTripStaffDataOfGivenTripIDs(tripIDs);
            logger.debug("TripStaffs data fetched.");
            HashSet<Integer> scheduleIDs = new HashSet<>();
            tripStaffsData.forEach(obj -> scheduleIDs
                    .add(obj.getTripStaffID().getTripDetails().getVehicleSchedule().getVehicleScheduleID()));
            List<Object[]> passengerScheduleData = adminDao
                    .getPassengerDetailsOfGivenSchedules(new ArrayList<>(scheduleIDs));
            List<PassengerStatus> passengerStatusData = adminDao.getPassengerStatusOfTrips(tripIDs);
            LinkedHashMap<Integer, List<TripToStaff>> tripStaffsMap = getDataInFormOfTripStaffMap(tripStaffsData);
            LinkedHashMap<Integer, List<PassengerStatus>> trippassengerStatusMap = !passengerStatusData.isEmpty() ? getDataInFormOfTripPassengerStatusMap(
                    passengerStatusData) : new LinkedHashMap<>();
            LinkedHashMap<Integer, List<User>> passengerScheduleDataMap = !passengerScheduleData.isEmpty() ? getDataInFormOfPassengerScheduleDataMap(
                    passengerScheduleData) : new LinkedHashMap<>();
            List<DetailReport> list = getDataInFormOfDetailReport(tripStaffsMap, trippassengerStatusMap,
                    passengerScheduleDataMap);
            logger.debug(DATA_FOUND);
            return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, list);
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            String s = errors.toString();
            logger.error("Exception =>  " + s);
            return ResponseHandler.generateResponse1(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    //checked
    private LinkedHashMap<Integer, List<User>> getDataInFormOfPassengerScheduleDataMap(List<Object[]> list) {
        LinkedHashMap<Integer, List<User>> output = new LinkedHashMap<>();
        List<User> users = new ArrayList<>();
        users.add((User) (list.get(0)[1]));
        int temp = (int) (list.get(0)[0]);
        for (int i = 1; i < list.size(); i++) {
            if ((int) (list.get(i)[0]) == temp) {
                users.add((User) (list.get(i)[1]));
            } else {
                output.put(temp, users);
                users = new ArrayList<>();
                users.add((User) (list.get(i)[1]));
                temp = (int) (list.get(i)[0]);
            }
        }
        output.put(temp, users);
        return output;
    }

    // checked
    private LinkedHashMap<Integer, List<TripToStaff>> getDataInFormOfTripStaffMap(List<TripToStaff> list) {
        LinkedHashMap<Integer, List<TripToStaff>> output = new LinkedHashMap<>();
        List<TripToStaff> tripStaffs = new ArrayList<>();
        int temp = list.get(0).getTripStaffID().getTripDetails().getTripDetailsID();
        for (TripToStaff i : list) {
            if (i.getTripStaffID().getTripDetails().getTripDetailsID() == temp) {
                tripStaffs.add(i);
            } else {
                output.put(temp, tripStaffs);
                tripStaffs = new ArrayList<>();
                temp = i.getTripStaffID().getTripDetails().getTripDetailsID();
                tripStaffs.add(i);
            }
        }
        output.put(temp, tripStaffs);
        return output;
    }

    // checked
    private LinkedHashMap<Integer, List<PassengerStatus>> getDataInFormOfTripPassengerStatusMap(
            List<PassengerStatus> list) {
        LinkedHashMap<Integer, List<PassengerStatus>> output = new LinkedHashMap<>();
        List<PassengerStatus> passengers = new ArrayList<>();
        int temp = list.get(0).getTripUser().getTripDetails().getTripDetailsID();
        for (PassengerStatus i : list) {
            if (i.getTripUser().getTripDetails().getTripDetailsID() == temp) {
                passengers.add(i);
            } else {
                output.put(temp, passengers);
                temp = i.getTripUser().getTripDetails().getTripDetailsID();
                passengers = new ArrayList<>();
                passengers.add(i);
            }
        }
        output.put(temp, passengers);
        return output;
    }

    // checked
    private List<DetailReport> getDataInFormOfDetailReport(LinkedHashMap<Integer, List<TripToStaff>> tripStaffsMap,
                                                           LinkedHashMap<Integer, List<PassengerStatus>> trippassengerStatusMap,
                                                           LinkedHashMap<Integer, List<User>> passengerScheduleDataMap) {
        List<DetailReport> output = new ArrayList<>();
        Set<Integer> tripIDs = tripStaffsMap.keySet();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        for (Integer tripID : tripIDs) {
            List<TripToStaff> staffs = tripStaffsMap.get(tripID);
            List<PassengerStatus> passengers = trippassengerStatusMap.get(tripID);
            TripDetails trip = staffs.get(0).getTripStaffID().getTripDetails();
            DetailReport detailReport = new DetailReport();
            detailReport.setRouteID(trip.getVehicleSchedule().getRoute().getRouteID());
            detailReport.setRouteName(trip.getVehicleSchedule().getRoute().getRouteName());
            detailReport.setVehicleScheduleName(trip.getVehicleSchedule().getVehicleScheduleName());
            detailReport.setTripStartTime(trip.getTripStart().format(formatter));
            detailReport.setTripDestinationTime(
                    trip.getBusReachedDestination() == null ? "-" : trip.getBusReachedDestination().format(formatter));
            detailReport.setDrivers(getDataInFormOfListOfTripStaffDetails(staffs.stream()
                    .filter(obj -> obj.getStaffType().getRoleID() == DRIVER_ROLE_ID).collect(Collectors.toList())));
            detailReport.setAttendants(getDataInFormOfListOfTripStaffDetails(staffs.stream()
                    .filter(obj -> obj.getStaffType().getRoleID() == ATTENDANT_ROLE_ID).collect(Collectors.toList())));
            detailReport.setPassengers(getDataInFormOfListOfPassengerDetailsOfTrip(passengers != null ? passengers : new ArrayList<>(),
                    (passengerScheduleDataMap.get(trip.getVehicleSchedule().getVehicleScheduleID())) != null ? (passengerScheduleDataMap.get(trip.getVehicleSchedule().getVehicleScheduleID())) : new ArrayList<>()));
            output.add(detailReport);
        }
        return output;
    }

    //checked
    private List<TripStaffDetails> getDataInFormOfListOfTripStaffDetails(List<TripToStaff> list) {
        List<TripStaffDetails> output = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        list.forEach(obj -> {
            User staff = obj.getTripStaffID().getStaff();
            output.add(new TripStaffDetails(staff.getUserUniqueKey(),
                    staff.getUserFirstName() + " " + staff.getUserMiddleName() + " " + staff.getUserLastName(),
                    obj.getStaffLoginTime().format(formatter),
                    obj.getStaffVerifiedTime() != null ? obj.getStaffVerifiedTime().format(formatter) : "-",
                    obj.getAdminVerifiedTime() != null ? obj.getAdminVerifiedTime().format(formatter) : "-"));
        });
        return output;
    }

    //checked
    private List<PassengerDetailsOfTrip> getDataInFormOfListOfPassengerDetailsOfTrip(
            List<PassengerStatus> passengersOfTrip, List<User> passengersOfSchedule) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        List<PassengerDetailsOfTrip> passengersList = new ArrayList<>();
        passengersOfTrip.forEach(obj -> {
            User passenger = obj.getTripUser().getUser();
            PassengerDetailsOfTrip passengerDetailsOfTrip = new PassengerDetailsOfTrip();
            passengerDetailsOfTrip.setName(passenger.getUserFirstName() + " " + passenger.getUserMiddleName() + " "
                    + passenger.getUserLastName());
            passengerDetailsOfTrip.setUniqueKey(passenger.getUserUniqueKey());
            passengerDetailsOfTrip.setPickupDateTime(
                    obj.getPassengerPickedUpTime() != null ? obj.getPassengerPickedUpTime().format(formatter) : "-");
            passengerDetailsOfTrip.setDropDateTime(
                    obj.getPassengerDropTime() != null ? obj.getPassengerDropTime().format(formatter) : "-");
            if (obj.getUserStatusCode().getStatusID() == MISSED_BUS) {
                passengerDetailsOfTrip.setMissedBusDateTime(obj.getUpdatedTime().format(formatter));
            } else {
                passengerDetailsOfTrip.setMissedBusDateTime("-");
            }
            if (obj.getUserStatusCode().getStatusID() == PICKED_UP) {
                passengerDetailsOfTrip.setStatus(PICKED_UP_STATUS);
            } else if (obj.getUserStatusCode().getStatusID() == DROPPED) {
                passengerDetailsOfTrip.setStatus(DROPPED_STATUS);
            } else if (obj.getUserStatusCode().getStatusID() == MISSED_BUS) {
                passengerDetailsOfTrip.setStatus(MISSED_BUS_STATUS);
            } else if (obj.getUserStatusCode().getStatusID() == SCHEDULED_LEAVE) {
                passengerDetailsOfTrip.setStatus(SCHEDULED_LEAVE_STATUS);
            }
            passengersList.add(passengerDetailsOfTrip);
        });
        List<Integer> passengerIDsOfTrip = passengersOfTrip.stream().map(obj -> obj.getTripUser().getUser().getUserID())
                .collect(Collectors.toList());
        passengersList.addAll(passengersOfSchedule.stream().filter(obj -> !passengerIDsOfTrip.contains(obj.getUserID()))
                .map(obj -> new PassengerDetailsOfTrip(
                        obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName(),
                        obj.getUserUniqueKey(), "-", "-", "-", YET_TO_BE_PICKED__STATUS))
                .collect(Collectors.toList()));
        return passengersList;
    }

    public ResponseEntity<Object> getUnmappedMappedResourcesSchedulesList() {
        List<VehicleSchedule> allSchedulesList = adminDao.getVehicleScheduleList();
        if (allSchedulesList.isEmpty()) {
            return ResponseHandler.generateResponse1(false, NO_DATA_FOUND, HttpStatus.NOT_FOUND, null);
        }
        List<Object[]> unmappedResourcesSchedulesList = adminDao.getUnmappedResourcesScheduleIDs();
        List<Integer> unmappedResourcesScheduleIDs = unmappedResourcesSchedulesList.stream().map(obj -> (int) (obj[0]))
                .toList();
        UnmappedMappedResourcesSchedules output = new UnmappedMappedResourcesSchedules();
        output.setUnmappedResourcesSchedules(unmappedResourcesSchedulesList.stream()
                .map(obj -> new VehicleScheduleIDName((int) (obj[0]), (String) (obj[1]))).toList());
        output.setMappedResourcesSchedules(allSchedulesList.stream()
                .filter(obj -> !unmappedResourcesScheduleIDs.contains(obj.getVehicleScheduleID()))
                .map(obj -> new VehicleScheduleIDName(obj.getVehicleScheduleID(), obj.getVehicleScheduleName()))
                .toList());
        return ResponseHandler.generateResponse1(true, DATA_FOUND, HttpStatus.OK, output);
    }

    public ResponseEntity<Object> assignResources(int scheduleID,
                                                  ResourcesAssignmentUpdation updatedResourcesAssignments) {

        ResponseEntity<Object> errorMessage = validateInputDataOfUpdateResourcesAssignmentIfValidReturnNull(scheduleID,
                updatedResourcesAssignments);
        if (errorMessage != null) {
            return errorMessage;
        }
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;
        if (updatedResourcesAssignments.getTrueIfMonthwiseAndfalseIfDaterangewise()) {
            start = (updatedResourcesAssignments.getMonth() == today.getMonthValue())
                    && (updatedResourcesAssignments.getYear() == today.getYear()) ? today
                    : LocalDate.of(updatedResourcesAssignments.getYear(),
                    updatedResourcesAssignments.getMonth(), 1);
            end = LocalDate.of(updatedResourcesAssignments.getYear(), updatedResourcesAssignments.getMonth(), 1)
                    .plusMonths(1).withDayOfMonth(1).minusDays(1);
        } else {
            start = updatedResourcesAssignments.getOldStartDate().isAfter(LocalDate.now())
                    ? updatedResourcesAssignments.getOldStartDate()
                    : today;
            end = updatedResourcesAssignments.getOldEndDate();
        }

        List<Object[]> resourcesAssignmentDataOrderedDatewiseFromDB = adminDao
                .getStaffVehicleDetailsOfGivenScheduleForGivenDateIntervalOrderedDatewise(scheduleID, start, end);
        if (resourcesAssignmentDataOrderedDatewiseFromDB.isEmpty()) {
            return assignNewResources(scheduleID, updatedResourcesAssignments.getEntries(), start, end);
        }
        LinkedHashMap<LocalDate, Resources> oldResourcesAssignmentMap = getDataInFormOfResourcesAssignmentMap(
                resourcesAssignmentDataOrderedDatewiseFromDB);

        LinkedHashMap<LocalDate, Resources> updatedResourcesAssignmentMap = getDataInFormOfResourcesAssignmentMap(
                updatedResourcesAssignments, start, end);
        if (start.isEqual(today)) {

            errorMessage = checkTodayResourcesDataCanBeEditedReturnNullIfCanBeEditedElseReturnErrorMessage(scheduleID,
                    oldResourcesAssignmentMap.get(today), updatedResourcesAssignmentMap.get(today));
            if (errorMessage != null) {
                return errorMessage;
            }
        }

        ResourcesAssignmentChangesToBeDone resourcesAssignmentChangesToBeDone = getResourcesAssignmentChangesToBeDone(
                oldResourcesAssignmentMap, updatedResourcesAssignmentMap);

        return adminDao.updateResourcesAssignment(scheduleID, resourcesAssignmentChangesToBeDone);
    }

    private ResourcesAssignmentChangesToBeDone getResourcesAssignmentChangesToBeDone(
            LinkedHashMap<LocalDate, Resources> oldResourcesAssignmentMap,
            LinkedHashMap<LocalDate, Resources> updatedResourcesAssignmentMap) {
        Set<LocalDate> oldResourcesAssignmentMapDates = oldResourcesAssignmentMap.keySet();
        Set<LocalDate> updatedResourcesAssignmentMapDates = updatedResourcesAssignmentMap.keySet();

        List<LocalDate> deleteDates = new ArrayList<>();
        List<LocalDate> temp = new ArrayList<>();
        temp.addAll(oldResourcesAssignmentMapDates);
        temp.removeAll(updatedResourcesAssignmentMapDates);
        deleteDates.addAll(temp);

        List<IDDate> addDrivers = new ArrayList<>();
        List<IDDate> addAttendants = new ArrayList<>();
        List<IDDate> addVehicles = new ArrayList<>();
        List<IDDate> disassociateDrivers = new ArrayList<>();
        List<IDDate> disassociateAttendants = new ArrayList<>();
        List<IDDate> disassociateVehicles = new ArrayList<>();

        temp = new ArrayList<>();
        temp.addAll(updatedResourcesAssignmentMapDates);
        temp.removeAll(oldResourcesAssignmentMapDates);
        temp.forEach(obj -> {
            Resources resources = updatedResourcesAssignmentMap.get(obj);
            resources.getDriverIDs().forEach(i -> addDrivers.add(new IDDate(i, obj)));
            resources.getAttendantIDs().forEach(i -> addAttendants.add(new IDDate(i, obj)));
            addVehicles.add(new IDDate(resources.getVehicleID(), obj));
        });

        temp = new ArrayList<>();
        temp.addAll(oldResourcesAssignmentMapDates);
        temp.retainAll(updatedResourcesAssignmentMapDates);
        for (LocalDate date : temp) {

            Resources old = oldResourcesAssignmentMap.get(date);
            Resources updated = updatedResourcesAssignmentMap.get(date);

            if (old.getVehicleID() != updated.getVehicleID()) {
                addVehicles.add(new IDDate(updated.getVehicleID(), date));
                disassociateVehicles.add(new IDDate(old.getVehicleID(), date));
            }
            List<Integer> tempIDs;
            if (!(new HashSet<>(old.getDriverIDs()).equals(new HashSet<>(updated.getDriverIDs())))) {
                tempIDs = new ArrayList<>();
                tempIDs.addAll(old.getDriverIDs());
                tempIDs.removeAll(updated.getDriverIDs());
                tempIDs.forEach(i -> disassociateDrivers.add(new IDDate(i, date)));
                tempIDs = new ArrayList<>();
                tempIDs.addAll(updated.getDriverIDs());
                tempIDs.removeAll(old.getDriverIDs());
                tempIDs.forEach(i -> addDrivers.add(new IDDate(i, date)));
            }
            if (!(new HashSet<>(old.getAttendantIDs()).equals(new HashSet<>(updated.getAttendantIDs())))) {
                tempIDs = new ArrayList<>();
                tempIDs.addAll(old.getAttendantIDs());
                tempIDs.removeAll(updated.getAttendantIDs());
                tempIDs.forEach(i -> disassociateAttendants.add(new IDDate(i, date)));
                tempIDs = new ArrayList<>();
                tempIDs.addAll(updated.getAttendantIDs());
                tempIDs.removeAll(old.getAttendantIDs());
                tempIDs.forEach(i -> addAttendants.add(new IDDate(i, date)));
            }
        }
        List<Integer> userIDs = new ArrayList<>();
        List<Integer> vehicleIDs = new ArrayList<>();
        addDrivers.forEach(i -> userIDs.add(i.getId()));
        addAttendants.forEach(i -> userIDs.add(i.getId()));
        disassociateDrivers.forEach(i -> userIDs.add(i.getId()));
        disassociateAttendants.forEach(i -> userIDs.add(i.getId()));
        addVehicles.forEach(i -> vehicleIDs.add(i.getId()));
        disassociateVehicles.forEach(i -> vehicleIDs.add(i.getId()));
        ResourcesAssignmentChangesToBeDone resourcesAssignmentChangesToBeDone = new ResourcesAssignmentChangesToBeDone();
        resourcesAssignmentChangesToBeDone.setDeleteDates(deleteDates);
        resourcesAssignmentChangesToBeDone.setAddDrivers(addDrivers);
        resourcesAssignmentChangesToBeDone.setAddAttendants(addAttendants);
        resourcesAssignmentChangesToBeDone.setAddVehicles(addVehicles);
        resourcesAssignmentChangesToBeDone.setDisassociateDrivers(disassociateDrivers);
        resourcesAssignmentChangesToBeDone.setDisassociateAttendants(disassociateAttendants);
        resourcesAssignmentChangesToBeDone.setDisassociateVehicles(disassociateVehicles);
        resourcesAssignmentChangesToBeDone.setUserIDs(userIDs);
        resourcesAssignmentChangesToBeDone.setVehicleIDs(vehicleIDs);
        return resourcesAssignmentChangesToBeDone;
    }

    private ResponseEntity<Object> checkTodayResourcesDataCanBeEditedReturnNullIfCanBeEditedElseReturnErrorMessage(
            int scheduleID, Resources oldResourcesAssignmentOfToday, Resources updatedResourcesAssignmentOfToday) {

        List<TripToStaff> tripStaffData = adminDao
                .getTripStaffDataCorrespondingToGivenScheduleStartingOnGivenDate(scheduleID, LocalDate.now());
        boolean tripCompleted = tripStaffData.stream().filter(obj -> obj.getAdminVerifiedTime() == null).toList()
                .isEmpty();
        if ((oldResourcesAssignmentOfToday != null) && (updatedResourcesAssignmentOfToday == null)
                && (!tripStaffData.isEmpty())) {
            logger.debug("Today's all resources assignment data cannot be deleted");
            return ResponseHandler.generateResponse2(false,
                    "Failed! Today's all resources assignment data cannot be deleted, as corresponding trip is already"
                            + ((tripCompleted) ? " completed." : " started."),
                    HttpStatus.OK);
        }
        if ((oldResourcesAssignmentOfToday != null) && (updatedResourcesAssignmentOfToday != null)
                && (!tripStaffData.isEmpty())) {
            ResponseEntity<Object> check = checkTodayResourcesDataCanBeEditedResourcesAssignmentTripStaffDataPresentReturnNullIfCanBeEditedElseReturnErrorMessage(
                    oldResourcesAssignmentOfToday, updatedResourcesAssignmentOfToday, tripStaffData, tripCompleted);
            if (check != null) {
                return check;
            }
        }
        return null;
    }

    private LinkedHashMap<LocalDate, Resources> getDataInFormOfResourcesAssignmentMap(
            ResourcesAssignmentUpdation updatedResourcesAssignments, LocalDate start, LocalDate end) {
        LinkedHashMap<LocalDate, Resources> output = new LinkedHashMap<>();
        List<LocalDate> dates = start.datesUntil(end.plusDays(1)).toList();
        for (ResourcesAssignmentEntry obj : updatedResourcesAssignments.getEntries()) {
            List<LocalDate> tempDates = obj.getStartDate().datesUntil(obj.getEndDate().plusDays(1))
                    .collect(Collectors.toList());
            tempDates.retainAll(dates);
            tempDates.forEach(
                    i -> output.put(i, new Resources(obj.getDriverIDs(), obj.getAttendantIDs(), obj.getVehicleID())));
        }
        return output;
    }

    private LinkedHashMap<LocalDate, Resources> getDataInFormOfResourcesAssignmentMap(
            List<Object[]> resourcesAssignmentDataOrderedDatewiseFromDB) {
        LinkedHashMap<LocalDate, Resources> output = new LinkedHashMap<>();
        LocalDate temp = (LocalDate) (resourcesAssignmentDataOrderedDatewiseFromDB.get(0)[0]);
        List<Integer> driverIDs = new ArrayList<>();
        List<Integer> attendantIDs = new ArrayList<>();
        int vehicleID = (Integer) (resourcesAssignmentDataOrderedDatewiseFromDB.get(0)[3]);
        for (Object[] obj : resourcesAssignmentDataOrderedDatewiseFromDB) {
            if (temp.isEqual((LocalDate) (obj[0]))) {
                if ((Integer) (obj[2]) == DRIVER_ROLE_ID) {
                    driverIDs.add((Integer) (obj[1]));
                } else {
                    attendantIDs.add((Integer) (obj[1]));
                }
            } else {
                output.put(temp, new Resources(driverIDs, attendantIDs, vehicleID));
                temp = (LocalDate) (obj[0]);
                driverIDs = new ArrayList<>();
                attendantIDs = new ArrayList<>();
                vehicleID = (Integer) (obj[3]);
                if ((Integer) (obj[2]) == DRIVER_ROLE_ID) {
                    driverIDs.add((Integer) (obj[1]));
                } else {
                    attendantIDs.add((Integer) (obj[1]));
                }
            }

        }
        output.put(temp, new Resources(driverIDs, attendantIDs, vehicleID));
        return output;
    }

    private ResponseEntity<Object> validateInputDataOfUpdateResourcesAssignmentIfValidReturnNull(int scheduleID,
                                                                                                 ResourcesAssignmentUpdation updatedResourcesAssignments) {

        if (updatedResourcesAssignments.getTrueIfMonthwiseAndfalseIfDaterangewise()) {
            if (!((updatedResourcesAssignments.getMonth() >= 1) && (updatedResourcesAssignments.getMonth() <= 12))) {
                logger.error("Invalid data");
                return ResponseHandler.generateResponse2(false, "Failed! Invalid data", HttpStatus.BAD_REQUEST);
            }
            if (LocalDate.of(updatedResourcesAssignments.getYear(), updatedResourcesAssignments.getMonth(), 1)
                    .plusMonths(1).minusDays(1).isBefore(LocalDate.now())) {
                logger.debug("Past month data cannot be edited.");
                return ResponseHandler.generateResponse2(false, "Failed! Past month data cannot be edited.",
                        HttpStatus.OK);
            }
        } else {
            if (updatedResourcesAssignments.getOldEndDate().isBefore(LocalDate.now())) {
                logger.debug("Past data cannot be edited");
                return ResponseHandler.generateResponse2(false, "Failed! Past data cannot be edited", HttpStatus.OK);
            }
            if (updatedResourcesAssignments.getOldEndDate().isBefore(updatedResourcesAssignments.getOldStartDate())) {
                logger.error("Invalid data");
                return ResponseHandler.generateResponse2(false, "Failed! Invalid data", HttpStatus.BAD_REQUEST);
            }
        }
        LocalDate checkStart = updatedResourcesAssignments.getTrueIfMonthwiseAndfalseIfDaterangewise()
                ? LocalDate.of(updatedResourcesAssignments.getYear(), updatedResourcesAssignments.getMonth(), 1)
                : updatedResourcesAssignments.getOldStartDate();
        LocalDate checkEnd = updatedResourcesAssignments.getTrueIfMonthwiseAndfalseIfDaterangewise()
                ? checkStart.plusMonths(1).withDayOfMonth(1).minusDays(1)
                : updatedResourcesAssignments.getOldEndDate();
        List<ResourcesAssignmentEntry> entries = updatedResourcesAssignments.getEntries();
        for (int i = 0; i < entries.size() - 1; i++) {
            if (!((!entries.get(i).getStartDate().isAfter(entries.get(i).getEndDate()))
                    && (entries.get(i).getEndDate().isBefore(entries.get(i + 1).getStartDate())))) {
                logger.error("Date range not selected properly");
                return ResponseHandler.generateResponse2(false, "Failed! Please check date ranges.",
                        HttpStatus.BAD_REQUEST);
            }
        }
        ResourcesAssignmentEntry lastEntry = entries.get(entries.size() - 1);
        if ((checkStart.isAfter(entries.get(0).getStartDate())) || (checkEnd.isBefore(lastEntry.getEndDate()))
                || (lastEntry.getStartDate().isAfter(lastEntry.getEndDate()))) {
            logger.error("Date range not selected properly");
            return ResponseHandler.generateResponse2(false, "Failed! Please check date ranges.",
                    HttpStatus.BAD_REQUEST);
        }
        VehicleSchedule vehicleSchedule = adminDao.getVehicleSchedule(scheduleID);
        if (vehicleSchedule == null) {
            return InvalidData.invalidVehicleScheduleID2();
        }
        List<Integer> driverIDsInEntries = new ArrayList<>();
        List<Integer> attendantIDsInEntries = new ArrayList<>();
        List<Integer> vehicleIDsInEntries = new ArrayList<>();
        for (ResourcesAssignmentEntry obj : entries) {
            if (obj.getDriverIDs().isEmpty()) {
                logger.debug("Minimum 1 driver must be assigned.");
                return ResponseHandler.generateResponse2(false, "Minimum 1 driver must be assigned.",
                        HttpStatus.BAD_REQUEST);
            }
            driverIDsInEntries.addAll(obj.getDriverIDs());
            attendantIDsInEntries.addAll(obj.getAttendantIDs());
            vehicleIDsInEntries.add(obj.getVehicleID());
            List<Integer> t = new ArrayList<>();
            t.addAll(obj.getDriverIDs());
            t.retainAll(obj.getAttendantIDs());
            if (!t.isEmpty()) {
                List<User> commonUsers = adminDao.getUsersHavingUserIDs(t);
                List<String> commonUserNames = commonUsers.stream()
                        .map(i -> i.getUserFirstName() + " " + i.getUserMiddleName() + " " + i.getUserLastName())
                        .toList();
                return ResponseHandler.generateResponse2(false,
                        "Failed! Below users can't be driver and attendant at same time for date range "
                                + obj.getStartDate() + " to " + obj.getEndDate() + " : " + commonUserNames,
                        HttpStatus.OK);
            }
        }
        if ((!adminDao.getUserListOfGivenRole(DRIVER_ROLE_ID).stream().map(i -> i.getUserID())
                .collect(Collectors.toList()).containsAll(driverIDsInEntries))
                || (!adminDao.getUserListOfGivenRole(ATTENDANT_ROLE_ID).stream().map(i -> i.getUserID())
                .collect(Collectors.toList()).containsAll(attendantIDsInEntries))
                || (!adminDao.getVehicleIDs().containsAll(vehicleIDsInEntries))) {
            logger.error("Invalid data");
            return ResponseHandler.generateResponse2(false, "Invalid driver or attendant or vehicle ID",
                    HttpStatus.NOT_FOUND);
        }
        return null;
    }

    private ResponseEntity<Object> checkTodayResourcesDataCanBeEditedResourcesAssignmentTripStaffDataPresentReturnNullIfCanBeEditedElseReturnErrorMessage(
            Resources oldResourcesAssignmentOfToday, Resources updatedResourcesAssignmentOfToday,
            List<TripToStaff> tripStaffData, boolean tripCompleted) {

        String allDisassociated = "";
        String disassociated = "";
        String added = "";
        String vehicleOutput = "";
        if (oldResourcesAssignmentOfToday.getVehicleID() != updatedResourcesAssignmentOfToday.getVehicleID()) {
            List<Integer> vehicleIDs = List.of(oldResourcesAssignmentOfToday.getVehicleID(),
                    updatedResourcesAssignmentOfToday.getVehicleID());
            List<VehicleDetails> vehicles = adminDao.getVehicleDetailsOfGivenIDs(vehicleIDs);
            String oldVehicleRegNumber = vehicles.stream()
                    .filter(obj -> obj.getVehicleID() == oldResourcesAssignmentOfToday.getVehicleID())
                    .map(obj -> obj.getRegisterationNumber()).toList().get(0);
            String updatedVehicleRegNumber = vehicles.stream()
                    .filter(obj -> obj.getVehicleID() == updatedResourcesAssignmentOfToday.getVehicleID())
                    .map(obj -> obj.getRegisterationNumber()).toList().get(0);
            vehicleOutput = vehicleOutput + " vehicle cannot be changed from " + oldVehicleRegNumber + " to "
                    + updatedVehicleRegNumber + ".";
        }
        List<Integer> driverIDs = new ArrayList<>();
        driverIDs.addAll(oldResourcesAssignmentOfToday.getDriverIDs());
        driverIDs.removeAll(updatedResourcesAssignmentOfToday.getDriverIDs());
        List<Integer> attendantIDs = new ArrayList<>();
        attendantIDs.addAll(oldResourcesAssignmentOfToday.getAttendantIDs());
        attendantIDs.removeAll(updatedResourcesAssignmentOfToday.getAttendantIDs());
        List<Integer> tempDriverIDs = tripStaffData.stream()
                .filter(obj -> obj.getStaffType().getRoleID() == DRIVER_ROLE_ID)
                .map(obj -> obj.getTripStaffID().getStaff().getUserID()).collect(Collectors.toList());
        List<Integer> tempAttendantIDs = tripStaffData.stream()
                .filter(obj -> obj.getStaffType().getRoleID() == ATTENDANT_ROLE_ID)
                .map(obj -> obj.getTripStaffID().getStaff().getUserID()).collect(Collectors.toList());
        tempDriverIDs.retainAll(driverIDs);
        tempAttendantIDs.retainAll(attendantIDs);
        List<Integer> userIDs = new ArrayList<>();
        userIDs.addAll(driverIDs);
        userIDs.addAll(attendantIDs);
        List<Integer> driverIDs2 = new ArrayList<>();
        driverIDs2.addAll(updatedResourcesAssignmentOfToday.getDriverIDs());
        driverIDs2.removeAll(oldResourcesAssignmentOfToday.getDriverIDs());
        List<Integer> attendantIDs2 = new ArrayList<>();
        attendantIDs2.addAll(updatedResourcesAssignmentOfToday.getAttendantIDs());
        attendantIDs2.removeAll(oldResourcesAssignmentOfToday.getAttendantIDs());
        userIDs.addAll(driverIDs2);
        userIDs.addAll(attendantIDs2);
        if (!userIDs.isEmpty()) {
            List<User> users = adminDao.getUsersHavingUserIDs(userIDs);
            List<String> allDisassociatedDrivers = users.stream().filter(obj -> driverIDs.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            List<String> allDisassociatedAttendants = users.stream()
                    .filter(obj -> attendantIDs.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            List<String> disassociatedDrivers = users.stream().filter(obj -> tempDriverIDs.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            List<String> disassociatedAttendants = users.stream()
                    .filter(obj -> tempAttendantIDs.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            List<String> addedDrivers = users.stream().filter(obj -> driverIDs2.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            List<String> addedAttendants = users.stream().filter(obj -> attendantIDs2.contains(obj.getUserID()))
                    .map(obj -> obj.getUserFirstName() + " " + obj.getUserMiddleName() + " " + obj.getUserLastName())
                    .toList();
            allDisassociated = allDisassociated
                    + ((!allDisassociatedDrivers.isEmpty()) ? (" Driver " + allDisassociatedDrivers) : "")
                    + ((!allDisassociatedAttendants.isEmpty()) ? (" Attendant " + allDisassociatedAttendants) : "");
            disassociated = disassociated
                    + ((!disassociatedDrivers.isEmpty()) ? (" Driver " + disassociatedDrivers) : "")
                    + ((!disassociatedAttendants.isEmpty()) ? (" Attendant " + disassociatedAttendants) : "");

            added = added + ((!addedDrivers.isEmpty()) ? (" Driver " + addedDrivers) : "")
                    + ((!addedAttendants.isEmpty()) ? (" Attendant " + addedAttendants) : "");

            allDisassociated = allDisassociated + ((allDisassociated.length() != 0) ? " cannot be disassociated " : "");
            disassociated = disassociated + ((disassociated.length() != 0) ? " cannot be disassociated " : "");
            added = added + ((added.length() != 0) ? " cannot be added  " : "");
        }
        if ((oldResourcesAssignmentOfToday.getVehicleID() == updatedResourcesAssignmentOfToday.getVehicleID())
                && (userIDs.isEmpty())) {
            return null;
        }
        String output = "Failed! "
                + (tripCompleted ? ("As today's trip is completed," + allDisassociated + added + vehicleOutput)
                : ("As today's trip is already started," + disassociated + vehicleOutput));
        logger.debug("Today's resources data cannot be edited.");
        return ResponseHandler.generateResponse2(false, output, HttpStatus.OK);
    }

    public ResponseEntity<Object> getAPICount(int tripID) {
        TripDetails trip = driverDao.getTripDetails(tripID);
        if (trip == null) {
            return InvalidData.invalidTripID1();
        }
        APICount apiCount = adminDao.getAPICount(tripID);
        APICountJSON apiCountJSON = new APICountJSON();
        BeanUtils.copyProperties(apiCount, apiCountJSON);
        return ResponseHandler.generateResponse1(true, "Data found.", HttpStatus.OK, apiCountJSON);
    }

    public AutoCompleteAPIBodyStatusCodeHeaders autocomplete(String search, String correlationId) {
        ResponseEntity<Object> autocompleteAPIResponseEntity = googleMapAPIHelper.autocomplete(search, correlationId).block();
        AutoCompleteAPIBodyStatusCodeHeaders output = new AutoCompleteAPIBodyStatusCodeHeaders();
        output.setApiBody(autocompleteAPIResponseEntity.getBody());
        output.setHttpHeaders(autocompleteAPIResponseEntity.getHeaders());
        output.setHttpStatusCode(autocompleteAPIResponseEntity.getStatusCode().value());
        return output;
    }




}
