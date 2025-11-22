package com.example.project.service;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.EventType;
import com.example.project.enums.Role;
import com.example.project.enums.ServiceType;
import com.example.project.event.AuthBookingEvent;
import com.example.project.event.AuthEvent;
import com.example.project.event.LogEvent;
import com.example.project.exceptions.*;
import com.example.project.interfaces.AuthService;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.enums.UserActionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthRabbitMsgServiceImpl rabbitMsgService;
    private final LogRabbitMsgServiceImpl logRabbitMsgService;
    private final BookingRabbitMsgServiceImpl bookingRabbitMsgService;

    @Override
    public void registerUser(UserRegistrationDto dto) {
        log.info("Регистрация пользователя: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // --- Booking event (в отдельном методе) ---
        sendBookingEventToQueue(savedUser);

        // --- Auth event ---
        log.info("Создание AuthEvent");
        AuthEvent authEvent = new AuthEvent();
        authEvent.setId(savedUser.getId());
        authEvent.setUsername(savedUser.getUsername());
        authEvent.setEmail(savedUser.getEmail());
        authEvent.setActionType(UserActionType.CREATED);
        log.info("Отправка AuthEvent");
        rabbitMsgService.sendUserRegisteredEvent(authEvent);
        log.info("AuthEvent отправлено");

        // --- Log event ---
        log.info("Создание LogEvent");
        LogEvent logEvent = new LogEvent(
                UUID.randomUUID(),
                EventType.REGISTER_EVENT,
                savedUser.getId().toString(),
                ServiceType.AUTH_SERVICE,
                "User registered successfully"
        );
        log.info("Отправка LogEvent");
        logRabbitMsgService.sendLogEvent(logEvent);
        log.info("LogEvent отправлено");

        log.warn("Пользователь сохранен: {}", savedUser.getEmail());
    }


    public void sendBookingEventToQueue(User user) {
        AuthBookingEvent event = new AuthBookingEvent();
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setEmail(user.getEmail());
        try {
            bookingRabbitMsgService.sendBookingEvent(event);
            log.warn("✅ AuthBookingEvent успешно отправлено!");
        } catch (Exception e) {
            log.error("❌ КРИТИЧЕСКАЯ ОШИБКА при отправке AuthBookingEvent:", e);
        }
    }

    @Override
    public UserResponseDto loginUser(UserLoginDto dto) {
        log.info("Попытка входа: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundByEmailException("Пользователь с таким email не найден."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Неверный пароль.");
        }

        String token = jwtUtil.generateToken(user.getUsername(), List.of(user.getRole()));

        // --- Log event ---
        LogEvent logEvent = new LogEvent(
                UUID.randomUUID(),
                EventType.LOGIN_EVENT,
                user.getId().toString(),
                ServiceType.AUTH_SERVICE,
                "User logged in successfully"
        );
        logRabbitMsgService.sendLogEvent(logEvent);

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}