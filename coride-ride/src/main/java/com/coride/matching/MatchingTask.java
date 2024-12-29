package com.coride.matching;

import com.coride.constant.StatusConstant;
import com.coride.constant.TimeConstant;
import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Carpooler;
import com.coride.entity.ConfirmationState;
import com.coride.entity.Driver;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.service.message.MessagingServiceTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Data
@Slf4j
public class MatchingTask implements Runnable,  Comparable<MatchingTask> {
    private Driver driver;
    private CarpoolGroup carpoolGroup;
    private RideMatcher rideMatcher;
    private final DriverMapper driverMapper;
    private final CarpoolerMapper carpoolerMapper;
    private final Long startTime;
    public final MessagingServiceTemplate messagingServiceTemplate;
    public final RedisTemplate redisTemplate;

    @Override
    public void run() {
        while (true) {
            try {
                //TODO can later be customized
                //carpoolGroup.setTotalSeats(carpoolGroup.getSeatsAvailable());

                // 查询匹配的乘客请求
                long currentTime = System.currentTimeMillis();
                long timeRemaining = (TimeConstant.scheduledTime - (currentTime - startTime)) / 1000;
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
                    //String matchID = UUID.randomUUID().toString();
                    String matchID = driver.getIdUser().toString() + "-" + newPassenger.getIdUser().toString();

                    redisTemplate.opsForValue().set("confirmation_state:" + matchID, new ConfirmationState(null, null, newPassenger.getIdUser(), driver.getIdUser()), 10, TimeUnit.MINUTES);

                    //confirmations.put(matchID, new ConfirmationState(null, null, newPassenger.getIdUser(), driver.getIdUser()));

                    matchIds.add(matchID);
                    driver.setMatchIds(matchIds);

                    messagingServiceTemplate.sendMatchConfirmNotification(new RideMatchConfirmDTO("Driver", driver.getIdUser(), newPassenger.getIdUser(), null, null, matchID, newPassenger.getName(), carpoolerMapper.getOriginById(newPassenger.getIdUser()), carpoolerMapper.getDestinationById(newPassenger.getIdUser()), null));
                    messagingServiceTemplate.sendMatchConfirmNotification(new RideMatchConfirmDTO("Carpooler", 0L, newPassenger.getIdUser(), driver, carpoolGroup.getPlateNo(), matchID, newPassenger.getName(), null, null, driverMapper.getVehiclesByPlateNo(carpoolGroup.getPlateNo())));
                    // driverRideService.notifyDriver(carpoolGroup.getDriverId(), sessionMap, newPassenger, matchID);
                    // driverRideService.notifyCarpooler(newPassenger.getIdUser(), sessionMap, carpoolGroup, driver, matchID);
                }

                Iterator<String> iterator = matchIds.iterator();
                while (iterator.hasNext()) {
                    String matchId = iterator.next();

                    ConfirmationState confirmationState = (ConfirmationState) redisTemplate.opsForValue().get("confirmation_state:" + matchId);

                    if (confirmationState.isConfirmedByBoth()) {
                        //双方都同意 加入拼车组 删除状态变量
                        passengersIds.add(confirmationState.getPassengerId());
                        carpoolGroup.setPassengersIds(passengersIds);
                        carpoolGroup.setSeatsAvailable(carpoolGroup.getSeatsAvailable() - 1);
                        carpoolerMapper.updateCarpoolerStatus(confirmationState.getPassengerId(), StatusConstant.ACCEPTED);
                        carpoolerMapper.addToCarpoolGroup(confirmationState.getPassengerId(), carpoolGroup.getIdDriver());

                        redisTemplate.delete("confirmation_state:" + matchId);
                        iterator.remove();
                        driver.setMatchIds(matchIds);
                    } else if (confirmationState.isDeclined()) {
                        //一方拒绝 通知拼车人 恢复状态继续匹配 司机端无操作
                        List<Long> failurePassengerId = new ArrayList<>();
                        failurePassengerId.add(confirmationState.getPassengerId());

                        //driverRideService.notifyCarpoolFailure(null, failurePassengerId, sessionMap);
                        carpoolerMapper.updateCarpoolerStatus(confirmationState.getPassengerId(), StatusConstant.AVAILABLE);
                        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Carpooler", "Failure", null, failurePassengerId));
                        redisTemplate.delete("confirmation_state:" + matchId);
                        iterator.remove();
                        driver.setMatchIds(matchIds);
                    }
                }

                if (carpoolGroup.getSeatsAvailable() == 0) {
                    //成功：车已匹配满
                    rideMatcher.carpoolGroupComplete(carpoolGroup, passengersIds, driver);
                    rideMatcher.updateMatchingStatus(carpoolGroup.getIdCarpoolGroup(), "Completed");

                    break;


                } else if ((currentTime - startTime) > TimeConstant.scheduledTime) {
                    //拼车组匹配超时
                    if (!carpoolGroup.getSeatsAvailable().equals(carpoolGroup.getTotalSeats())) {
                        //成功：有人匹配
                        rideMatcher.carpoolGroupComplete(carpoolGroup, passengersIds, driver);
                        rideMatcher.updateMatchingStatus(carpoolGroup.getIdCarpoolGroup(), "Completed");

                        break;

                    } else {
                        //driverRideService.notifyCarpoolFailure(carpoolGroup.getDriverId(), passengersIds, sessionMap);
                        driverMapper.updateDriverStatus(StatusConstant.REJECTED, carpoolGroup.getIdDriver());
                        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Both", "Failure", driver.getIdUser(), passengersIds));

                        rideMatcher.updateMatchingStatus(carpoolGroup.getIdCarpoolGroup(), "Failed");

                        //失败：无人匹配
                        break;

                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int compareTo(MatchingTask other) {
        // 按预约时间优先级排序，时间越早优先级越高
        return this.carpoolGroup.getDepartureTime().compareTo(other.carpoolGroup.getDepartureTime());
    }
}
