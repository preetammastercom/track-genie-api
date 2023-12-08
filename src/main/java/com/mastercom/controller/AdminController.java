package com.mastercom.controller;

//phase 2
// TODO : APIs of "delete" are to be modified.

import com.mastercom.dto.*;
import com.mastercom.dto.request.GenericRequestDTO;
import com.mastercom.dto.response.ResponseWrapper;
import com.mastercom.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.mastercom.constant.ApplicationConstant.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("trackgenie/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    // TODO : remember to delete this
    @GetMapping("tryQuery")
    public Object tryQuery() {
        return adminService.tryQuery();
    }

    @GetMapping("getDriverList")
    public ResponseEntity<Object> getDriverList() {
        return adminService.getDriverList();

    }

    @GetMapping("getAttendantList")
    public ResponseEntity<Object> getAttendantList() {
        return adminService.getAttendantList();

    }


    @PostMapping("addStudent")
    public ResponseEntity<Object> addStudent(@RequestBody AddStudent addStudent) {
        return adminService.addStudent(addStudent);
    }


    @PostMapping("addDriver")
    public ResponseEntity<Object> addDriver(@RequestBody DriverDTO driverDTO) {
        return adminService.addDriver(driverDTO);
    }


    @PostMapping("addAttendant")
    public ResponseEntity<Object> addAttendant(@RequestBody AttendantDTO attendantDTO) {
        return adminService.addAttendant(attendantDTO);
    }

    @GetMapping("getRouteList")
    public ResponseEntity<Object> getRouteList() {
        return adminService.getRouteList();

    }

    @GetMapping("getStopsofRoute/{routeID}")
    public ResponseEntity<Object> getStopsofRoute(@PathVariable int routeID) {
        return adminService.getStopsofRoute(routeID);

    }

    @PostMapping("addVehicle")
    public ResponseEntity<Object> addVehicle(@RequestBody VehicleDetailsDTO vehicleDetailsDTO) {
        return adminService.addVehicle(vehicleDetailsDTO);
    }

    @DeleteMapping("deleteRoute/{routeID}")
    public ResponseEntity<Object> deleteRoute(@PathVariable int routeID) {
        return adminService.deleteRoute(routeID);
    }

    @GetMapping("getRouteListWithSourceAndDestination")
    public ResponseEntity<Object> getRouteListWithSourceAndDestination() {
        return adminService.getRouteListWithSourceAndDestination();

    }

    @GetMapping("getStudentList")
    public ResponseEntity<Object> getStudentList() {
        return adminService.getStudentList();

    }

    @DeleteMapping("deleteStudent/{userID}")
    public ResponseEntity<Object> deleteStudent(@PathVariable int userID) {
        return adminService.deleteStudent(userID);
    }

    @DeleteMapping("deleteDriver/{userID}")
    public ResponseEntity<Object> deleteDriver(@PathVariable int userID) {
        return adminService.deleteDriver(userID);
    }

    @DeleteMapping("deleteAttendant/{userID}")
    public ResponseEntity<Object> deleteAttendant(@PathVariable int userID) {
        return adminService.deleteAttendant(userID);
    }

    @GetMapping("getVehicleScheduleList")
    public ResponseEntity<Object> getVehicleScheduleList() {
        return adminService.getVehicleScheduleList();

    }

    @GetMapping("getVehicle_ID_RegisterationNumberList")
    public ResponseEntity<Object> getVehicle_ID_RegisterationNumberList() {
        return adminService.getVehicle_ID_RegisterationNumberList();

    }

    @DeleteMapping("deleteVehicleSchedule/{vehicleScheduleID}")
    public ResponseEntity<Object> deleteVehicleSchedule(@PathVariable int vehicleScheduleID) {
        return adminService.deleteVehicleSchedule(vehicleScheduleID);

    }

    @PutMapping("updateVehicle/{vehicleID}")
    public ResponseEntity<Object> updateVehicle(@PathVariable int vehicleID,
                                                @RequestBody VehicleDetailsDTO vehicleDetailsDTO) {
        return adminService.updateVehicle(vehicleID, vehicleDetailsDTO);
    }

    @DeleteMapping("deleteVehicle/{vehicleID}")
    public ResponseEntity<Object> deleteVehicle(@PathVariable int vehicleID) {
        return adminService.deleteVehicle(vehicleID);
    }

    @GetMapping("getVehicleList")
    public ResponseEntity<Object> getVehicleStatusList() {
        return adminService.getVehicleStatusList();

    }

    @GetMapping("getVehicleShedulesOfRoute/{routeID}")
    public ResponseEntity<Object> getVehicleShedulesOfRoute(@PathVariable int routeID) {
        return adminService.getVehicleShedulesOfRoute(routeID);

    }

    @PostMapping("assignVehicleScheduleToStudent/{userID}")
    public ResponseEntity<Object> assignVehicleScheduleToStudent(@PathVariable int userID,
                                                                 @RequestBody OnwardReturnVehicleScheduleID onward_Return_VehicleScheduleID) {
        return adminService.assignVehicleScheduleToStudent(userID, onward_Return_VehicleScheduleID);
    }

    @PutMapping("updateStudent/{userID}")
    public ResponseEntity<Object> updateStudent(@PathVariable int userID, @RequestBody PassengerDTO passengerDTO) {
        return adminService.updateStudent(userID, passengerDTO);
    }

    @PutMapping("updateStudentStopRoute/{userID}")
    public ResponseEntity<Object> updateStudentStopRoute(@PathVariable int userID,
                                                         @RequestBody StudentStopRouteUpdate studentStopRouteUpdate) {
        return adminService.updateStudentStopRoute(userID, studentStopRouteUpdate);
    }

    @PutMapping("updateDriver/{userID}")
    public ResponseEntity<Object> updateDriver(@PathVariable int userID, @RequestBody DriverDTO driverDTO) {
        return adminService.updateDriver(userID, driverDTO);
    }

    @PutMapping("updateAttendant/{userID}")
    public ResponseEntity<Object> updateAttendant(@PathVariable int userID, @RequestBody AttendantDTO attendantDTO) {
        return adminService.updateAttendant(userID, attendantDTO);
    }

    @GetMapping("getOnwardActiveRouteList")
    ResponseEntity<Object> getOnwardActiveRouteList() {
        return adminService.getOnwardActiveRouteList();

    }

    @GetMapping("getReturnActiveRouteList")
    ResponseEntity<Object> getReturnActiveRouteList() {
        return adminService.getReturnActiveRouteList();

    }

    @RequestMapping(value = "/fileUploadAPI", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> fileUpload(@RequestParam("multipartFile") MultipartFile multipartFile) {
        return adminService.fileUpload(multipartFile);
    }

    @GetMapping("getOnwardJourneyVehicleShedulesOfRoute/{routeID}")
    public ResponseEntity<Object> getOnwardJourneyVehicleShedulesOfRoute(@PathVariable int routeID) {
        return adminService.getVehicleSheduleIDNameWithPassengerCountOfGivenRouteForGivenTypeOfJourney(routeID,
                ONWARD_JOURNEY);

    }

    @GetMapping("getReturnJourneyVehicleShedulesOfRoute/{routeID}")
    public ResponseEntity<Object> getReturnJourneyVehicleShedulesOfRoute(@PathVariable int routeID) {
        return adminService.getVehicleSheduleIDNameWithPassengerCountOfGivenRouteForGivenTypeOfJourney(routeID,
                RETURN_JOURNEY);

    }

    @GetMapping("getReport/{date}/{typeOfJourney}")
    public ResponseEntity<Object> getReport(@PathVariable String date, @PathVariable int typeOfJourney) {
        return adminService.getReport(date, typeOfJourney);
    }

    @PostMapping("generateVehicleScheduleName")
    public ResponseEntity<Object> generateVehicleScheduleName(
            @RequestBody VehicleScheduleNameGeneration vehicleScheduleNameGeneration) {
        return adminService.generateVehicleScheduleName(vehicleScheduleNameGeneration);
    }

    @GetMapping("getCurrentAndStartEndLatLongOfActiveRoutes/{typeOfJourney}")
    public ResponseEntity<Object> getCurrentAndStartEndLatLongOfActiveRoutes(@PathVariable int typeOfJourney) {
        return adminService.getCurrentAndStartEndLatLongOfActiveRoutes(typeOfJourney);
    }

    @PutMapping("adminVerifiedVideoUploadedByGivenUser/{tripDetailsID}/{userID}")
    public ResponseEntity<Object> adminVerifiedVideoUploadedByGivenUser(@PathVariable int tripDetailsID,
                                                                        @PathVariable int userID) {
        return adminService.adminVerifiedVideoUploadedByGivenUser(tripDetailsID, userID);
    }

    @PutMapping("adminClickedOnVerifyAllVideos/{tripDetailsID}")
    public ResponseEntity<Object> adminClickedOnVerifyAllVideos(@PathVariable int tripDetailsID) {
        return adminService.adminClickedOnVerifyAllVideos(tripDetailsID);
    }

    @GetMapping("getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates/{roleID}/{vehicleScheduleID}")
    public ResponseEntity<Object> getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates(
            @PathVariable int roleID, @PathVariable int vehicleScheduleID) {
        return adminService.getGivenStaffRoleUsersListOfVehicleScheduleForCurrentAndFutureDates(roleID,
                vehicleScheduleID);
    }

    @GetMapping("getConfigurableParameters")
    public ResponseEntity<Object> getConfigurableParameters() {
        return adminService.getConfigurableParameters();
    }

    @PutMapping("setTimeOfNotifyingStaffForBusNotVerified/{notifyingStaffForBusNotVerified}")
    public ResponseEntity<Object> setTimeOfNotifyingStaffForBusNotVerified(
            @PathVariable int notifyingStaffForBusNotVerified) {
        return adminService.setTimeOfNotifyingStaffForBusNotVerified(notifyingStaffForBusNotVerified);
    }

    @PutMapping("setTimeBeforeWhichLeaveCanBeCanceled/{cancelLeaveBeforeTime}")
    public ResponseEntity<Object> setTimeBeforeWhichLeaveCanBeCanceled(@PathVariable int cancelLeaveBeforeTime) {
        return adminService.setTimeBeforeWhichLeaveCanBeCanceled(cancelLeaveBeforeTime);
    }

    @PutMapping("setBusDelayedTimeForNotifyingPassengers/{notifyingPassengersAboutBusDelayed}")
    public ResponseEntity<Object> setBusDelayedTimeForNotifyingPassengers(
            @PathVariable int notifyingPassengersAboutBusDelayed) {
        return adminService.setBusDelayedTimeForNotifyingPassengers(notifyingPassengersAboutBusDelayed);
    }

    @GetMapping("getNotifications/{roleID}")
    public ResponseEntity<Object> getNotifications(@PathVariable int roleID) {
        Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
                RequestAttributes.SCOPE_REQUEST);
        return adminService.getNotifications(userID, roleID);
    }

    @DeleteMapping("deleteNotification/{id}")
    public ResponseEntity<Object> deleteNotification(@PathVariable int id) {
        Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
                RequestAttributes.SCOPE_REQUEST);
        return adminService.deleteNotification(id, userID);
    }

    @GetMapping("getAdminProfile")
    public ResponseEntity<Object> getAdminProfile() {
        Integer userID = (Integer) RequestContextHolder.currentRequestAttributes().getAttribute(USER_ID,
                RequestAttributes.SCOPE_REQUEST);
        return adminService.getAdminProfile(userID);
    }

    @PostMapping("addRouteAndItsStops")
    public ResponseEntity<Object> addRouteAndItsStops(@RequestBody RouteStopsDetails routeStopsDetails) {
        return adminService.addRouteAndItsStops(routeStopsDetails);
    }

    @GetMapping("broadcastNotificationToPassengersOfVehicleSchedule/{vehicleScheduleID}/{mins}")
    public ResponseEntity<Object> broadcastNotificationToPassengersOfVehicleSchedule(
            @PathVariable int vehicleScheduleID, @PathVariable int mins) {
        return adminService.broadcastNotificationToPassengersOfVehicleSchedule(vehicleScheduleID, mins);
    }

    @PostMapping("addSchedule")
    public ResponseEntity<Object> addSchedule(@RequestBody Schedule schedule) {
        return adminService.addSchedule(schedule);
    }

    @PutMapping("updateRouteAndItsStops/{routeID}")
    public ResponseEntity<Object> updateRouteAndItsStops(@PathVariable int routeID,
                                                         @RequestBody RouteStops routeStops) {
        return adminService.updateRouteAndItsStops(routeID, routeStops);
    }

    @GetMapping("getRouteListOnRouteScreen")
    public ResponseEntity<Object> getRouteListOnRouteScreen() {
        return adminService.getRouteListOnRouteScreen();

    }

    @PutMapping("updateScheduledTimeAtStopsOfSchedule/{scheduleID}")
    public ResponseEntity<Object> updateScheduledTimeAtStopsOfSchedule(@PathVariable int scheduleID,
                                                                       @RequestBody ScheduleUpdate scheduleUpdate) {
        return adminService.updateScheduledTimeAtStopsOfSchedule(scheduleID, scheduleUpdate);
    }

    @GetMapping("getRouteDetails/{routeID}")
    public ResponseEntity<Object> getRouteDetails(@PathVariable int routeID) {
        return adminService.getRouteDetails(routeID);

    }

    @GetMapping("getScheduleDetails/{scheduleID}")
    public ResponseEntity<Object> getScheduleDetails(@PathVariable int scheduleID) {
        return adminService.getScheduleDetails(scheduleID);

    }

    @GetMapping("getAvailableResources/{scheduleID}/{startDate}/{endDate}")
    public ResponseEntity<Object> getAvailableResources(@PathVariable int scheduleID, @PathVariable String startDate,
                                                        @PathVariable String endDate) {
        return adminService.getAvailableResources(scheduleID, LocalDate.parse(startDate), LocalDate.parse(endDate));

    }

    @GetMapping("getStopsOrderForGivenRouteAndTypeOfJourney/{routeID}/{typeOfJourney}")
    public ResponseEntity<Object> getStopsOrderForGivenRouteAndTypeOfJourney(@PathVariable int routeID,
                                                                             @PathVariable int typeOfJourney) {
        return adminService.getStopsOrderForGivenRouteAndTypeOfJourney(routeID, typeOfJourney);

    }

    // TODO : email part remaining
    @PutMapping("generateTemporaryPassword/{userUniqueKey}")
    public ResponseEntity<Object> generateTemporaryPassword(@PathVariable String userUniqueKey) {
        return adminService.generateTemporaryPassword(userUniqueKey);
    }

    @GetMapping("getScheduleList/{month}/{year}")
    public ResponseEntity<Object> getScheduleList(@PathVariable int month, @PathVariable int year) {
        return adminService.getScheduleList(month, year);
    }

    @GetMapping("getResourcesAssignment/{scheduleID}/{month}/{year}")
    public ResponseEntity<Object> getResourcesAssignment(@PathVariable int scheduleID, @PathVariable int month,
                                                         @PathVariable int year) {
        return adminService.getResourcesAssignment(scheduleID, month, year);
    }

    @PutMapping("assignResources/{scheduleID}")
    public ResponseEntity<Object> assignResources(@PathVariable int scheduleID,
                                                  @RequestBody ResourcesAssignmentUpdation updatedResourcesAssignments) {
        return adminService.assignResources(scheduleID, updatedResourcesAssignments);
    }

    @GetMapping("proceedForResourcesAssignment/{month}/{year}")
    public ResponseEntity<Object> proceedForResourcesAssignment(@PathVariable int month, @PathVariable int year) {
        return adminService.proceedForResourcesAssignment(month, year);
    }

    // TODO: later on remove this api
    @PostMapping("refreshVideoURL")
    public ResponseEntity<Object> refreshVideoURL(@RequestBody VideoURL videoURL) {
        return adminService.refreshVideoURL(videoURL.getUrl());
    }

    @GetMapping("getDetailReport/{date}/{typeOfJourney}")
    public ResponseEntity<Object> getDetailReport(@PathVariable String date, @PathVariable int typeOfJourney) {
        return adminService.getDetailReport(date, typeOfJourney);
    }

    @GetMapping("getUnmappedMappedResourcesSchedulesList")
    public ResponseEntity<Object> getUnmappedMappedResourcesSchedulesList() {
        return adminService.getUnmappedMappedResourcesSchedulesList();
    }

    @GetMapping("getAPICount/{tripID}")
    public ResponseEntity<Object> getAPICount(@PathVariable int tripID) {
        return adminService.getAPICount(tripID);
    }

    @PostMapping("autocomplete")
    public ResponseEntity<ResponseWrapper<AutoCompleteAPIBodyStatusCodeHeaders>> autocomplete(@RequestBody GenericRequestDTO request) {
        log.debug("correlationid:, {}", request.getCorrelationId());
        ResponseWrapper output = new ResponseWrapper();
        output.setSuccess(true);
        output.setStatusCode(200);
        output.setMessage(null);
        output.setTime(LocalDateTime.now());
        output.setCorrelationId(request.getCorrelationId());
        output.setData(adminService.autocomplete(request.getSearchQuery(), request.getCorrelationId()));
        return ResponseEntity.ok(output);
    }
}
