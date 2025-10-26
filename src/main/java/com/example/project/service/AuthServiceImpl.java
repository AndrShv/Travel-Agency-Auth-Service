package com.example.project.service;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.Role;
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

    @Override
    public void registerUser(UserRegistrationDto dto) {
        log.info("Регистрация пользователя: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        log.info("Создание нового пользователя с email: {}", dto.getEmail());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);


        log.info("Попытка сохранить пользователя: {}", user.getUsername(), dto.getEmail(), user.getRole());

        User savedUser = userRepository.save(user);

        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
    }

    @Override
    public UserResponseDto loginUser(UserLoginDto dto) {
        log.info("Попытка входа пользователя: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UserNotFoundByEmailException("Пользователь с указанным email не найден."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Введен неверный пароль.");
        }

        if (user.getRole() == null) {
            throw new InvalidUserRoleException("Профиль пользователя не настроен (отсутствует роль).");
        }

        String token = jwtUtil.generateToken(user.getUsername(), List.of(user.getRole()));

        log.info("Пользователь {} успешно вошел", user.getEmail());
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}
