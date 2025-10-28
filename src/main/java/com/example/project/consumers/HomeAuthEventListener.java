package com.example.project.consumers;

import com.example.project.event.AuthEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeAuthEventListener {

    @RabbitListener(queues = "home.travel.agency.queue")
    public AuthEvent handleTourCreated(AuthEvent event) {
        System.out.println("[Home Service] Получено событие аутентификации пользователя: "
                + event.getUsername() + " (" + event.getEmail() + "), Действие: " + event.getActionType());
        return event;
    }
}

