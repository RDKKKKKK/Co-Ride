package com.coride.matching;

import com.alibaba.fastjson.JSON;
import com.coride.constant.StatusConstant;
import com.coride.constant.TimeConstant;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Carpooler;
import com.coride.entity.ConfirmationState;
import com.coride.entity.Driver;
import com.coride.exception.BaseException;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.service.message.MessagingServiceTemplate;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RideMatcher {

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MessagingServiceTemplate messagingServiceTemplate;

    @Transactional
    public void rideMatch(CarpoolGroup carpoolGroup) throws BaseException {

        Driver driver;

        String driverJson = stringRedisTemplate.opsForValue().get("cache:user:" + carpoolGroup.getIdDriver());
        if (StrUtil.isNotBlank(driverJson)){
            driver = JSON.parseObject(driverJson, Driver.class);
        }
        else {
            driver = driverMapper.getDriverById(carpoolGroup.getIdDriver());
            stringRedisTemplate.opsForValue().set("cache:user:" + carpoolGroup.getIdDriver(), JSON.toJSONString(driver));
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new MatchingTask(driver, carpoolGroup, this, driverMapper, carpoolerMapper, executor, System.currentTimeMillis(), messagingServiceTemplate, redisTemplate);
        executor.scheduleAtFixedRate(task, 0, TimeConstant.executePeriod, TimeUnit.SECONDS);

    }

    @AllArgsConstructor
    private static class MatchingTask implements Runnable {
        private Driver driver;
        private CarpoolGroup carpoolGroup;
        private RideMatcher rideMatcher;
        private final DriverMapper driverMapper;
        private final CarpoolerMapper carpoolerMapper;
        private final ScheduledExecutorService executor;
        private final Long startTime;
        private final MessagingServiceTemplate messagingServiceTemplate;
        private final RedisTemplate redisTemplate;

        @Override
        public void run() {
            try {
                //TODO can later be customized
                //carpoolGroup.setTotalSeats(carpoolGroup.getSeatsAvailable());

                // 查询匹配的乘客请求
                long currentTime = System.currentTimeMillis();
                long timeRemaining = (TimeConstant.scheduledTime - (currentTime - startTime))/1000;
                log.info("Ride match time remaining: " + timeRemaining);
                List<Long> passengersIds;
                if (carpoolGroup.getPassengersIds() != null)
                    passengersIds = carpoolGroup.getPassengersIds();
                else
                    passengersIds = new ArrayList<>();

                Carpooler newPassenger = rideMatcher.findMatch(carpoolGroup);
                List<String> matchIds = new ArrayList<>();

                if (driver.getMatchIds() != null)
                    matchIds = driver.getMatchIds();


                if (newPassenger != null) {
                    log.info("Match carpooler ID=: " + newPassenger.getIdUser());
                    String matchID = UUID.randomUUID().toString();

                    redisTemplate.opsForValue().set("confirmation_state:" + matchID, new ConfirmationState(null, null, newPassenger.getIdUser(), driver.getIdUser()), 10, TimeUnit.MINUTES);

                    //confirmations.put(matchID, new ConfirmationState(null, null, newPassenger.getIdUser(), driver.getIdUser()));
                    carpoolerMapper.updateCarpoolerStatus(newPassenger.getIdUser(), StatusConstant.PENDING);

                    matchIds.add(matchID);
                    driver.setMatchIds(matchIds);

                    messagingServiceTemplate.sendMatchConfirmNotification(new RideMatchConfirmDTO("Driver", driver.getIdUser(), newPassenger.getIdUser(), null, null, matchID, newPassenger.getName(), carpoolerMapper.getOriginById(newPassenger.getIdUser()), carpoolerMapper.getDestinationById(newPassenger.getIdUser()), null));
                    messagingServiceTemplate.sendMatchConfirmNotification(new RideMatchConfirmDTO("Carpooler", 0L, newPassenger.getIdUser(), driver, carpoolGroup.getPlateNo(), matchID, newPassenger.getName(), null, null, driverMapper.getVehiclesByPlateNo(carpoolGroup.getPlateNo())));
                    //driverRideService.notifyDriver(carpoolGroup.getDriverId(), sessionMap, newPassenger, matchID);
                    // driverRideService.notifyCarpooler(newPassenger.getIdUser(), sessionMap, carpoolGroup, driver, matchID);
                }

                Iterator<String> iterator = matchIds.iterator();
                while (iterator.hasNext()) {
                    String matchId = iterator.next();

                    ConfirmationState confirmationState = (ConfirmationState) redisTemplate.opsForValue().get("confirmation_state:" + matchId);

                    if (confirmationState.isConfirmedByBoth()) {
                        passengersIds.add(confirmationState.getPassengerId());
                        carpoolGroup.setPassengersIds(passengersIds);
                        carpoolGroup.setSeatsAvailable(carpoolGroup.getSeatsAvailable() - 1);
                        carpoolerMapper.updateCarpoolerStatus(confirmationState.getPassengerId(), StatusConstant.ACCEPTED);
                        carpoolerMapper.addToCarpoolGroup(confirmationState.getPassengerId(), carpoolGroup.getIdDriver());

                        redisTemplate.delete("confirmation_state" + matchId);
                        //confirmations.remove(matchId);
                        iterator.remove();
                        driver.setMatchIds(matchIds);
                    } else if (confirmationState.isDeclined()) {
                        List<Long> failurePassengerId = new ArrayList<>();
                        failurePassengerId.add(confirmationState.getPassengerId());

                        //driverRideService.notifyCarpoolFailure(null, failurePassengerId, sessionMap);
                        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Carpooler", "Failure", null, failurePassengerId));
                        carpoolerMapper.updateCarpoolerStatus(confirmationState.getPassengerId(), StatusConstant.AVAILABLE);
                        redisTemplate.delete("confirmation_state" + matchId);
                        iterator.remove();
                        driver.setMatchIds(matchIds);
                    }
                }

                if (carpoolGroup.getSeatsAvailable() == 0) {
                    executor.shutdown();
                    //driverRideService.notifyCarpoolSuccess(carpoolGroup.getDriverId(), passengersIds, sessionMap);
                    messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Both", "Success", driver.getIdUser(), passengersIds));

                    driverMapper.updateDriverStatus(StatusConstant.COMPLETED, carpoolGroup.getIdDriver());
                    for (Long id : passengersIds) {
                        carpoolerMapper.updateCarpoolerStatus(id, StatusConstant.COMPLETED);
                    }
                    driverMapper.insertCarpoolGroup(carpoolGroup);

                 } else if ((currentTime - startTime) > TimeConstant.scheduledTime) {
                    if (!carpoolGroup.getSeatsAvailable().equals(carpoolGroup.getTotalSeats())) {
                        executor.shutdown();

                        //driverRideService.notifyCarpoolSuccess(carpoolGroup.getDriverId(), passengersIds, sessionMap);
                        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Both", "Success", driver.getIdUser(), passengersIds));

                        driverMapper.updateDriverStatus(StatusConstant.COMPLETED, carpoolGroup.getIdDriver());
                        for (Long id : passengersIds) {
                            carpoolerMapper.updateCarpoolerStatus(id, StatusConstant.COMPLETED);
                        }

                        driverMapper.insertCarpoolGroup(carpoolGroup);
                    } else {
                        executor.shutdown();

                        //driverRideService.notifyCarpoolFailure(carpoolGroup.getDriverId(), passengersIds, sessionMap);
                        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Both", "Failure", driver.getIdUser(), passengersIds));

                        driverMapper.updateDriverStatus(StatusConstant.REJECTED, carpoolGroup.getIdDriver());
                    }
                }
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

    }

    public Carpooler findMatch(CarpoolGroup carpoolGroup) {
        Integer orgId = carpoolerMapper.getIdOrganizationByUserId(carpoolGroup.getIdDriver());
        carpoolGroup.setIdOrganization(orgId);
        Long matchedId = carpoolerMapper.findBestMatchID(carpoolGroup);
        return carpoolerMapper.findById(matchedId);
    }


}
