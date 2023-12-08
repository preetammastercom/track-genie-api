package com.mastercom.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mastercom.client.SMSClientHelper;

import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class SMSService {

    @Autowired
    SMSClientHelper clientHelper;

    public String sendOTPMessage(String mobileNumber){
        String otp = getRandomNumberString();
//        Mono<String> stringMono = clientHelper.sendOtp(mobileNumber, otp);
//        String res = stringMono.block();
        return otp;
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
       // return String.format("%06d", number);
        return "999999";
    }
}
