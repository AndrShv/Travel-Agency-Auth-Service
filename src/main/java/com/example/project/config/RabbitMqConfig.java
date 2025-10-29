package com.example.project.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    // --- Connection ---

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
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
        return new TopicExchange("auth.exchange");
    }
    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange("log.exchange");
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

    // --- Bindings ---
    @Bean
    public Binding bindingAuth(Queue authQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with("auth.#");
    }

    @Bean
    public Binding bindingLog(Queue logQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(logQueue).to(authExchange).with("log.#");
    }
}
