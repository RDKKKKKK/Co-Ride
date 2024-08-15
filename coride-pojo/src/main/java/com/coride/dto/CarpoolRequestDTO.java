package com.coride.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CarpoolRequestDTO implements Serializable {
    private Double originLongitude;

    private Double originLatitude;

    private Double destinationLongitude;

    private Double destinationLatitude;

    private String token;

    private String accountType;

    private String messageType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;

}
