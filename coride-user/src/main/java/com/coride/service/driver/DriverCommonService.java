package com.coride.service.driver;

import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.dto.UserAccountDTO;
import com.coride.entity.Vehicle;

import java.text.ParseException;
import java.util.List;

public interface DriverCommonService {
    UserAccountDTO getDriverAccount(Long id);

    List<DriverRecordDTO> getDriverRecord(Long id);

    List<Vehicle> getVehicles(Long id);

    List<DriverRecordDetailDTO> getDriverRecordDetail(Long id, String departureTime) throws ParseException;
}
