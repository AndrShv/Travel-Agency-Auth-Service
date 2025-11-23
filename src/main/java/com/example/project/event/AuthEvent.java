package com.example.project.event;

import com.example.project.enums.UserActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthEvent {
    private UUID id;
    private String username;
    private String email;
    private UUID tourId;
    private String tourName;
    private UserActionType actionType;
}
