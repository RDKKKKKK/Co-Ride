package com.coride.implementor;

import com.coride.dto.DriverInfoDTO;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.Vehicle;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.Session;
import java.io.IOException;


public class CarpoolerRole implements Role{

    RideMatchConfirmDTO rideMatchConfirmDTO;
    RideMatchResultDTO rideMatchResultDTO;

    public CarpoolerRole(RideMatchConfirmDTO rideMatchConfirmDTO){
        this.rideMatchConfirmDTO = rideMatchConfirmDTO;
    }

    @Override
    public String sendConfirmation() throws IOException {
        DriverInfoDTO driverInfoDTO = new DriverInfoDTO();
        Vehicle vehicle = rideMatchConfirmDTO.getVehicle();

        driverInfoDTO.setMessageType("rideConfirm");
        driverInfoDTO.setAccountType("Carpooler");
        driverInfoDTO.setDriverName(rideMatchConfirmDTO.getDriver().getName());
        driverInfoDTO.setSeats(vehicle.getSeats());
        driverInfoDTO.setCarType(vehicle.getName());
        driverInfoDTO.setMatchId(rideMatchConfirmDTO.getMatchId());

        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(driverInfoDTO);

        return message;
    }

    @Override
    public String sendResult() throws IOException {
        return null;
    }
}
