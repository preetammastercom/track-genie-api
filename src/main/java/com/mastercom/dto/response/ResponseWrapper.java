package com.mastercom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWrapper<T> {
    private Boolean success;
    private Integer statusCode;
    private String message;
    private LocalDateTime time;
    private String correlationId;
    private T data;
}
