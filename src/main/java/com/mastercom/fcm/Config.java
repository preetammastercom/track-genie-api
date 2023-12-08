package com.mastercom.fcm;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class Config {
	
	@Value("${adminSDKfileName}")
	private String adminSDKfileName;

	private static final Logger logger = LogManager.getLogger(Config.class);

	
	@Bean
	FirebaseMessaging firebaseMessaging() throws IOException {
		
		
//		GoogleCredentials googleCredentials = GoogleCredentials
//        .fromStream(new FileInputStream("/home/TrackGenieServer/IMP/track-genie-78ec0-firebase-adminsdk-gbj3h-e8b2c0aa6b.json"));
//		
		
		
		
//	    GoogleCredentials googleCredentials = GoogleCredentials
//	            .fromStream(new FileInputStream("E:/IMP/track-genie-78ec0-firebase-adminsdk-gbj3h-e8b2c0aa6b.json"));
		
	    GoogleCredentials googleCredentials = GoogleCredentials
        .fromStream(new ClassPathResource(adminSDKfileName).getInputStream());

	    FirebaseOptions firebaseOptions = FirebaseOptions
	            .builder()
	            .setCredentials(googleCredentials)
	            .build();
	    FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "my-app");
	   logger.info(app +" is initilized.");
	    return FirebaseMessaging.getInstance(app);
	}
	
	
	

	
	
}
