package com.example.project.service;

import com.example.project.event.AuthBookingEvent;
import com.example.project.exceptions.RabbitMqSendingException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingRabbitMsgServiceImpl {

    private final TopicExchange bookingExchange;
    private final RabbitTemplate rabbitTemplate;

    public void sendBookingEvent(AuthBookingEvent event) {
        String routingKey = "booking.user.sent";
        System.out.println("🚀 Отправка AuthBookingEvent в RabbitMQ...");
        System.out.println(("Exchange: {}"+ bookingExchange.getName()));
        System.out.println("Routing key: {}" + routingKey);
        System.out.println("Event: {}" + event);

        try {
            rabbitTemplate.convertAndSend(bookingExchange.getName(), routingKey, event);
            log.info("✅ AuthBookingEvent успешно отправлено");
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке AuthBookingEvent", e);
            throw new RabbitMqSendingException("Не удалось отправить событие в RabbitMQ");
        }
    }
    @PostConstruct
    public void init() {
        System.out.println("🔥 Booking producer LOADED!!!");
    }
}