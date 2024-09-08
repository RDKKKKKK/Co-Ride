package com.coride.matching;

import com.coride.constant.StatusConstant;
import com.coride.entity.CarpoolGroup;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.service.driver.DriverRideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * Always running thread to check:
 * 1. desired driver request ready to be matched
 * 2. timeout carpooler request to be notify
 */

@Component
@Slf4j
public class RideMatchingScheduler {

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private DriverRideService driverRideService;

    @Autowired
    private RideMatcher rideMatcher;

    @Scheduled(fixedRate = 10000) // 每10秒检查一次
    public void findDriverRequestToStartMatching(){

        log.info("Finding available requests...");
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 计算10分钟之前的时间
        LocalDateTime tenMinutesLater = now.plusMinutes(10);

        CarpoolGroup carpoolGroup = driverMapper.findAvailableCarpoolGroup(now, tenMinutesLater);

        if (carpoolGroup != null){
            driverMapper.updateDriverStatus(StatusConstant.AVAILABLE, carpoolGroup.getIdDriver());
            rideMatcher.rideMatch(carpoolGroup);
        }

        carpoolerMapper.refreshAllStatus(now, tenMinutesLater, StatusConstant.AVAILABLE);

    }
}
