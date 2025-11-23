package com.example.project.event;


import lombok.Data;

import java.util.UUID;

@Data
public class AuthBookingResponseEvent {
    private UUID userId;
    private String username;
    private String email;
    private boolean exists;
    private String correlationId;
}
