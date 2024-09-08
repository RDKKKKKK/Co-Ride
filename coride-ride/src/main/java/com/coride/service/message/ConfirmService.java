package com.coride.service.message;

import com.coride.entity.CarpoolGroup;
import com.coride.service.driver.DriverRideService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmService {

    @Autowired
    DriverRideService driverRideService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("RideConfirmQueue"),
            exchange = @Exchange(name = "notification2RideExchange", type = ExchangeTypes.TOPIC),
            key = "confirmation"
    ))
    public void confirm(String msg) throws JsonProcessingException {
        driverRideService.rideConfirm(msg);
    }

}
