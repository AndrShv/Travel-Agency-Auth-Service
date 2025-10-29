package com.example.project.service;


import com.example.project.event.AuthEvent;
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
public class AuthRabbitMsgServiceImpl {

    private final TopicExchange authExchange;

    private final RabbitTemplate rabbitTemplate;


    public void sendUserRegisteredEvent(AuthEvent event) {
        String routingKey = "auth.#";
        System.out.println(event.getId());
        System.out.println(event.getEmail());
        System.out.println(event.getUsername());
        rabbitTemplate.convertAndSend(authExchange.getName(), routingKey, event);
    }
}
