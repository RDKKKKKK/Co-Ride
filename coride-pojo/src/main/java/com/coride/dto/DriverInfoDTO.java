package com.coride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverInfoDTO {
    private String accountType;

    private String messageType;

    private String driverName;

    private String carType;

    private Integer seats;

    private String matchId;
}
