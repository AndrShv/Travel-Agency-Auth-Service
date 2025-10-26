package com.example.project.handlers;


import com.example.project.enums.Role;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.custom.CustomOidcUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String email = null;
        if (authentication.getPrincipal() instanceof CustomOidcUser customOidcUser) {
            email = customOidcUser.getEmail();
        }

        if (email != null) {
            Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String identifier = user.getEmail();
                List<Role> roles = List.of(user.getRole());
                String token = jwtUtil.generateToken(identifier, roles);

                Cookie jwtCookie = new Cookie("jwt", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(jwtCookie);

                log.info("✅ JWT выдан и помещён в cookie пользователю {}", email);
            } else {
                log.warn("⚠️ Пользователь с email {} не найден при OAuth2 login", email);
            }
        }

        response.sendRedirect("/main");
    }
}

