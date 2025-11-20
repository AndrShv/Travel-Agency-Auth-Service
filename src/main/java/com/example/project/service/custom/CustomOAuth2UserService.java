package com.example.project.service.custom;

import com.example.project.enums.Role;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();

        if (email == null || email.isBlank()) {
            log.error("Email не получен от OAuth2 провайдера");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        log.info("OAuth2 вход: email={}", normalizedEmail);

        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail);

        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Найден существующий пользователь: {} (id: {})", user.getEmail(), user.getId());

            boolean needsSave = updateUserFromOAuth2(user, oidcUser);

            if (needsSave) {
                user = userRepository.save(user);
                log.info("Пользователь обновлён: {}", user.getEmail());
            }
        } else {
            user = createNewUserFromOAuth2(normalizedEmail, oidcUser);
            log.info("Создан новый пользователь из OAuth2: {} (id: {})", user.getEmail(), user.getId());
        }

        return new CustomOidcUser(oidcUser, user);
    }

    private boolean updateUserFromOAuth2(User user, OidcUser oidcUser) {
        boolean needsSave = false;

        String oauthName = oidcUser.getFullName();
        if (oauthName != null && !oauthName.isBlank()) {
            if (user.getUsername() == null ||
                    user.getUsername().isBlank() ||
                    user.getUsername().equals(user.getEmail()) ||
                    user.getUsername().startsWith("oauth2-")) {

                log.debug("Обновляем username: {} → {}", user.getUsername(), oauthName);
                user.setUsername(oauthName);
                needsSave = true;
            }
        }

        if (user.getRole() == null) {
            log.debug("Устанавливаем роль: USER");
            user.setRole(Role.USER);
            needsSave = true;
        }

        if (!user.isActive()) {
            log.debug("Активируем пользователя");
            user.setActive(true);
            needsSave = true;
        }

        if (user.getBalance() == null) {
            log.debug("Инициализируем баланс: 0");
            user.setBalance(BigDecimal.ZERO);
            needsSave = true;
        }

        return needsSave;
    }

    private User createNewUserFromOAuth2(String email, OidcUser oidcUser) {
        String oauthName = oidcUser.getFullName();
        String username = (oauthName != null && !oauthName.isBlank()) ? oauthName : email;

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        User newUser = User.builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .balance(BigDecimal.ZERO)
                .active(true)
                .role(Role.USER)
                .build();

        log.info("Создаём нового пользователя из OAuth2:");
        log.info("  Email: {}", newUser.getEmail());
        log.info("  Username: {}", newUser.getUsername());
        log.info("  Role: {}", newUser.getRole());

        return userRepository.save(newUser);
    }
}