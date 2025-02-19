package com.coride.dto;

import com.coride.entity.Driver;
import com.coride.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideMatchConfirmDTO implements Serializable {
    private String roleType;

    private Long driverId;

    private Long carpoolerId;

    private Driver driver;

    private String plateNo;

    private String matchId;

    private String carpoolerName;

    private String originName;

    private String destinationName;

    private Vehicle vehicle;
}
