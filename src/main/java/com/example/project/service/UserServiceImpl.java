package com.example.project.service;



import com.example.project.dto.UserDTO;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.enums.Role;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.exceptions.UserNotFoundByIDException;
import com.example.project.exceptions.UserNotFoundByNameException;
import com.example.project.interfaces.UserService;
import com.example.project.mapper.UserMapper;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Попытка получить список всех пользователей");
        List<UserDTO> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserDTO)
                .toList();
        log.info("✅ Успешно получено {} пользователей", users.size());
        return users;
    }


    @Override
    public UserDTO register(UserRegistrationDto userRegistrationDto) {
        log.info("Попытка регистрации пользователя (Service layer): username={}", userRegistrationDto.getUsername());

        User user = userMapper.toUser(userRegistrationDto);
        user.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("✅ Пользователь успешно зарегистрирован (Service layer): ID={}, Username={}",
                savedUser.getId(), savedUser.getUsername());
        return userMapper.toUserDTO(savedUser);
    }


    @Override
    public UserDTO updateUser(String username, UserDTO userDTO) {
        log.info("Попытка обновить данные пользователя: Username={}, New Phone={}, New Role={}",
                username, userDTO.getPhoneNumber(), userDTO.getRole());

        UserDTO updatedDto = userRepository.findUserByUsername(username)
                .map(existingUser -> {
                    log.debug("Обновление полей для пользователя {}: Role from {} to {}",
                            username, existingUser.getRole(), userDTO.getRole());

                    existingUser.setUsername(userDTO.getUsername());

                    if (userDTO.getBalance() != null) {
                        existingUser.setBalance(BigDecimal.valueOf(userDTO.getBalance()));
                    }
                    if (userDTO.getRole() != null) {
                        existingUser.setRole(Role.valueOf(userDTO.getRole()));
                    }
                    User updated = userRepository.save(existingUser);
                    log.info("✅ Пользователь успешно обновлен: ID={}, Username={}", updated.getId(), updated.getUsername());
                    return userMapper.toUserDTO(updated);
                })
                .orElseThrow(() -> {
                    log.warn("Обновление отклонено: Пользователь с именем {} не найден", username);
                    return new UserNotFoundByNameException("Пользователь с именем " + username + " не найден для обновления.");
                });
        return updatedDto;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.info("Попытка получить пользователя по Username: {}", username);
        return userRepository.findUserByUsername(username)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> {
                    log.warn("Пользователь с именем {} не найден", username);
                    return new UserNotFoundByNameException("Пользователь с именем " + username + " не найден.");
                });
    }


    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Попытка получить пользователя по ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toUserDTO)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundByIDException("Пользователь с ID " + id + " не найден.");
                });
    }

    @Override
    public List<UserDTO> searchUsers(String query) {
        log.info("Попытка поиска пользователей по запросу: '{}'", query);
        List<UserDTO> foundUsers = userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(userMapper::toUserDTO)
                .toList();
        log.info("✅ Поиск завершен. Найдено {} пользователей по запросу: '{}'", foundUsers.size(), query);
        return foundUsers;
    }


    @Override
    public User findByEmail(String email) {
        log.info("Попытка найти сущность User по Email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Сущность User с email '{}' не найдена", email);
                    return new UserNotFoundByEmailException("Пользователь с email '" + email + "' не найден.");
                });
    }

    @Override
    public User findByEmailOrName(String query) {
        log.info("Попытка найти сущность User по Email или Username: {}", query);

        User user = userRepository.findByEmail(query)
                .orElseGet(() -> userRepository.findByUsername(query)
                        .orElseThrow(() -> {
                            log.warn("Сущность User с email или именем '{}' не найдена", query);
                            return new UserNotFoundByEmailException("Пользователь с email или именем '" + query + "' не найден.");
                        })
                );

        log.info("✅ Сущность User найдена: ID={}, Username={}", user.getId(), user.getUsername());
        return user;
    }
    @Override
    public UserDTO changeAccountStatus(UUID id, boolean active) {
        log.info("Попытка изменить статус аккаунта для User ID: {}, New Status: {}", id, active);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundByIDException("Пользователь с ID " + id + " не найден для изменения статуса.");
                });

        user.setActive(active);
        User updatedUser = userRepository.save(user);

        log.info("✅ Статус аккаунта успешно изменен: ID={}, Username={}, IsActive={}",
                updatedUser.getId(), updatedUser.getUsername(), updatedUser.isActive());

        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    public UserDTO changeRole(UUID id, Role role) {
        log.info("Попытка изменить роль пользователя ID: {}, New Role: {}", id, role);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден", id);
                    return new UserNotFoundByIDException("Пользователь с ID " + id + " не найден для изменения роли.");
                });

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        log.info("✅ Роль пользователя успешно изменена: ID={}, Username={}, Role={}",
                updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRole());

        return userMapper.toUserDTO(updatedUser);
    }

}