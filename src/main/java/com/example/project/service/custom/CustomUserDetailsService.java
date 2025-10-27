package com.example.project.service.custom;


import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + identifier));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Пользователь неактивен: " + identifier);
        }

        return new CustomUserDetails(user);
    }

}

