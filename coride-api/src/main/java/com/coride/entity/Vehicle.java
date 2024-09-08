package com.coride.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Vehicle implements Serializable {
    private String idVehicle;

    private Integer seats;

    private String name;

    private String plateNo;

}
