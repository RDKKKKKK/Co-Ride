package com.coride.service.driver;

import com.coride.dto.DriverRecordDTO;
import com.coride.dto.DriverRecordDetailDTO;
import com.coride.dto.RecordDetailDTO;
import com.coride.entity.CarpoolGroup;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;
import java.util.List;


public interface DriverRideService {
    //String carpool(RideOfferDTO rideOfferDTO);

    //List<CarpoolRequest> rideMatch(CarpoolGroup carpoolGroup);

    List<String> getVehicles();

    void request(CarpoolGroup carpoolGroup);

    void cancelRide(Long id, RecordDetailDTO recordDetailDTO);

    List<CarpoolGroup> getCarpoolGroupByDriverId(Long id);

    DriverRecordDTO getDriverRecord(CarpoolGroup carpoolGroup);

    List<DriverRecordDetailDTO> getRecordDetail(Long id, Date time);

    public void rideConfirm(String msg) throws JsonProcessingException;

    /*

    void rideMatch(CarpoolGroup carpoolGroup) throws BaseException;

    Carpooler findMatch(CarpoolGroup carpoolGroup);

    void notifyDriver(Long driverId, Map<String, Session> sessionMap, Carpooler newPassenger, String matchId);

    void notifyCarpooler(Long carpoolerId, Map<String, Session> sessionMap, CarpoolGroup carpoolGroup, Driver driver, String matchId);

    void notifyCarpoolSuccess(Long driverId, List<Long> passengers,  Map<String, Session> sessionMap) throws InterruptedException;

    void notifyCarpoolFailure(Long driverId,  List<Long> passengers, Map<String, Session> sessionMap);

     */
}
