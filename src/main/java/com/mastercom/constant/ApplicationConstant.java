package com.mastercom.constant;

public class ApplicationConstant {

	//common status codes
	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
	public static final String INVALID_INPUT_DATA = "INVALID_INPUT_DATA";
	public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
	
	//common status messages
	public static final String FORBIDDEN = "FORBIDDEN";
	public static final String UNEXPECTED_ERROR = "UNEXPECTED_ERROR";
	public static final String UNPROCESSABLE_ENTITY = "UNPROCESSABLE_ENTITY";
	public static final String PRECONDITION_FAILED = "PRECONDITION_FAILED";
    public static final String UNAUTHORIZED_ACCESS_MSG = "Access Is Unauthorized";
    public static final String FORBIDDEN_MSG = "Resource Access Is Forbidden";
    public static final String INVALID_OR_MISSING_AUTH_TOKEN="Invalid Or Missing Auth Token";
    
    //roleIDs constants
    public static final Integer ADMIN_ROLE_ID=1;
    public static final Integer TEACHER_ROLE_ID=2;
    public static final Integer PASSENGER_ROLE_ID=3;
    public static final Integer DRIVER_ROLE_ID=4;
    public static final Integer ATTENDANT_ROLE_ID=5;
    
    //type of journey constants
    public static final Integer ONWARD_JOURNEY=1;
    public static final Integer RETURN_JOURNEY=2;
    
    //Passenger Status Code
    public static final Integer PICKED_UP=1;
    public static final Integer DROPPED=2;
    public static final Integer MISSED_BUS=3;
    public static final Integer SCHEDULED_LEAVE=4;
    
    //Trip passenger status
    public static final String PICKED="Picked";
    public static final String In_BUS="In Bus";
    
    //user constants
    public static final String DEVICE_ID="DeviceID";
    
    //FCM topic constant
    public static final String SCHEDULE_TOPIC="Track_Genie_ScheduleId_";
    public static final String TEST_TOPIC="WORLD";
    public static final String PASSENGER_SCHEDULE_TOPIC="Track_Genie_Passenger_ScheduleId_";
    public static final String ADMIN_TOPIC="Track_Genie_Admin";
    //FCM data message constants
    public static final String REFRESH_DATA_MESSAGE="refresh";
    public static final String REFRESH_LAT_lONG_DATA_MESSAGE="refreshLatLong";
    public static final String LAT_DATA_MESSAGE="lat";
    public static final String LONG_DATA_MESSAGE="long";
    
    //passenger homescreen: passenger status
    public static final String PASSENGER_ONBOARDED = "Child onboarded.";
    public static final String PASSENGER_DROPPED = "Child dropped.";
    public static final String PASSENGER_MISSED_BUS = "Child missed bus.";
    public static final String PASSENGER_LEAVE = "Child on scheduled leave.";
    public static final String PASSENGER_NOT_BOARDED = "Child not boarded.";
    public static final String TRIP_NOT_STARTED = "Trip is not started.";
    
    //passenger homescreen "your location" message
    public static final String YOUR_LOCATION="Your Location";
    
    // request key constant
    public static final String USER_ID="userID"; 
    
    // Detail report : passenger status
    public static final String PICKED_UP_STATUS="Picked up";
    public static final String DROPPED_STATUS="Dropped";
    public static final String MISSED_BUS_STATUS="Missed bus";
    public static final String SCHEDULED_LEAVE_STATUS="Scheduled leave";
    public static final String YET_TO_BE_PICKED__STATUS="Yet to be Picked";
    
}
