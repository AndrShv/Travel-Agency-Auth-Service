package com.example.project.event;



import com.example.project.enums.EventType;
import com.example.project.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEvent {
    private UUID id;
    private EventType eventType;
    private String userId;
    private ServiceType serviceType;
    private String details;
}

