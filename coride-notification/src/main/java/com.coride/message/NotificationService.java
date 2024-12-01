package com.coride.message;

import com.alibaba.fastjson.JSONObject;
import com.coride.abstraction.ConfirmMessage;
import com.coride.abstraction.ResultMessage;
import com.coride.dto.CarpoolerInfoDTO;
import com.coride.dto.DriverInfoDTO;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.Vehicle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.websocket.Session;

import java.io.IOException;

import static com.coride.RideWebSocketServer.sessionMap;

@Service
@Slf4j
public class NotificationService {

    // 监听匹配确认通知
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "matchConfirmNotificationQueue", durable = "true"),
            exchange = @Exchange(name = "ride2NotificationExchange", type = ExchangeTypes.TOPIC),
            key = "notification.confirm"
    ))
    public void processMatchConfirmNotification(RideMatchConfirmDTO rideMatchConfirmDTO) throws IOException {
        // 处理匹配确认通知逻辑
        ConfirmMessage confirmMessage = new ConfirmMessage(rideMatchConfirmDTO);
        confirmMessage.processMessage();
        }

        /*
        String id;

        if (rideMatchConfirmDTO.getRoleType().equals("Driver")) {
            id = "D" + rideMatchConfirmDTO.getDriverId();

            CarpoolerInfoDTO carpoolerInfoDTO = new CarpoolerInfoDTO();

            carpoolerInfoDTO.setAccountType("Driver");
            carpoolerInfoDTO.setMessageType("rideConfirm");
            carpoolerInfoDTO.setMatchId(rideMatchConfirmDTO.getMatchId());
            carpoolerInfoDTO.setOriginName(rideMatchConfirmDTO.getOriginName());
            carpoolerInfoDTO.setDestinationName(rideMatchConfirmDTO.getDestinationName());
            carpoolerInfoDTO.setPassengerName(rideMatchConfirmDTO.getCarpoolerName());

            ObjectMapper mapper = new ObjectMapper();
            String message = mapper.writeValueAsString(carpoolerInfoDTO);

            Session session = sessionMap.get(id);
            if (session == null) return;
            session.getAsyncRemote().sendText(message);
            log.info("Confirmation sent to driver");
        }

        //Carpooler Message
        else{
            id = "C" + rideMatchConfirmDTO.getCarpoolerId();

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

            Session session = sessionMap.get(id);
            if (session == null) return;
            session.getAsyncRemote().sendText(message);
            log.info("Confirmation sent to carpooler");*/

    // 监听匹配结果通知
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "matchResultNotificationQueue", durable = "true"),
            exchange = @Exchange(name = "ride2NotificationExchange", type = ExchangeTypes.TOPIC),
            key = "notification.result"
    ))
    public void processMatchResultNotification(RideMatchResultDTO rideMatchResultDTO) throws IOException {
        ResultMessage resultMessage = new ResultMessage(rideMatchResultDTO);
        resultMessage.processMessage();
    }
}


        /* 处理匹配结果通知逻辑

        if (rideMatchResultDTO.getResultType().equals("Success")){
            String driverSessionId = "D" + rideMatchResultDTO.getDriverId();
            Session driverSession = sessionMap.get(driverSessionId);
            if (driverSession == null) return;


            JSONObject driverMessage = new JSONObject();
            driverMessage.put("messageType", "carpoolSuccess");
            driverMessage.put("accountType", "Driver");

            driverSession.getAsyncRemote().sendText(driverMessage.toString());

            for (Long id : rideMatchResultDTO.getCarpoolerIds()){
                String passengerSessionId = "C" + id;
                Session passengerSession = sessionMap.get(passengerSessionId);
                if (passengerSession == null) return;

                JSONObject passengerMessage = new JSONObject();
                passengerMessage.put("messageType", "carpoolSuccess");
                passengerMessage.put("accountType", "Carpooler");

                passengerSession.getAsyncRemote().sendText(passengerMessage.toString());
            }
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

            for (Long id : rideMatchResultDTO.getCarpoolerIds()) {
                String passengerSessionId = "C" + id;
                Session passengerSession = sessionMap.get(passengerSessionId);
                if (passengerSession == null) return;


                JSONObject passengerMessage = new JSONObject();
                passengerMessage.put("messageType", "carpoolFailure");
                passengerMessage.put("accountType", "Carpooler");

                passengerSession.getAsyncRemote().sendText(passengerMessage.toString());
            }*/


    //Driver Message
        /*
        List<Long> passengersIds = rideMatchConfirmDTO.getCarpoolGroup().getPassengersIds();
        List<String> wayPoints = new ArrayList<>();

        for (Long passengerId : passengersIds){
            wayPoints.add(carpoolerMapper.getOriginById(passengerId));
            wayPoints.add(carpoolerMapper.getDestinationById(passengerId));
        }

        //TODO
        String wayPointString;
        for (String wayPoint : wayPoints){
            wayPoint += "|";
        }*/

    /**
     * parameters needed for ntf
     * driver: origin & destination_name + passenger_name + matchId
     * carpooler: plateNo + driver_name + car_type + seat_pax + matchId
     */
