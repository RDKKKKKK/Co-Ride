package com.coride.abstraction;

import com.alibaba.fastjson.JSONObject;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.observer.WebSocketSessionObserver;
import lombok.AllArgsConstructor;

import javax.websocket.Session;
import java.io.IOException;

import static com.coride.RideWebSocketServer.sessionMap;


@AllArgsConstructor
public class ResultMessage extends Message{

    public RideMatchResultDTO rideMatchResultDTO;

    @Override
    public void processMessage() throws IOException {

        for (Long id : rideMatchResultDTO.getCarpoolerIds()){
            String passengerSessionId = "C" + id;
            Session passengerSession = sessionMap.get(passengerSessionId);
            if (passengerSession == null) return;
            this.addObserver(new WebSocketSessionObserver(passengerSession));
        }

        if (rideMatchResultDTO.getResultType().equals("Success")){
            String driverSessionId = "D" + rideMatchResultDTO.getDriverId();
            Session driverSession = sessionMap.get(driverSessionId);
            if (driverSession == null) return;

            JSONObject driverMessage = new JSONObject();
            driverMessage.put("messageType", "carpoolSuccess");
            driverMessage.put("accountType", "Driver");

            driverSession.getAsyncRemote().sendText(driverMessage.toString());


            JSONObject passengerMessage = new JSONObject();
            passengerMessage.put("messageType", "carpoolSuccess");
            passengerMessage.put("accountType", "Carpooler");

            notifyObservers(passengerMessage.toString());
        }

        else if (rideMatchResultDTO.getResultType().equals("Failure")){
            if (rideMatchResultDTO.getRoleType().equals("Both")) {
                String driverSessionId = "D" + rideMatchResultDTO.getDriverId();
                Session driverSession = sessionMap.get(driverSessionId);
                if (driverSession == null) return;

                JSONObject driverMessage = new JSONObject();
                driverMessage.put("messageType", "carpoolFailure");
                driverMessage.put("accountType", "Driver");

                driverSession.getAsyncRemote().sendText(driverMessage.toString());
            }

            JSONObject passengerMessage = new JSONObject();
            passengerMessage.put("messageType", "carpoolFailure");
            passengerMessage.put("accountType", "Carpooler");

            notifyObservers(passengerMessage.toString());
        }

    }
}
