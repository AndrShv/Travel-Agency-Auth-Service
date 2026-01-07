package com.example.project.event;


import lombok.Data;

import java.util.UUID;

@Data
public class BookingAuthEvent {
    private UUID userId;
    private String correlationId;
}

