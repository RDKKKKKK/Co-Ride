package com.coride.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarpoolGroup implements Serializable {
    private Long idCarpoolGroup;

    private Long idDriver;

    private String plateNo;

    private Integer seatsAvailable;

    private Integer totalSeats;

    private Double originLongitude;

    private Double originLatitude;

    private Double destinationLongitude;

    private Double destinationLatitude;

    private String originName;

    private String destinationName;

    private String status;

    private List<Long> passengersIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrivalTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;

    private Integer idOrganization;

}
