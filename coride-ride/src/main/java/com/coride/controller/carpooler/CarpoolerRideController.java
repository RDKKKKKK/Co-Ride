package com.coride.controller.carpooler;

import com.coride.context.BaseContext;
import com.coride.dto.CarpoolRequestDTO;
import com.coride.dto.RecordDetailDTO;
import com.coride.entity.CarpoolRequest;
import com.coride.result.Result;
import com.coride.service.carpooler.CarpoolerRideService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carpooler/ride")
@Slf4j
public class CarpoolerRideController {

    @Autowired
    private CarpoolerRideService carpoolerRideService;

    @PostMapping(value = "/cancel", produces = "application/json;charset=UTF-8")
    public Result cancelRide(@RequestBody RecordDetailDTO recordDetailDTO){
        Long id = BaseContext.getCurrentId();
        carpoolerRideService.cancelRide(id, recordDetailDTO);
        return Result.success();
    }

    @PostMapping("/record")
    public Result<List<CarpoolRequest>> getCarpoolerRecord(Long id){
        return Result.success(carpoolerRideService.record(id));
    }

    @PostMapping("/driver")
    public Result<String> getCarpoolDriverNameByGroupId(Long id){
        String name = carpoolerRideService.getCarpoolDriverNameByGroupId(id);
        return Result.success(name);
    }
}
