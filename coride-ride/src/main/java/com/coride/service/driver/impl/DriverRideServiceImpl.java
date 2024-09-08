package com.coride.service.driver.impl;

import com.coride.constant.StatusConstant;
import com.coride.context.BaseContext;
import com.coride.dto.ConfirmDTO;
import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.dto.RecordDetailDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.ConfirmationState;
import com.coride.entity.Vehicle;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.matching.RideMatcher;
import com.coride.service.driver.DriverRideService;
import com.coride.service.message.MessagingServiceTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DriverRideServiceImpl implements DriverRideService {


    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    //public static final ConcurrentHashMap<String, ConfirmationState> confirmations = new ConcurrentHashMap<>();


    @Override
    public List<String> getVehicles() {
        Long id = BaseContext.getCurrentId();
        Set<String> plateNos = stringRedisTemplate.opsForSet().members("cache:driversVehicles:" + id);
        if (!plateNos.isEmpty())
            return new ArrayList<>(plateNos);

        List<String> plates = driverMapper.getVehiclesByDriverId(id);
        stringRedisTemplate.opsForSet().add("cache:driversVehicles:" + id, plates.toArray(new String[0]));
        return plates;
        }

    @Override
    public void request(CarpoolGroup carpoolGroup) {
        Integer seats = driverMapper.getSeats(carpoolGroup.getPlateNo());
        carpoolGroup.setSeatsAvailable(seats);
        carpoolGroup.setTotalSeats(seats);
        carpoolGroup.setStatus(StatusConstant.SCHEDULED);
        carpoolGroup.setRequestTime(LocalDateTime.now());
        carpoolGroup.setOriginName(getLocationName(carpoolGroup.getOriginLatitude(), carpoolGroup.getOriginLongitude()));
        carpoolGroup.setDestinationName(getLocationName(carpoolGroup.getDestinationLatitude(), carpoolGroup.getDestinationLongitude()));

        driverMapper.insertCarpoolGroup(carpoolGroup);
        //rideMatcher.rideMatch(carpoolGroup);
        //driver request doesn't directly trigger matching process
        //instead scheduled thread do it

        log.info("Driver request accepted!");
    }

    @Override
    public void cancelRide(Long id, RecordDetailDTO recordDetailDTO) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = null;
        try {
            time = dateFormat.parse(recordDetailDTO.getDepartureTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        driverMapper.cancelRide(id, time);
    }

    @Override
    public List<CarpoolGroup> getCarpoolGroupByDriverId(Long id) {
        return driverMapper.getDriverRecordById(id);
    }

    @Override
    public DriverRecordDTO getDriverRecord(CarpoolGroup carpoolGroup) {
        DriverRecordDTO driverRecordDTO = new DriverRecordDTO();

        driverRecordDTO.setPlateNo(carpoolGroup.getPlateNo());
        driverRecordDTO.setDepartureTime(carpoolGroup.getDepartureTime());
        driverRecordDTO.setStatus(carpoolGroup.getStatus());

        Vehicle vehicle = driverMapper.getVehiclesByPlateNo(carpoolGroup.getPlateNo());
        driverRecordDTO.setCarName(vehicle.getName());

        List<String> names = driverMapper.getPassengerNamesByCarpoolGroupId(carpoolGroup.getIdCarpoolGroup());
        driverRecordDTO.setCarpoolers(names);

        driverRecordDTO.setOrigin(carpoolGroup.getOriginName());
        driverRecordDTO.setDestination(carpoolGroup.getDestinationName());

        return driverRecordDTO;
    }

    @Override
    public List<DriverRecordDetailDTO> getRecordDetail(Long id, Date time) {
        Long carpoolGroupId = driverMapper.getGroupIdByDriverId(id, time);
        ArrayList<DriverRecordDetailDTO> passengerDetailsByCarpoolGroupId = driverMapper.getPassengerDetailsByCarpoolGroupId(carpoolGroupId);

        return passengerDetailsByCarpoolGroupId;
    }

    public String getLocationName(double latitude, double longitude) {
        try {
            String ak = "GKtfRmYn1AlU6NXy0iWIekzgZdF0SkmG";
            String baseUrl = "http://api.map.baidu.com/reverse_geocoding/v3/?";
            String requestUrl = baseUrl + "ak=" + ak + "&output=json&coordtype=wgs84ll&location=" + latitude + "," + longitude +"&extensions_poi=1&radius=100";
            String poiName = "";

            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 解析响应内容
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject result = jsonResponse.getJSONObject("result");

            // 尝试从pois字段获取简短地点名字
            if (result.has("pois") && result.getJSONArray("pois").length() > 0) {
                JSONArray pois = result.getJSONArray("pois");
                JSONObject poi = pois.getJSONObject(0);
                poiName = poi.getString("name");
            }
            return poiName;
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown location";
        }
    }


    public void rideConfirm(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ConfirmDTO confirmDTO = mapper.readValue(message, ConfirmDTO.class);
        String matchID = confirmDTO.getMatchId();

        ConfirmationState confirmationState = (ConfirmationState) redisTemplate.opsForValue().get("confirmation_state:" + matchID);
        //ConfirmationState confirmationState = confirmations.get(confirmDTO.getMatchId());

        switch (confirmDTO.getAccountType()){
            case "Carpooler":
                confirmationState.setPassengerConfirmed(confirmDTO.isConfirmed());
                log.info("Carpooler ID="+confirmationState.getPassengerId()+" confirmed="+confirmDTO.isConfirmed());
                break;

            case "Driver":
                confirmationState.setDriverConfirmed(confirmDTO.isConfirmed());
                log.info("Driver ID="+confirmationState.getDriverId()+" confirmed="+confirmDTO.isConfirmed());
                break;
        }

        redisTemplate.opsForValue().set("confirmation_state:" + matchID, confirmationState, 10, TimeUnit.MINUTES);

    }



    /*
    @Override
    public void notifyDriver(Long driverId, Map<String, Session> sessionMap, Carpooler newPassenger, String matchID) {
        String sessionId = "D" + driverId.toString();
        Session session = sessionMap.get(sessionId);

        try {
            CarpoolerInfoDTO carpoolerInfoDTO = carpoolerMapper.getInfo(newPassenger.getIdUser());
            JSONObject message = new JSONObject();
            message.put("messageType", "rideConfirm");
            message.put("passengerName", newPassenger.getName());
            message.put("originName", carpoolerInfoDTO.getOriginName());
            message.put("destinationName", carpoolerInfoDTO.getDestinationName());
            message.put("accountType", "Driver");
            message.put("matchId", matchID);

            session.getAsyncRemote().sendText(message.toString());
            log.info("Confirmation sent to driver");

        }catch (Exception e){
            System.out.println(e);
        }

    }

    @Override
    public void notifyCarpooler(Long carpoolerId, Map<String, Session> sessionMap, CarpoolGroup carpoolGroup, Driver driver, String matchID) {
        String sessionId = "C" + carpoolerId;
        Session session = sessionMap.get(sessionId);

        DriverInfoDTO driverInfoDTO = new DriverInfoDTO();
        Vehicle vehicle = driverMapper.getVehiclesByPlateNo(carpoolGroup.getPlateNo());

        driverInfoDTO.setDriverName(driver.getName());
        driverInfoDTO.setSeats(vehicle.getSeats());
        driverInfoDTO.setCarType(vehicle.getName());
        driverInfoDTO.setMatchId(matchID);

        JSONObject message = new JSONObject();

        message.put("messageType", "rideConfirm");
        message.put("driverName", driverInfoDTO.getDriverName());
        message.put("carType", driverInfoDTO.getCarType());
        message.put("seats", driverInfoDTO.getSeats());
        message.put("matchId", driverInfoDTO.getMatchId());
        message.put("accountType", "Carpooler");

        session.getAsyncRemote().sendText(message.toString());
        log.info("Confirmation sent to carpooler");

    }


    @Override
    public void notifyCarpoolSuccess(Long driverId, List<Long> passengers,  Map<String, Session> sessionMap) throws InterruptedException {
        String driverSessionId = "D" + driverId;
        Session driverSession = sessionMap.get(driverSessionId);

        JSONObject driverMessage = new JSONObject();
        driverMessage.put("messageType", "carpoolSuccess");
        driverMessage.put("accountType", "Driver");

        driverSession.getAsyncRemote().sendText(driverMessage.toString());

        for (Long id : passengers){
            String passengerSessionId = "C" + id;
            Session passengerSession = sessionMap.get(passengerSessionId);

            JSONObject passengerMessage = new JSONObject();
            passengerMessage.put("messageType", "carpoolSuccess");
            passengerMessage.put("accountType", "Driver");


            rabbitTemplate.convertAndSend("ride.result");
            passengerSession.getAsyncRemote().sendText(passengerMessage.toString());
        }
    }

    @Override
    public void notifyCarpoolFailure(Long driverId, List<Long> passengers, Map<String, Session> sessionMap) {
        if (driverId != null) {
            String driverSessionId = "D" + driverId;
            Session driverSession = sessionMap.get(driverSessionId);

            JSONObject driverMessage = new JSONObject();
            driverMessage.put("messageType", "carpoolFailure");
            driverMessage.put("accountType", "Driver");

            driverSession.getAsyncRemote().sendText(driverMessage.toString());
        }

        for (Long id : passengers){
            String passengerSessionId = "C" + id;
            Session passengerSession = sessionMap.get(passengerSessionId);

            JSONObject passengerMessage = new JSONObject();
            passengerMessage.put("messageType", "carpoolFailure");
            passengerMessage.put("accountType", "Carpooler");

            passengerSession.getAsyncRemote().sendText(passengerMessage.toString());
        }
    }

    @Override
    public List<CarpoolRequest> rideMatch(CarpoolGroup carpoolGroup) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        final Runnable task = new Runnable() {
            public void run() {
                // ride match algorithm
                System.out.println("Try matching... - " + System.currentTimeMillis() / 1000);
            }
        };

        // 每15秒执行一次任务，总共执行8次（2分钟）
        final int initialDelay = 0;
        final int period = 15;
        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

        // 在2分钟后关闭scheduler并执行后续操作
        scheduler.schedule(new Runnable() {
            public void run() {
                scheduler.shutdown();
                try {
                    if (scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                        // 这里执行后续操作
                        System.out.println("Matching ended... - " + System.currentTimeMillis() / 1000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, 1, TimeUnit.MINUTES);
        return null;
    }

        @Override
    public String carpool(RideOfferDTO rideOfferDTO) {

        CarpoolGroup carpoolGroup = new CarpoolGroup();
        BeanUtils.copyProperties(rideOfferDTO, carpoolGroup);
        carpoolGroup.setRequestTime(LocalDateTime.now());
        carpoolGroup.setStatus(StatusConstant.PENDING);

        RoutePlanRequestDTO routePlanRequestDTO = routePlanningService.constructRouteRequest(carpoolGroup, rideMatch(carpoolGroup));
        String routeResult = routePlanningService.sendRouteRequest(routePlanRequestDTO, " ");

        return routeResult;
    }

    */
}
