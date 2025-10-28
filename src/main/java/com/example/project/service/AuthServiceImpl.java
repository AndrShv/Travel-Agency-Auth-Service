package com.example.project.service;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.Role;
import com.example.project.event.AuthEvent;
import com.example.project.exceptions.*;
import com.example.project.interfaces.AuthService;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthRabbitMsgServiceImpl rabbitMsgService;

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

        AuthEvent event = new AuthEvent();
        event.setId(savedUser.getId());
        event.setUsername(savedUser.getUsername());
        event.setEmail(savedUser.getEmail());
        rabbitMsgService.sendUserRegisteredEvent(event);

        log.info("Событие отправлено в RabbitMQ: {}", event);

        log.info("Пользователь сохранен: {}", savedUser.getEmail());
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

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}
