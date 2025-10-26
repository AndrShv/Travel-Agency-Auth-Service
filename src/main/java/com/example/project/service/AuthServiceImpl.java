package com.example.project.service;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.Role;
import com.example.project.exceptions.InvalidCredentialsException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.interfaces.AuthService;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public void registerUser(UserRegistrationDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setBalance(BigDecimal.ZERO);
        user.setActive(true);

        User savedUser = userRepository.save(user);

    }

    @Override
    public UserResponseDto loginUser(UserLoginDto dto) {

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new InvalidCredentialsException("Необходимо указать email для входа.");
        }

        User user;
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> {
                        return new UserNotFoundByEmailException("Пользователь с указанным email не найден.");
                    });
        } else {
            throw new InvalidCredentialsException("Необходимо  указать email для входа.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Введен неверный пароль.");
        }

        if (user.getRole() == null) {
            throw new InvalidCredentialsException("Профиль пользователя не настроен (отсутствует роль).");
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