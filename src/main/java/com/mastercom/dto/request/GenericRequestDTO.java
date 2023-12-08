package com.mastercom.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data

public class GenericRequestDTO extends RequestWrapper{
    private Integer tripID;
    private Integer typeOfJourney;
    private String date;
    private String searchQuery;
}
