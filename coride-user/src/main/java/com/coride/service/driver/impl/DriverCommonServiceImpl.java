package com.coride.service.driver.impl;

import com.coride.client.RideClient;
import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.dto.UserAccountDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.Driver;
import com.coride.entity.Vehicle;
import com.coride.mapper.CarpoolerMapper;
import com.coride.mapper.DriverMapper;
import com.coride.service.driver.DriverCommonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DriverCommonServiceImpl implements DriverCommonService {

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private RideClient rideClient;

    @Override
    public UserAccountDTO getDriverAccount(Long id) {
        Driver driver = driverMapper.getDriverAccountById(id);
        UserAccountDTO userAccountDTO = new UserAccountDTO();
        BeanUtils.copyProperties(driver, userAccountDTO);

        return userAccountDTO;
    }

    @Override
    public List<DriverRecordDTO> getDriverRecord(Long id) {

        List<CarpoolGroup> carpoolGroups = rideClient.getCarpoolGroupByDriverId(id);
        //List<CarpoolGroup> carpoolGroups = driverMapper.getDriverRecordById(id);

        ArrayList<DriverRecordDTO> driverRecordDTOS = new ArrayList<>();

        for (CarpoolGroup carpoolGroup : carpoolGroups){
            DriverRecordDTO driverRecordDTO = rideClient.getDriverRecord(carpoolGroup);
            driverRecordDTOS.add(driverRecordDTO);
        }


        return driverRecordDTOS;
    }

    @Override
    public List<Vehicle> getVehicles(Long id) {
        return driverMapper.getVehiclesInfoByDriverId(id);
    }

    @Override
    public List<DriverRecordDetailDTO> getDriverRecordDetail(Long id, String departureTime) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = dateFormat.parse(departureTime);

        List<DriverRecordDetailDTO> passengerDetailsByCarpoolGroupId = rideClient.getRecordDetail(id, time);

         return passengerDetailsByCarpoolGroupId;
    }

}
