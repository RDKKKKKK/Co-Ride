package com.coride.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideMatchResultDTO implements Serializable {
    private String roleType;

    private String resultType;

    private Long driverId;

    private List<Long> carpoolerIds;

}
