package com.example.project.service.custom;


import com.example.project.enums.Role;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
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
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var allUsers = userRepository.findAll();

        for (User u : allUsers) {
            System.out.println("   📌 БД: email='" + u.getEmail() + "' username='" + u.getUsername() + "'");
        }

        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail);


        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            boolean needsSave = updateUserFromGoogle(user, oidcUser);

            if (needsSave) {
                user = userRepository.save(user);
            }

        } else {
            user = createNewUser(normalizedEmail, oidcUser);
        }

        return new CustomOidcUser(oidcUser, user);
    }

    private boolean updateUserFromGoogle(User user, OidcUser oidcUser) {
        boolean needsSave = false;
        String googleName = oidcUser.getFullName();
        if (googleName != null && !googleName.isBlank()) {
            if (user.getUsername() == null || user.getUsername().isBlank() ||
                    user.getUsername().equals(user.getEmail()) ||
                    user.getUsername().startsWith("oauth2-")) {

                user.setUsername(googleName);
                needsSave = true;
            }
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
            needsSave = true;
        }
        if (!user.isActive()) {
            user.setActive(true);
            needsSave = true;
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
            needsSave = true;
        }

        return needsSave;
    }

    private User createNewUser(String email, OidcUser oidcUser) {

        String googleName = oidcUser.getFullName();
        String username = (googleName != null) ? googleName : email;

        User newUser = User.builder()
                .email(email)
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .balance(BigDecimal.ZERO)
                .active(true)
                .role(Role.USER)
                .build();

        System.out.println("➕ Создаём нового пользователя:");
        System.out.println("   Email: " + newUser.getEmail());
        System.out.println("   Username: " + newUser.getUsername());

        return userRepository.save(newUser);
    }

}
