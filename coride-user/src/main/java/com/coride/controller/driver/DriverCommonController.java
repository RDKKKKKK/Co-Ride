package com.coride.controller.driver;

import com.coride.context.BaseContext;
import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.dto.RecordDetailDTO;
import com.coride.dto.UserAccountDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Driver;
import com.coride.entity.Vehicle;
import com.coride.result.Result;
import com.coride.service.driver.DriverCommonService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/driver/common")
@Slf4j
public class DriverCommonController {

    @Autowired
    private DriverCommonService driverCommonService;

    @GetMapping(value = "/account", produces = "application/json;charset=UTF-8")
    public Result<UserAccountDTO> getDriverAccount(){
        Long id = BaseContext.getCurrentId();
        return Result.success(driverCommonService.getDriverAccount(id));
    }

    @GetMapping(value = "/record", produces = "application/json;charset=UTF-8")
    public Result<List<DriverRecordDTO>> getDriverRecord(){
        Long id = BaseContext.getCurrentId();
        return Result.success(driverCommonService.getDriverRecord(id));
    }

    @GetMapping(value = "/vehicle", produces = "application/json;charset=UTF-8")
    public Result<List<Vehicle>> getVehicles(){
        Long id = BaseContext.getCurrentId();
        return Result.success(driverCommonService.getVehicles(id));
    }

    @PostMapping(value = "/record/detail", produces = "application/json;charset=UTF-8")
    public Result<List<DriverRecordDetailDTO>> getRecordDetails(@RequestBody RecordDetailDTO recordDetailDTO) throws ParseException {
        Long id = BaseContext.getCurrentId();
        return Result.success(driverCommonService.getDriverRecordDetail(id, recordDetailDTO.getDepartureTime()));
    }
}
