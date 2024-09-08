package com.coride.dto;

import lombok.Data;

@Data
public class CarpoolerInfoDTO {

    private String originName;

    private String destinationName;

    private String messageType;

    private String passengerName;

    private String matchId;

    private String accountType;
}
