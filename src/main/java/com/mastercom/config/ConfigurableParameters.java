package com.mastercom.config;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "min")
public class ConfigurableParameters {

	private int notifyingStaffForBusNotVerified;
	private int cancelLeaveBeforeTime;
	private int notifyingPassengersAboutBusDelayed;
	private int otpExpireTime;
	private int temporaryPasswordExpiry;
	
	

	
	public LinkedHashMap< String, Integer> getAllConfigurableParameters() {
		LinkedHashMap< String, Integer> hm=new LinkedHashMap<>();
		hm.put("notifyingStaffForBusNotVerified", getNotifyingStaffForBusNotVerified());
		hm.put("cancelLeaveBeforeTime", getCancelLeaveBeforeTime());
		hm.put("notifyingPassengersAboutBusDelayed", getNotifyingPassengersAboutBusDelayed());
		hm.put("otpExpiretime", getOtpExpireTime());
		hm.put("temporaryPasswordExpiry", getTemporaryPasswordExpiry());
		return hm;
	}

	public int getNotifyingStaffForBusNotVerified() {
		return notifyingStaffForBusNotVerified;
	}

	public void setNotifyingStaffForBusNotVerified(int notifyingStaffForBusNotVerified) {
		this.notifyingStaffForBusNotVerified = notifyingStaffForBusNotVerified;
	}

	public int getCancelLeaveBeforeTime() {
		return cancelLeaveBeforeTime;
	}

	public void setCancelLeaveBeforeTime(int cancelLeaveBeforeTime) {
		this.cancelLeaveBeforeTime = cancelLeaveBeforeTime;
	}

	public int getNotifyingPassengersAboutBusDelayed() {
		return notifyingPassengersAboutBusDelayed;
	}

	public void setNotifyingPassengersAboutBusDelayed(int notifyingPassengersAboutBusDelayed) {
		this.notifyingPassengersAboutBusDelayed = notifyingPassengersAboutBusDelayed;
	}

	public int getOtpExpireTime() {
		return otpExpireTime;
	}

	public void setOtpExpireTime(int otpExpireTime) {
		this.otpExpireTime = otpExpireTime;
	}

	public int getTemporaryPasswordExpiry() {
		return temporaryPasswordExpiry;
	}

	public void setTemporaryPasswordExpiry(int temporaryPasswordExpiry) {
		this.temporaryPasswordExpiry = temporaryPasswordExpiry;
	}

}
