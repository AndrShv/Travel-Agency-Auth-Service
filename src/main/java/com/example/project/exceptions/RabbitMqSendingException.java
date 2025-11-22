package com.example.project.exceptions;

public class RabbitMqSendingException extends RuntimeException {
    public RabbitMqSendingException(String message) {
        super(message);
    }
}
