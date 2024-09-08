package com.coride.service.message;

import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import com.coride.service.carpooler.CarpoolerRideService;
import com.coride.service.driver.DriverRideService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    @Autowired
    private DriverRideService driverRideService;

    @Autowired
    private CarpoolerRideService carpoolerRideService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("driverRequestQueue"),
            exchange = @Exchange(name = "notification2RideExchange", type = ExchangeTypes.TOPIC),
            key = "request.driver"
    ))
    public void driverRequest(CarpoolGroup carpoolGroup){
        driverRideService.request(carpoolGroup);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("passengerRequestQueue"),
            exchange = @Exchange(name = "notification2RideExchange", type = ExchangeTypes.TOPIC),
            key = "request.passenger"
    ))
    public void passengerRequest(CarpoolRequest carpoolRequest){
        carpoolerRideService.request(carpoolRequest);
    }

}
