package com.example.project.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthBookingEvent {
    private UUID userId;
    private String username;
    private String email;
}
