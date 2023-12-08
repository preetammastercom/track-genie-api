package com.mastercom.service;

import com.mastercom.config.ConfigurableParameters;
import com.mastercom.dao.AdminDao;
import com.mastercom.entity.User;
import com.mastercom.handler.ResponseHandler;
import com.mastercom.dto.jwtDTO.AuthRequest;
import com.mastercom.dto.jwtDTO.AuthResponse;
import com.mastercom.dto.jwtDTO.GenerateOTPReq;
import com.mastercom.repository.UserRepository;

import com.mastercom.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mastercom.constant.ApplicationConstant.DEVICE_ID;
import static com.mastercom.constant.ApplicationConstant.ADMIN_ROLE_ID;
import static com.mastercom.constant.ApplicationConstant.PASSENGER_ROLE_ID;;

@Service
public class AuthenticationService {

	@Autowired
	SMSService smsService;

	@Autowired
	JwtService jwtService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AdminService adminService;
	@Autowired
	PassengerService passengerService;
@Autowired
private EncryptionUtil encryptionUtil;
	@Autowired
	AdminDao adminDao;

	@Autowired
	ConfigurableParameters configurableParameters;
	private static final Logger logger = LogManager.getLogger(AuthenticationService.class);

	private static final String SERVER_ERROR = "Server Error!!!";

	public ResponseEntity<Object> triggerOTP(GenerateOTPReq req) {

		User byUserPhoneNumber = userRepository.findByUserPhoneNumber(Long.valueOf(req.getMobileNumber()));
		if (byUserPhoneNumber == null) {
			return ResponseHandler.generateResponse2(false, "Mobile number is not registered!", HttpStatus.NOT_FOUND);

		}
		String otp = smsService.sendOTPMessage(req.getMobileNumber());
		byUserPhoneNumber.setOtp(Integer.valueOf(otp));
		byUserPhoneNumber.setOtpGenerationDateTime(LocalDateTime.now());
		userRepository.save(byUserPhoneNumber);
		return ResponseHandler.generateResponse2(true, "Success!", HttpStatus.OK);

	}

	public ResponseEntity<Object> authenticateRequest(AuthRequest authRequest) {
		String authRequestPassword = authRequest.getPassword();
		User user = userRepository.findByUserUniqueKey(authRequest.getUserUniqueKey());
		boolean showPasswordSettingScreen = false;
		boolean authenticatedUser = false;
		System.err.println(user.getTempPassword());
		if ((user != null) && (user.getRoles().stream()
				.anyMatch(role -> Objects.equals(role.getRoleID(), authRequest.getRoleID())))) {
			if ((user.getPassword() != null) && (authRequestPassword.equals(encryptionUtil.decrypt(user.getPassword())))) {
				authenticatedUser = true;
			} else if ((user.getTempPassword() != null) && ((encryptionUtil.decrypt(user.getTempPassword())).equals(authRequestPassword))) {
				if (user.getTempPasswordCreationTimeStamp()
						.plusSeconds(configurableParameters.getTemporaryPasswordExpiry() * 60)
						.isAfter(LocalDateTime.now())) {
					authenticatedUser = true;
					showPasswordSettingScreen = true;
				} else {
					logger.debug("Password expired!");
					return ResponseHandler.generateResponse2(false, "Password expired!", HttpStatus.OK);
				}
			}
		}
		if (authenticatedUser) {
//TODO : it may need in future...

//		if ((user != null) && (user.getOtp() != null)
//				&& (user.getRoles().stream()
//						.anyMatch(role -> Objects.equals(role.getRoleID(), authRequest.getRoleID())))
//				&& (user.getOtp().equals(Integer.valueOf(authRequest.getOtp())))
//				&& (user.getUserUniqueKey().equals(authRequest.getUserUniqueKey()))
//				&& (Objects.equals(authRequest.getRoleID(), ADMIN_ROLE_ID)
//						? Objects.equals(authRequest.getPassword(), user.getPassword())
//						: true)) {
//			LocalDateTime otpGenertionDateTime = user.getOtpGenerationDateTime();
//			if (!((otpGenertionDateTime.plusSeconds(configurableParameters.getOtpExpireTime() * 60)
//					.isAfter(currentDateTime))
//					|| (otpGenertionDateTime.plusSeconds(configurableParameters.getOtpExpireTime() * 60)
//							.isEqual(currentDateTime)))) {
//				return ResponseHandler.generateResponse2(false, "OTP Expired", HttpStatus.OK);
//
//			}
			if (authRequest.getFcmToken().equals("")) {
				return ResponseHandler.generateResponse2(false, "Incorrect data format", HttpStatus.OK);
			}

			Map<String, Object> extraClaims = new LinkedHashMap<>();

			extraClaims.put(DEVICE_ID, authRequest.getDeviceID());

			String token = jwtService.generateToken(extraClaims, user);

			if (adminService.saveTokensAndDeviceID(user, token, authRequest.getFcmToken(), authRequest.getDeviceID(), showPasswordSettingScreen)) {
				logger.debug("User Authentication Successful");
				return ResponseHandler.generateResponse1(true, "User Authentication Successful", HttpStatus.OK,
						new AuthResponse(user, token, showPasswordSettingScreen));
			} else {
				return ResponseHandler.generateResponse2(false, SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.debug("Invalid User Credentials");
			return ResponseHandler.generateResponse2(false, "Invalid User Credentials", HttpStatus.UNAUTHORIZED);
		}
	}

	public ResponseEntity<Object> logout(Integer userID) {
		String fcmToken = adminDao.logout(userID);
		User user = adminDao.getUser(userID);
		if (fcmToken != null) {
			if ((user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList()))
					.contains(PASSENGER_ROLE_ID)) {
				passengerService.unsubscribeFcmTokenOfPassengerFromTopicOnLogout(userID, fcmToken);
			}
			else if((user.getRoles().stream().map(role -> role.getRoleID()).collect(Collectors.toList()))
					.contains(ADMIN_ROLE_ID)) {
				adminService.unsubscribeFcmTokenOfAdminFromTopicOnLogout( fcmToken);
			}
			return ResponseHandler.generateResponse2(true, "Logout successful!!!", HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse2(false, "Server Error!!!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
