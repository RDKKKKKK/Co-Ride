package com.coride.service.carpooler.impl;

import com.coride.client.RideClient;
import com.coride.dto.CarpoolerRecordDTO;
import com.coride.dto.UserAccountDTO;
import com.coride.entity.CarpoolRequest;
import com.coride.entity.Carpooler;
import com.coride.mapper.CarpoolerMapper;
import com.coride.service.carpooler.CarpoolerCommonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarpoolerCommonServiceImpl implements CarpoolerCommonService {

    @Autowired
    private CarpoolerMapper carpoolerMapper;

    @Autowired
    private RideClient rideClient;

    @Override
    public UserAccountDTO getCarpoolerAccount(Long id) {
        Carpooler carpooler = carpoolerMapper.getCarpoolerAccountById(id);

        UserAccountDTO userAccountDTO = new UserAccountDTO();
        BeanUtils.copyProperties(carpooler, userAccountDTO);

        return userAccountDTO;

    }

    @Override
    public List<CarpoolerRecordDTO> getCarpoolerRecord(Long id) {

        List<CarpoolRequest> records = rideClient.getCarpoolerRecord(id);
        //List<CarpoolRequest> records = carpoolerMapper.getCarpoolerRecordsById(id);
        ArrayList<CarpoolerRecordDTO> carpoolerRecordDTOS = new ArrayList<>();

        for (CarpoolRequest record : records){
            CarpoolerRecordDTO carpoolerRecordDTO = new CarpoolerRecordDTO();

            carpoolerRecordDTO.setDepartureTime(record.getDepartureTime());
            carpoolerRecordDTO.setOrigin(record.getOriginName());
            carpoolerRecordDTO.setDestination(record.getDestinationName());
            carpoolerRecordDTO.setStatus(record.getStatus());

            carpoolerRecordDTO.setDriverName(rideClient.getCarpoolDriverByGroupId(record.getIdCarpoolRequest()));
            //carpoolerRecordDTO.setDriverName(carpoolerMapper.getDriverNameByCarpoolGroupId(record.getIdCarpoolRequest()));
            carpoolerRecordDTOS.add(carpoolerRecordDTO);
        }

        return carpoolerRecordDTOS;
    }
}
