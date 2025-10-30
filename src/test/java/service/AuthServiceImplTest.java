package service;


import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.Role;
import com.example.project.event.AuthEvent;
import com.example.project.exceptions.InvalidPasswordException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.AuthRabbitMsgServiceImpl;
import com.example.project.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Тесты аутентификации и регистрации")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthRabbitMsgServiceImpl rabbitMsgService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        registrationDto = UserRegistrationDto.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        loginDto = UserLoginDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Регистрация пользователя")
    class RegisterUserTests {

        @Test
        @DisplayName("✓ Успешная регистрация нового пользователя")
        void registerUser_Success() {
            // Arrange
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registrationDto.getPassword()))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            authService.registerUser(registrationDto);

            // Assert
            verify(userRepository, times(1)).existsByEmail(registrationDto.getEmail());
            verify(passwordEncoder, times(1)).encode(registrationDto.getPassword());
            verify(userRepository, times(1)).save(any(User.class));

            // Проверяем, что сохранённый пользователь имеет правильные данные
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(registrationDto.getUsername(), savedUser.getUsername());
            assertEquals(registrationDto.getEmail(), savedUser.getEmail());
            assertEquals(Role.USER, savedUser.getRole());
            assertEquals(BigDecimal.ZERO, savedUser.getBalance());
            assertTrue(savedUser.isActive());
        }

        @Test
        @DisplayName("✗ Ошибка: пользователь с таким email уже существует")
        void registerUser_UserAlreadyExists() {
            // Arrange
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

            // Act & Assert
            UserAlreadyExistsException exception = assertThrows(
                    UserAlreadyExistsException.class,
                    () -> authService.registerUser(registrationDto),
                    "Должно выброситься исключение UserAlreadyExistsException"
            );

            assertEquals("Пользователь с таким email уже существует", exception.getMessage());

            // Проверяем, что save не был вызван
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("✓ Пароль правильно кодируется при регистрации")
        void registerUser_PasswordEncoded() {
            // Arrange
            String rawPassword = "plainPassword123!@#";
            String encodedPassword = "hashedPassword_xyz";
            registrationDto.setPassword(rawPassword);

            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            authService.registerUser(registrationDto);

            // Assert
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
            verify(passwordEncoder).encode(passwordCaptor.capture());
            assertEquals(rawPassword, passwordCaptor.getValue());
        }

        @Test
        @DisplayName("✓ Новый пользователь получает роль USER по умолчанию")
        void registerUser_DefaultRoleIsUser() {
            // Arrange
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            authService.registerUser(registrationDto);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(Role.USER, userCaptor.getValue().getRole());
        }

        @Test
        @DisplayName("✓ Новый пользователь создаётся с нулевым балансом")
        void registerUser_InitialBalanceIsZero() {
            // Arrange
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            authService.registerUser(registrationDto);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(BigDecimal.ZERO, userCaptor.getValue().getBalance());
        }

        @Test
        @DisplayName("✓ Новый пользователь активирован по умолчанию")
        void registerUser_UserActiveByDefault() {
            // Arrange
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            authService.registerUser(registrationDto);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().isActive());
        }
    }

    @Nested
    @DisplayName("Вход пользователя")
    class LoginUserTests {

        @Test
        @DisplayName("✓ Успешный вход с правильным паролем")
        void loginUser_Success() {
            // Arrange
            String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.abc123";

            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole())))
                    .thenReturn(token);

            // Act
            UserResponseDto response = authService.loginUser(loginDto);

            // Assert
            assertNotNull(response);
            assertEquals(testUser.getId(), response.getId());
            assertEquals(testUser.getUsername(), response.getUsername());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals("USER", response.getRole());
            assertEquals(token, response.getToken());

            verify(userRepository, times(1)).findByEmail(loginDto.getEmail());
            verify(passwordEncoder, times(1)).matches(loginDto.getPassword(), testUser.getPassword());
            verify(jwtUtil, times(1)).generateToken(testUser.getUsername(), List.of(testUser.getRole()));
        }

        @Test
        @DisplayName("✗ Ошибка: пользователь не найден")
        void loginUser_UserNotFound() {
            // Arrange
            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundByEmailException exception = assertThrows(
                    UserNotFoundByEmailException.class,
                    () -> authService.loginUser(loginDto),
                    "Должно выброситься исключение UserNotFoundByEmailException"
            );

            assertEquals("Пользователь с таким email не найден.", exception.getMessage());

            // Проверяем, что дальнейшие операции не выполнялись
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtUtil, never()).generateToken(anyString(), anyList());
            verify(rabbitMsgService, never()).sendUserRegisteredEvent(any());
        }

        @Test
        @DisplayName("✗ Ошибка: неверный пароль")
        void loginUser_InvalidPassword() {
            // Arrange
            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword()))
                    .thenReturn(false);

            // Act & Assert
            InvalidPasswordException exception = assertThrows(
                    InvalidPasswordException.class,
                    () -> authService.loginUser(loginDto),
                    "Должно выброситься исключение InvalidPasswordException"
            );

            assertEquals("Неверный пароль.", exception.getMessage());

            // Проверяем, что JWT не был сгенерирован
            verify(jwtUtil, never()).generateToken(anyString(), anyList());
            verify(rabbitMsgService, never()).sendUserRegisteredEvent(any());
        }

        @Test
        @DisplayName("✓ JWT генерируется с username и ролью пользователя")
        void loginUser_JwtGeneratedCorrectly() {
            // Arrange
            String token = "jwt_token_12345";

            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole())))
                    .thenReturn(token);

            // Act
            UserResponseDto response = authService.loginUser(loginDto);

            // Assert
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<List> rolesCaptor = ArgumentCaptor.forClass(List.class);

            verify(jwtUtil).generateToken(usernameCaptor.capture(), rolesCaptor.capture());

            assertEquals(testUser.getUsername(), usernameCaptor.getValue());
            assertEquals(List.of(testUser.getRole()), rolesCaptor.getValue());
            assertEquals(token, response.getToken());
        }

        @Test
        @DisplayName("✓ Ответ содержит все необходимые данные пользователя")
        void loginUser_ResponseComplete() {
            // Arrange
            String token = "eyJhbGciOiJIUzUxMiJ9";
            testUser.setRole(Role.ADMIN);

            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser.getUsername(), List.of(Role.ADMIN)))
                    .thenReturn(token);

            // Act
            UserResponseDto response = authService.loginUser(loginDto);

            // Assert
            assertNotNull(response.getId());
            assertNotNull(response.getUsername());
            assertNotNull(response.getEmail());
            assertNotNull(response.getRole());
            assertNotNull(response.getToken());

            assertEquals(testUser.getId(), response.getId());
            assertEquals("testuser", response.getUsername());
            assertEquals("test@example.com", response.getEmail());
            assertEquals("ADMIN", response.getRole());
            assertEquals(token, response.getToken());
        }

        @Test
        @DisplayName("✓ Пароль проверяется правильно с закодированным паролем в БД")
        void loginUser_PasswordMatchingVerified() {
            // Arrange
            String rawPassword = "myPassword123";
            String hashedPassword = "hashed_xyz_abc";

            loginDto.setPassword(rawPassword);
            testUser.setPassword(hashedPassword);

            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(rawPassword, hashedPassword))
                    .thenReturn(true);
            when(jwtUtil.generateToken(anyString(), anyList()))
                    .thenReturn("token");

            // Act
            authService.loginUser(loginDto);

            // Assert
            verify(passwordEncoder).matches(rawPassword, hashedPassword);
        }

        @Test
        @DisplayName("✓ Входные параметры не модифицируются")
        void loginUser_InputNotModified() {
            // Arrange
            String originalEmail = loginDto.getEmail();
            String originalPassword = loginDto.getPassword();

            when(userRepository.findByEmail(originalEmail))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(originalPassword, testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(anyString(), anyList()))
                    .thenReturn("token");

            // Act
            authService.loginUser(loginDto);

            // Assert
            assertEquals(originalEmail, loginDto.getEmail());
            assertEquals(originalPassword, loginDto.getPassword());
        }
    }

    @Nested
    @DisplayName("Интеграционные сценарии")
    class IntegrationTests {

        @Test
        @DisplayName("✓ Пользователь может зарегистрироваться и затем войти")
        void registerAndLogin_Success() {
            // Arrange - регистрация
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registrationDto.getPassword()))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act - регистрация
            authService.registerUser(registrationDto);

            // Assert - проверяем регистрацию
            verify(userRepository).save(any(User.class));

            // Arrange - вход
            when(userRepository.findByEmail(loginDto.getEmail()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole())))
                    .thenReturn("jwt_token");

            // Act - вход
            UserResponseDto response = authService.loginUser(loginDto);

            // Assert - проверяем вход
            assertNotNull(response);
            assertEquals("jwt_token", response.getToken());
        }
    }
}
