package com.example.project.consumers;

import com.example.project.event.AuthBookingResponseEvent;
import com.example.project.event.BookingAuthEvent;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthBookingConsumer {

    private final UserRepository userRepository;

    @RabbitListener(queues = "auth.travel.agency.queue")
    @SendTo
    public AuthBookingResponseEvent handleCheck(BookingAuthEvent event) {

        log.info("📥 Auth получил запрос проверки пользователя: {}", event);

        User user = userRepository.findById(event.getUserId()).orElse(null);

        AuthBookingResponseEvent response = new AuthBookingResponseEvent();
        response.setUserId(event.getUserId());
        response.setExists(user != null);
        response.setCorrelationId(event.getCorrelationId());

        if (user != null) {
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
        }

        log.info("📤 Auth формирует ответ: {}", response);
        return response;
    }
}
