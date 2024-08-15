package com.coride.service.driver.impl;

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

    @Override
    public UserAccountDTO getDriverAccount(Long id) {
        Driver driver = driverMapper.getDriverAccountById(id);
        UserAccountDTO userAccountDTO = new UserAccountDTO();
        BeanUtils.copyProperties(driver, userAccountDTO);

        return userAccountDTO;
    }

    @Override
    public List<DriverRecordDTO> getDriverRecord(Long id) {
        List<CarpoolGroup> carpoolGroups = driverMapper.getDriverRecordById(id);
        ArrayList<DriverRecordDTO> driverRecordDTOS = new ArrayList<>();

        for (CarpoolGroup carpoolGroup : carpoolGroups){
            DriverRecordDTO driverRecordDTO = new DriverRecordDTO();

            driverRecordDTO.setPlateNo(carpoolGroup.getPlateNo());
            driverRecordDTO.setDepartureTime(carpoolGroup.getDepartureTime());
            driverRecordDTO.setStatus(carpoolGroup.getStatus());

            Vehicle vehicle = driverMapper.getVehiclesByPlateNo(carpoolGroup.getPlateNo());
            driverRecordDTO.setCarName(vehicle.getName());

            List<String> names = driverMapper.getPassengerNamesByCarpoolGroupId(carpoolGroup.getIdCarpoolGroup());
            driverRecordDTO.setCarpoolers(names);

            driverRecordDTO.setOrigin(carpoolGroup.getOriginName());
            driverRecordDTO.setDestination(carpoolGroup.getDestinationName());

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
        Long carpoolGroupId = driverMapper.getGroupIdByDriverId(id, time);

        ArrayList<DriverRecordDetailDTO> passengerDetailsByCarpoolGroupId = driverMapper.getPassengerDetailsByCarpoolGroupId(carpoolGroupId);
        return passengerDetailsByCarpoolGroupId;
    }

}
