package com.example.project.service;


import com.example.project.event.LogEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class LogRabbitMsgServiceImpl {

    private final TopicExchange logExchange;

    private final RabbitTemplate rabbitTemplate;


    public void sendLogEvent(LogEvent event) {
        String routingKey = "log.#";
        System.out.println(event.getId());
        System.out.println(event.getEventType());
        System.out.println(event.getUserId());
        System.out.println(event.getServiceType());
        rabbitTemplate.convertAndSend(logExchange.getName(), routingKey, event);
    }
}
