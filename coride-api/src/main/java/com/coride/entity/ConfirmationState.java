package com.coride.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmationState {
    private Boolean driverConfirmed = null;

    private Boolean passengerConfirmed = null;

    private Long passengerId;

    private Long driverId;

    public synchronized void updateConfirmation(String userId, boolean confirmed) {
        if (userId.equals("driver")) {
            driverConfirmed = confirmed;
        } else if (userId.equals("passenger")) {
            passengerConfirmed = confirmed;
        }
    }

    public synchronized boolean isConfirmedByBoth() {
        if (driverConfirmed != null && passengerConfirmed != null)
            return Boolean.TRUE.equals(driverConfirmed) && Boolean.TRUE.equals(passengerConfirmed);
        else
            return false;
    }

    public boolean isDeclined() {
        if (driverConfirmed != null && passengerConfirmed != null)
            return !driverConfirmed || !passengerConfirmed;
        else
            return false;
    }
}

