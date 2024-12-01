package com.coride.observer;

import lombok.AllArgsConstructor;

import javax.websocket.Session;


@AllArgsConstructor
public class WebSocketSessionObserver implements WebSocketObserver{

    private final Session session;

    @Override
    public void update(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }
}
