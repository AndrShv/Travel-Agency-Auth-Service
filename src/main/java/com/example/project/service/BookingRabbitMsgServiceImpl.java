package com.example.project.service;


import com.example.project.event.AuthBookingEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class BookingRabbitMsgServiceImpl {

    private final TopicExchange bookingExchange;

    private final RabbitTemplate rabbitTemplate;


    public void sendBookingEvent(AuthBookingEvent event) {
        String routingKey = "booking.#";
        System.out.println(event.toString());
        rabbitTemplate.convertAndSend(bookingExchange.getName(), routingKey, event);
    }
}
