package com.mastercom.dto.jwtDTO;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private Boolean success;
    private String journeyId;
    private String time;
    private List<ErrorDetail> errorDetails;
    private String statusCode;
    private String message;
}
