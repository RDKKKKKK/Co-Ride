package com.coride.implementor;

import com.coride.dto.CarpoolerInfoDTO;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.websocket.Session;
import java.io.IOException;
import static com.coride.RideWebSocketServer.sessionMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverRole implements Role{

    RideMatchConfirmDTO rideMatchConfirmDTO;
    RideMatchResultDTO rideMatchResultDTO;

    public DriverRole(RideMatchConfirmDTO rideMatchConfirmDTO) {
        this.rideMatchConfirmDTO = rideMatchConfirmDTO;
    }


    @Override
    public String sendConfirmation() throws IOException {
        CarpoolerInfoDTO carpoolerInfoDTO = new CarpoolerInfoDTO();

        carpoolerInfoDTO.setAccountType("Driver");
        carpoolerInfoDTO.setMessageType("rideConfirm");
        carpoolerInfoDTO.setMatchId(rideMatchConfirmDTO.getMatchId());
        carpoolerInfoDTO.setOriginName(rideMatchConfirmDTO.getOriginName());
        carpoolerInfoDTO.setDestinationName(rideMatchConfirmDTO.getDestinationName());
        carpoolerInfoDTO.setPassengerName(rideMatchConfirmDTO.getCarpoolerName());

        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(carpoolerInfoDTO);

        return message;
    }

    @Override
    public String sendResult() throws IOException {
        return null;
    }
}
