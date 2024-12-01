package com.coride.abstraction;

import com.coride.dto.CarpoolerInfoDTO;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.implementor.CarpoolerRole;
import com.coride.implementor.DriverRole;
import com.coride.implementor.Role;
import com.coride.observer.WebSocketObserver;
import com.coride.observer.WebSocketSessionObserver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;

import java.io.IOException;

import static com.coride.RideWebSocketServer.sessionMap;

@Slf4j
@AllArgsConstructor
public class ConfirmMessage extends Message{

    public RideMatchConfirmDTO rideMatchConfirmDTO;

    @Override
    public void processMessage() throws IOException {

        String id;

        if(rideMatchConfirmDTO.getRoleType().equals("Driver")) {
            role = new DriverRole(rideMatchConfirmDTO);
            id = "D" + rideMatchConfirmDTO.getDriverId();
        }
        else{
            role = new CarpoolerRole(rideMatchConfirmDTO);
            id = "C" + rideMatchConfirmDTO.getDriverId();
        }

        String message = role.sendConfirmation();
        Session session = sessionMap.get(id);

        if (session != null) {
            WebSocketObserver observer = new WebSocketSessionObserver(session);
            this.addObserver(observer);
        }

        notifyObservers(message);

        log.info("Confirmation sent!");
    }
}
