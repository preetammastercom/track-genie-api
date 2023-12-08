package com.mastercom.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response1 {
	   
	   private boolean responseStatus;
	   private String responseMessage;
	   private Object data;
	
}
