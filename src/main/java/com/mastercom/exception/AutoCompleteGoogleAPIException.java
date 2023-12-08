package com.mastercom.exception;

import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
public class AutoCompleteGoogleAPIException extends RuntimeException {
    private final transient ResponseEntity<Object> responseEntityObject;
    private final String correlationId;

    public AutoCompleteGoogleAPIException(ResponseEntity<Object> responseEntityObject, String correlationId) {
        this.responseEntityObject = responseEntityObject;
        this.correlationId = correlationId;
    }


}
