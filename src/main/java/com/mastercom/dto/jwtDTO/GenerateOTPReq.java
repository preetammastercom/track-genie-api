package com.mastercom.dto.jwtDTO;

import lombok.Data;

@Data
public class GenerateOTPReq {
    String mobileNumber;
    Integer retryCount;
}
