package com.example.project.service.custom;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalized = identifier.trim().toLowerCase(Locale.ROOT);

        log.debug("Попытка загрузить пользователя по identifier: {}", identifier);

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(normalized);

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsernameIgnoreCase(normalized);
        }

        User user = userOpt.orElseThrow(() -> {
            log.warn("Пользователь не найден ни по email, ни по username: {}", identifier);
            return new UsernameNotFoundException("Пользователь не найден: " + identifier);
        });

        if (!user.isActive()) {
            log.warn("Пользователь неактивен: {}", user.getEmail());
            throw new UsernameNotFoundException("Пользователь неактивен: " + identifier);
        }

        log.debug("Пользователь загружен успешно: {} (email: {}, id: {})",
                user.getUsername(), user.getEmail(), user.getId());

        return new CustomUserDetails(user);
    }
}