package com.coride.client;

import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@FeignClient(value = "ride-service", url = "ride-service:8081")
public interface RideClient {

    @PostMapping("/carpooler/ride/record")
    List<CarpoolRequest> getCarpoolerRecord(Long id);

    @PostMapping("/carpooler/ride/driver")
    String getCarpoolDriverByGroupId(Long id);

    @PostMapping("/driver/ride/group")
    List<CarpoolGroup> getCarpoolGroupByDriverId(Long id);

    @PostMapping("/driver/ride/record")
    DriverRecordDTO getDriverRecord(CarpoolGroup carpoolGroup);

    @PostMapping("/driver/ride/detail")
    List<DriverRecordDetailDTO> getRecordDetail(@RequestParam("id") Long id, @RequestParam("time") Date time);


}
