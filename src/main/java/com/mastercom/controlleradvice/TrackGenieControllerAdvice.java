package com.mastercom.controlleradvice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercom.dto.AutoCompleteAPIBodyStatusCodeHeaders;
import com.mastercom.dto.response.ResponseWrapper;
import com.mastercom.exception.AutoCompleteGoogleAPIException;
import com.mastercom.handler.ResponseHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;


@Slf4j
@ControllerAdvice
public class TrackGenieControllerAdvice extends ResponseEntityExceptionHandler {
    private ObjectMapper mapper = new ObjectMapper();

//	 @ExceptionHandler(MethodArgumentNotValidException.class)
//	    public ResponseEntity<Object> handleValidationExceptions(
//	      MethodArgumentNotValidException ex) {
//	    	String str="";
//	    	Map<String, String> errors = new HashMap<>();
//	        ex.getBindingResult().getAllErrors().forEach((error) -> {
//	            String fieldName = ((FieldError) error).getField();
//	            String errorMessage = error.getDefaultMessage();
//	            errors.put(fieldName, errorMessage);
//	        });
//	        for (String key : errors.keySet()) {
//				str=str+key+" -> " +errors.get(key)+".    ";
//			}
//	        log.error("TrackGenieControllerAdvice >>> handleGenericException >>> Exception => ",ex);
//	        return ResponseHandler.generateResponse2(false,str , HttpStatus.BAD_REQUEST);
//	    }


    //TODO : commented for now
//	@Override
//	    public ResponseEntity<Object> handleMethodArgumentNotValid(
//				MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
//	    	String str="";
//	    	Map<String, String> errors = new HashMap<>();
//	        ex.getBindingResult().getAllErrors().forEach((error) -> {
//	            String fieldName = ((FieldError) error).getField();
//	            String errorMessage = error.getDefaultMessage();
//	            errors.put(fieldName, errorMessage);
//	        });
//	        for (String key : errors.keySet()) {
//				str=str+key+" -> " +errors.get(key)+"    ";
//			}
//	        log.error("TrackGenieControllerAdvice >>> handleMethodArgumentNotValidException >>> Exception => ",ex);
//	        return ResponseHandler.generateResponse2(false,str , HttpStatus.BAD_REQUEST);
//	    }

    @SneakyThrows
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception) {
        log.error("TrackGenieControllerAdvice >>> handleGenericException >>> Exception => ", exception);
        return ResponseHandler.generateResponse2(false, "Server Error!!!", HttpStatus.INTERNAL_SERVER_ERROR);
//	        return ResponseEntity
//	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//	                .body(ErrorResponse.builder().statusCode(UNEXPECTED_ERROR)
//	                        .build());
    }

    @SneakyThrows
    @ExceptionHandler(AutoCompleteGoogleAPIException.class)
    public ResponseEntity<Object> handleAutoCompleteGogleAPIException(AutoCompleteGoogleAPIException exception) {
        log.error("TrackGenieControllerAdvice >>> handleAutoCompleteGoogleAPIException >>> Exception => ", exception);
        AutoCompleteAPIBodyStatusCodeHeaders autoCompleteAPIBodyStatusCodeHeaders = new AutoCompleteAPIBodyStatusCodeHeaders();
        autoCompleteAPIBodyStatusCodeHeaders.setApiBody(exception.getResponseEntityObject().getBody());
        autoCompleteAPIBodyStatusCodeHeaders.setHttpHeaders(exception.getResponseEntityObject().getHeaders());
        autoCompleteAPIBodyStatusCodeHeaders.setHttpStatusCode(exception.getResponseEntityObject().getStatusCode().value());
        ResponseWrapper<AutoCompleteAPIBodyStatusCodeHeaders> responseWrapper = new ResponseWrapper<AutoCompleteAPIBodyStatusCodeHeaders>();
        responseWrapper.setSuccess(true);
        responseWrapper.setStatusCode(200);
        responseWrapper.setMessage(null);
        responseWrapper.setTime(LocalDateTime.now());
        responseWrapper.setCorrelationId(exception.getCorrelationId());
        responseWrapper.setData(autoCompleteAPIBodyStatusCodeHeaders);
        return ResponseEntity.ok(responseWrapper);
    }
}
