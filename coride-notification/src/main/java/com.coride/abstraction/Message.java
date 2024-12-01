package com.coride.abstraction;

import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.implementor.Role;
import com.coride.observer.WebSocketObserver;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Message {
    Role role;
    List<WebSocketObserver> observers = new ArrayList<>();

    public void addObserver(WebSocketObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(String message) {
        for (WebSocketObserver observer : observers) {
            observer.update(message);
        }
    }

    public abstract void processMessage() throws IOException;
}
