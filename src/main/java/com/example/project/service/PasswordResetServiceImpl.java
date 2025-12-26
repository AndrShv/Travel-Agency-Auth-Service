package com.example.project.service;

import com.example.project.exceptions.EmailSendingExeption;
import com.example.project.exceptions.InvalidTokenException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.interfaces.PasswordResetService;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@AllArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private static final Random random = new Random();
    private static final long TOKEN_LIFETIME_MINUTES = 1;

    @Override
    public String sendResetToken(String email) {
        log.info("Попытка сгенерировать и отправить токен для сброса пароля на email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Сброс пароля отклонен: Пользователь с email {} не найден", email);
                    return new UserNotFoundByEmailException("Пользователь с email " + email + " не найден.");
                });

        int code = 100000 + random.nextInt(900000);
        String token = String.valueOf(code);

        user.setResetToken(token);
        user.setResetTokenCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.debug("Для пользователя {} сохранён одноразовый токен сброса: {}", email, token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Сброс пароля");
        message.setText("Ваш код для сброса пароля: " + token);

        try {
            mailSender.send(message);
            log.info("✅ Код для сброса пароля успешно отправлен на: {}", email);
        } catch (Exception e) {
            log.error("⚠️ Ошибка при отправке email с токеном сброса на {}: {}", email, e.getMessage(), e);
            throw new EmailSendingExeption("Не удалось отправить email с кодом сброса.", e);
        }

        return token;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Попытка сбросить пароль с использованием токена: {}", token);

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> {
                    log.warn("Сброс пароля отклонен: Предоставленный токен недействителен: {}", token);
                    return new InvalidTokenException("Предоставленный код недействителен или устарел.");
                });
        if (user.getResetTokenCreatedAt().plusMinutes(TOKEN_LIFETIME_MINUTES).isBefore(LocalDateTime.now())) {
            log.warn("Сброс пароля отклонен: Токен {} истёк", token);
            throw new InvalidTokenException("Код для сброса пароля истёк.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenCreatedAt(null);
        userRepository.save(user);

        log.info("Пароль успешно обновлен для пользователя: {}", user.getEmail());
    }
}
