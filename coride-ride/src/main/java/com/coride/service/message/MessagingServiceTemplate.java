package com.coride.service.message;

import com.coride.dto.RideMatchConfirmDTO;
import com.coride.dto.RideMatchResultDTO;
import com.coride.entity.CarpoolGroup;
import com.coride.entity.CarpoolRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagingServiceTemplate {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String topicExchangeName = "ride2NotificationExchange";

    public void sendMatchConfirmNotification(RideMatchConfirmDTO message) {
        rabbitTemplate.convertAndSend(topicExchangeName, "notification.confirm", message);
    }

    public void sendMatchResultNotification(RideMatchResultDTO message) {
        rabbitTemplate.convertAndSend(topicExchangeName, "notification.result", message);
    }
}

