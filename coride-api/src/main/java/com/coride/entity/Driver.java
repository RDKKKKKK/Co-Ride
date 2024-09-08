package com.coride.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver extends User implements Serializable {
    private Integer idVehicle;

    private List<String> matchIds;
}
