package com.coride.service.carpooler;

import com.coride.dto.CarpoolerRecordDTO;
import com.coride.dto.UserAccountDTO;

import java.util.List;

public interface CarpoolerCommonService {
    UserAccountDTO getCarpoolerAccount(Long id);

    List<CarpoolerRecordDTO> getCarpoolerRecord(Long id);
}
