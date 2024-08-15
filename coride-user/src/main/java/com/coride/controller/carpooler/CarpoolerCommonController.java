package com.coride.controller.carpooler;

import com.coride.context.BaseContext;
import com.coride.dto.CarpoolerRecordDTO;
import com.coride.dto.UserAccountDTO;
import com.coride.result.Result;
import com.coride.service.carpooler.CarpoolerCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carpooler/common")
@Slf4j
public class CarpoolerCommonController {

    @Autowired
    private CarpoolerCommonService carpoolerCommonService;

    @GetMapping(value = "/account")
    public Result<UserAccountDTO> getCarpoolerAccount(){
        Long id = BaseContext.getCurrentId();
        return Result.success(carpoolerCommonService.getCarpoolerAccount(id));
    }

    @GetMapping(value = "/record")
    public Result<List<CarpoolerRecordDTO>> getCarpoolerRecord(){
        Long id = BaseContext.getCurrentId();
        return Result.success(carpoolerCommonService.getCarpoolerRecord(id));
    }
}
