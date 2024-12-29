package com.coride.matching;

import com.alibaba.fastjson.JSON;
import com.coride.constant.StatusConstant;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Carpooler;
import com.coride.entity.Driver;
import com.coride.exception.BaseException;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.service.message.MessagingServiceTemplate;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Data
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

    @Autowired
    @Qualifier("carpoolThreadPool") // 使用统一线程池
    private ThreadPoolExecutor scheduler;

    private final Map<Long, String> matchingGroups = new ConcurrentHashMap<>(); // 拼车组状态表

    private final ReentrantLock lock = new ReentrantLock(); // 可选锁

    @Transactional
    public void rideMatch(CarpoolGroup carpoolGroup) throws BaseException {

        Long groupId = carpoolGroup.getIdCarpoolGroup();

        // 添加到统计表
        matchingGroups.put(groupId, "In Progress");

        Driver driver;

        String driverJson = stringRedisTemplate.opsForValue().get("cache:user:" + carpoolGroup.getIdDriver());
        if (StrUtil.isNotBlank(driverJson)){
            driver = JSON.parseObject(driverJson, Driver.class);
        }
        else {
            driver = driverMapper.getDriverById(carpoolGroup.getIdDriver());
            stringRedisTemplate.opsForValue().set("cache:user:" + carpoolGroup.getIdDriver(), JSON.toJSONString(driver));
        }

        Runnable task = new MatchingTask(driver, carpoolGroup, this, driverMapper, carpoolerMapper, System.currentTimeMillis(), messagingServiceTemplate, redisTemplate);

        /*
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5); // 5个线程的线程池
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(task, 0, TimeConstant.executePeriod, TimeUnit.SECONDS); */

        // TODO Keynote - Thread Pool
        scheduler.execute(task);
        //scheduler.scheduleAtFixedRate(task, 0, TimeConstant.executePeriod, TimeUnit.SECONDS);
    }


    //匹配和状态更新在同一个事务内，同时加行级写锁
    @Transactional
    public Carpooler findMatch(CarpoolGroup carpoolGroup) {
        Integer orgId = carpoolerMapper.getIdOrganizationByUserId(carpoolGroup.getIdDriver());
        carpoolGroup.setIdOrganization(orgId);
        Long matchedId = carpoolerMapper.findBestMatchID(carpoolGroup);
        Carpooler newPassenger = carpoolerMapper.findById(matchedId);
        if(newPassenger != null){
            carpoolerMapper.updateCarpoolerStatus(newPassenger.getIdUser(), StatusConstant.PENDING);

        }
        return newPassenger;
    }


    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    public void carpoolGroupComplete(CarpoolGroup carpoolGroup, List<Long> passengersIds, Driver driver){
        messagingServiceTemplate.sendMatchResultNotification(new RideMatchResultDTO("Both", "Success", driver.getIdUser(), passengersIds));

        driverMapper.updateDriverStatus(StatusConstant.COMPLETED, carpoolGroup.getIdDriver());
        for (Long id : passengersIds) {
            carpoolerMapper.updateCarpoolerStatus(id, StatusConstant.COMPLETED);
        }

        driverMapper.insertCarpoolGroup(carpoolGroup);
    }

    // 监控当前处于匹配中的拼车组数

    public void updateMatchingStatus(Long groupId, String status) {
        // 更新拼车组状态
        matchingGroups.put(groupId, status);

        // 如果匹配完成或超时，移除拼车组
        if ("Completed".equals(status) || "Timeout".equals(status)) {
            matchingGroups.remove(groupId);
        }
    }

    public void logCurrentMatchingStatus() {
        lock.lock(); // 确保日志输出一致性
        try {
            log.info("Currently matching groups: {}", matchingGroups.size());
            matchingGroups.forEach((groupId, status) -> {
                log.info("Group ID: {}, Status: {}", groupId, status);
            });
        } finally {
            lock.unlock();
        }
    }


}
