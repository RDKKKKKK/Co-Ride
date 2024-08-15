package com.coride.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoutePlanRequestDTO implements Serializable{
    private String origin; // 起点经纬度
    private String destination; // 终点经纬度
    private String waypoints; // 途径点经纬度
    private String AK;
}
