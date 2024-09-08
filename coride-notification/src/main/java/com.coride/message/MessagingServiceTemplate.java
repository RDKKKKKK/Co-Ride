package com.coride.message;

import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagingServiceTemplate {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String topicExchangeName = "notification2RideExchange";

    public void sendPassengerRequest(CarpoolRequest message) {
        rabbitTemplate.convertAndSend(topicExchangeName, "request.passenger", message);
    }

    public void sendDriverRequest(CarpoolGroup message) {
        rabbitTemplate.convertAndSend(topicExchangeName, "request.driver", message);
    }

    public void sendConfirmation(String message){
        rabbitTemplate.convertAndSend(topicExchangeName, "confirmation", message);
    }

}

