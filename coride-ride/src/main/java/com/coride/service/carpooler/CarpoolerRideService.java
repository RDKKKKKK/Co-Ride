package com.coride.service.carpooler;

import com.coride.dto.RecordDetailDTO;
import com.coride.entity.CarpoolRequest;

import java.util.List;

public interface CarpoolerRideService {
    void request(CarpoolRequest carpoolRequest);

    void cancelRide(Long id, RecordDetailDTO recordDetailDTO);

    List<CarpoolRequest> record(Long id);

    String getCarpoolDriverNameByGroupId(Long id);

}
