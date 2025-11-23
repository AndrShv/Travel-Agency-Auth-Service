package com.example.project.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // --- Exchanges ---
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange("auth.exchange", true, false);
    }

    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange("log.exchange", true, false);
    }

    @Bean
    public TopicExchange homeExchange() {
        return new TopicExchange("home.exchange", true, false);
    }

    @Bean
    @Qualifier("bookingExchange")
    public TopicExchange bookingExchange() {
        return new TopicExchange("booking.exchange", true, false);
    }

    // --- Queues ---
    @Bean
    public Queue authQueue() {
        return new Queue("auth.travel.agency.queue", true);
    }

    @Bean
    public Queue logQueue() {
        return new Queue("log.travel.agency.queue", true);
    }

    @Bean
    public Queue homeQueue() {
        return new Queue("home.travel.agency.queue", true);
    }

    @Bean
    public Queue bookingQueue() {
        return new Queue("booking.travel.agency.queue", true);
    }

    // --- Bindings ---
    @Bean
    public Binding bindingAuth(Queue authQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.#");
    }

    @Bean
    public Binding bindingLog(Queue logQueue, TopicExchange logExchange) {
        return BindingBuilder.bind(logQueue).to(logExchange).with("log.#");
    }

    @Bean
    public Binding bindingHome(Queue homeQueue, TopicExchange homeExchange) {
        return BindingBuilder.bind(homeQueue).to(homeExchange).with("home.#");
    }

    @Bean
    public Binding bindingBooking(Queue bookingQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingQueue).to(bookingExchange).with("booking.#");
    }

    // --- Declarables ---

}