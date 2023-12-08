package com.mastercom.dto.jwtDTO;

import com.mastercom.entity.User;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
	
	private User user;
	private String jwtToken;
	private boolean showPasswordSettingScreen;
//    String status;
//    String message;
}
