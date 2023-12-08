package com.mastercom.fcm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;

import lombok.extern.slf4j.Slf4j;

import static com.mastercom.constant.ApplicationConstant.*;

@Service
@Slf4j
public class FirebaseMessagingService {

	private final FirebaseMessaging firebaseMessaging;

	public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {

		this.firebaseMessaging = firebaseMessaging;

	}

	public void sendNotification(Note note, String token) {
		Notification notification = Notification.builder().setTitle(note.getSubject()).setBody(note.getContent())
				.build();
		Message message = Message.builder().setToken(token).setNotification(notification).build();
		log.debug("message is created.");
		firebaseMessaging.sendAsync(message);
	}
	
	// This method is to be used for tokens count less than or equal to 500.
	public void sendMulticastAsync(Note note, List<String> registrationTokens) {
		if(registrationTokens.isEmpty()) {
			return ;
		}
		log.debug("Inside method");
		Notification notification = Notification.builder().setTitle(note.getSubject()).setBody(note.getContent())
				.build();
		MulticastMessage message = MulticastMessage.builder().addAllTokens(registrationTokens)
				.setNotification(notification).build();
		firebaseMessaging.sendMulticastAsync(message);
	}

	// This method is to be used for tokens count less than or equal to 500.
	public int sendMulticast(Note note, List<String> registrationTokens) throws FirebaseMessagingException {
		
		Notification notification = Notification.builder().setTitle(note.getSubject()).setBody(note.getContent())
				.build();
		MulticastMessage message = MulticastMessage.builder().addAllTokens(registrationTokens)
				.setNotification(notification).build();
		BatchResponse response = firebaseMessaging.sendMulticast(message);
		
		return response.getSuccessCount();
	}

	
	
	

	public String notifyMultiple2(Note note, List<String> registrationTokens) {
		log.debug("Inside method");
		int totalTokens = registrationTokens.size();
		int n = totalTokens;
		int success = 0;
		while (n != 0) {
			List<String> list500 = registrationTokens.stream().limit(500).collect(Collectors.toList());
			registrationTokens.removeAll(list500);
			int c = 0;
			try {
				c = sendMulticast(note, list500);
			} catch (FirebaseMessagingException e) {
				e.printStackTrace();
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				String s = errors.toString();
				log.error("Exception =>  " + s);
			}
			success = success + c;
			n = registrationTokens.size();

		}
		String output;
		if (totalTokens > 0) {
			if (totalTokens == success) {
				output = "Notification sent to all users.";
			} else {
				if (success > 0) {
					output = "Sending Notification Failed for some users.";
				} else {
					output = "Sending Notification Failed for all users.";
				}

			}
		} else {
			output = "Total tokens count is zero. So, no notification sent.";
		}
		return output;

	}

	public void subscribeToTopic(String topic, String fcmToken) {
		firebaseMessaging.subscribeToTopicAsync(Arrays.asList(fcmToken), topic);
	}

	public void unsubscribeFromTopic(String topic, String fcmToken) {
		firebaseMessaging.unsubscribeFromTopicAsync(Arrays.asList(fcmToken), topic);
	}

	public void sendMessageToTopic(String topic, String key, String value) {
		Message message = Message.builder().putData(key, value)
//				.setNotification(Notification.builder()
//				        .setTitle("Test message")
//				        .setBody("This is test message!!!")
//				        .build())
				.setTopic(topic).build();
		firebaseMessaging.sendAsync(message);
	}

	public void sendRefreshMessageToScheduleTopicAndAdminTopic(int vehicleScheduleID) {
		sendMessageToTopic(SCHEDULE_TOPIC + vehicleScheduleID, REFRESH_DATA_MESSAGE, REFRESH_DATA_MESSAGE);
		sendMessageToTopic(ADMIN_TOPIC, REFRESH_DATA_MESSAGE, REFRESH_DATA_MESSAGE);
	}

	public void subscribeToScheduleTopic(int vehicleScheduleID, String fcmToken) {
		subscribeToTopic(SCHEDULE_TOPIC + vehicleScheduleID, fcmToken);
	}

	public void unsubscribeFromScheduleTopic(int vehicleScheduleID, String fcmToken) {
		unsubscribeFromTopic(SCHEDULE_TOPIC + vehicleScheduleID, fcmToken);
	}

	public void sendLatLongRefreshMessageToPassengerScheduleTopic(int vehicleScheduleID, String latitude,
			String longitude) {
		Message message = Message.builder().putData(REFRESH_LAT_lONG_DATA_MESSAGE, REFRESH_LAT_lONG_DATA_MESSAGE)
				.putData(LAT_DATA_MESSAGE, latitude).putData(LONG_DATA_MESSAGE, longitude)
				.setTopic(PASSENGER_SCHEDULE_TOPIC + vehicleScheduleID).build();
		firebaseMessaging.sendAsync(message);
	}

	public void subscribeToPassengerScheduleTopic(int vehicleScheduleID, String fcmToken) {
		subscribeToTopic(PASSENGER_SCHEDULE_TOPIC + vehicleScheduleID, fcmToken);
	}

	public void unsubscribeFromPassengerScheduleTopic(int vehicleScheduleID, String fcmToken) {
		unsubscribeFromTopic(PASSENGER_SCHEDULE_TOPIC + vehicleScheduleID, fcmToken);
	}

	public void sendRefreshMessageToAdminTopic() {
		sendMessageToTopic(ADMIN_TOPIC, REFRESH_DATA_MESSAGE, REFRESH_DATA_MESSAGE);
	}

	public void subscribeToAdminTopic(String fcmToken) {
		subscribeToTopic(ADMIN_TOPIC, fcmToken);
	}

	public void unsubscribeFromAdminTopic(String fcmToken) {
		unsubscribeFromTopic(ADMIN_TOPIC, fcmToken);
	}

	public void sendRefreshMessageToToken(String fcmToken) {
		sendMessageToToken(fcmToken, REFRESH_DATA_MESSAGE, REFRESH_DATA_MESSAGE);
	}

	private void sendMessageToToken(String fcmToken, String key, String value) {
		Message message = Message.builder().putData(key, value).setToken(fcmToken).build();
		firebaseMessaging.sendAsync(message);
	}
}
