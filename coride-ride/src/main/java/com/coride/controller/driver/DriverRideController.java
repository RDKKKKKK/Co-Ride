package com.coride.controller.driver;

import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.service.driver.DriverRideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.coride.context.BaseContext;
import com.coride.dto.RecordDetailDTO;
import com.coride.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/driver/ride")
@Slf4j
public class DriverRideController {

    @Autowired
    private DriverRideService driverRideService;


    @GetMapping(value = "/vehicles", produces = "application/json;charset=UTF-8")
    public Result<List<String>> getVehicles(){
        List<String> vehicles = driverRideService.getVehicles();
        log.info("Found vehicles: " + vehicles);
        return Result.success(vehicles);
    }

    @PostMapping(value = "/cancel", produces = "application/json;charset=UTF-8")
    public Result cancelRide(@RequestBody RecordDetailDTO recordDetailDTO){
        Long id = BaseContext.getCurrentId();
        driverRideService.cancelRide(id, recordDetailDTO);
        return Result.success();
    }

    @PostMapping(value = "/group", produces = "application/json;charset=UTF-8")
    public Result<List<CarpoolGroup>> getCarpoolGroupByDriverId(Long id){
        return Result.success(driverRideService.getCarpoolGroupByDriverId(id));
    }

    @PostMapping(value = "/record", produces = "application/json;charset=UTF-8")
    public Result<DriverRecordDTO> getDriverRecord(CarpoolGroup carpoolGroup){
        return Result.success(driverRideService.getDriverRecord(carpoolGroup));
    }

    @PostMapping(value = "/detail", produces = "application/json;charset=UTF-8")
    public Result<List<DriverRecordDetailDTO>> getRecordDetail(@RequestParam("id") Long id, @RequestParam("time") Date time){
        return Result.success(driverRideService.getRecordDetail(id, time));
    }
}
