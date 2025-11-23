package service;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.enums.EventType;
import com.example.project.enums.Role;
import com.example.project.enums.ServiceType;
import com.example.project.enums.UserActionType;
import com.example.project.event.AuthEvent;
import com.example.project.event.LogEvent;
import com.example.project.exceptions.InvalidPasswordException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.jwt.JwtUtil;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.AuthRabbitMsgServiceImpl;
import com.example.project.service.AuthServiceImpl;
import com.example.project.service.LogRabbitMsgServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthRabbitMsgServiceImpl rabbitMsgService;

    @Mock
    private LogRabbitMsgServiceImpl logRabbitMsgService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private User testUser;
    private UUID expectedUuid; // Добавлено для хранения конкретного UUID
    private String expectedUuidString; // Добавлено для строкового представления

    @BeforeEach
    void setUp() {
        // Устанавливаем конкретный UUID, который мы ожидаем
        expectedUuid = UUID.fromString("e63fc7d5-8464-471a-bd3e-ba60a157ca00");
        expectedUuidString = expectedUuid.toString();

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
                // Используем фиксированный UUID
                .id(expectedUuid)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.USER)
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.registerUser(registrationDto);

        // Assert
        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(userRepository).save(any(User.class));
        verify(rabbitMsgService).sendUserRegisteredEvent(any(AuthEvent.class));
        verify(logRabbitMsgService).sendLogEvent(any(LogEvent.class));
    }

    @Test
    @DisplayName("Регистрация: пользователь с таким email уже существует")
    void testRegisterUser_UserAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(registrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Пользователь с таким email уже существует");

        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userRepository, never()).save(any());
        verify(rabbitMsgService, never()).sendUserRegisteredEvent(any());
        verify(logRabbitMsgService, never()).sendLogEvent(any());
    }

    @Test
    @DisplayName("Регистрация: проверка сохранения корректных данных пользователя")
    void testRegisterUser_UserDataCorrectness() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        authService.registerUser(registrationDto);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser)
                .isNotNull()
                .extracting("username", "email", "password", "role", "active")
                .containsExactly(
                        "testuser",
                        "test@example.com",
                        "encoded_password",
                        Role.USER,
                        true
                );
        assertThat(savedUser.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Регистрация: проверка отправленного AuthEvent")
    void testRegisterUser_AuthEventCorrectness() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ArgumentCaptor<AuthEvent> eventCaptor = ArgumentCaptor.forClass(AuthEvent.class);

        // Act
        authService.registerUser(registrationDto);

        // Assert
        verify(rabbitMsgService).sendUserRegisteredEvent(eventCaptor.capture());
        AuthEvent authEvent = eventCaptor.getValue();

        assertThat(authEvent)
                .isNotNull()
                .extracting("id", "username", "email", "actionType")
                .containsExactly(
                        // Исправлено: ожидаем объект UUID, а не 1L
                        expectedUuid,
                        "testuser",
                        "test@example.com",
                        UserActionType.CREATED
                );
    }

    @Test
    @DisplayName("Регистрация: проверка отправленного LogEvent")
    void testRegisterUser_LogEventCorrectness() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ArgumentCaptor<LogEvent> logEventCaptor = ArgumentCaptor.forClass(LogEvent.class);

        // Act
        authService.registerUser(registrationDto);

        // Assert
        verify(logRabbitMsgService).sendLogEvent(logEventCaptor.capture());
        LogEvent logEvent = logEventCaptor.getValue();

        assertThat(logEvent)
                .isNotNull()
                .extracting("eventType", "userId", "serviceType", "details")
                .containsExactly(
                        EventType.REGISTER_EVENT,
                        // Исправлено: ожидаем String (UUID), а не "1"
                        expectedUuidString,
                        ServiceType.AUTH_SERVICE,
                        "User registered successfully"
                );
    }

    @Test
    @DisplayName("Успешный вход пользователя")
    void testLoginUser_Success() {
        // Arrange
        String token = "jwt_token_123";
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole()))).thenReturn(token);

        // Act
        UserResponseDto response = authService.loginUser(loginDto);

        // Assert
        assertThat(response)
                .isNotNull()
                .extracting("id", "username", "email", "role", "token")
                .containsExactly(
                        // Исправлено: ожидаем объект UUID, а не строку
                        expectedUuid,
                        "testuser",
                        "test@example.com",
                        "USER",
                        token
                );

        verify(userRepository).findByEmail(loginDto.getEmail());
        verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPassword());
        verify(jwtUtil).generateToken(testUser.getUsername(), List.of(testUser.getRole()));
        verify(logRabbitMsgService).sendLogEvent(any(LogEvent.class));
    }

    @Test
    @DisplayName("Вход: пользователь не найден")
    void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.loginUser(loginDto))
                .isInstanceOf(UserNotFoundByEmailException.class)
                .hasMessage("Пользователь с таким email не найден.");

        verify(userRepository).findByEmail(loginDto.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(logRabbitMsgService, never()).sendLogEvent(any());
    }

    @Test
    @DisplayName("Вход: неверный пароль")
    void testLoginUser_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.loginUser(loginDto))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Неверный пароль.");

        verify(userRepository).findByEmail(loginDto.getEmail());
        verify(passwordEncoder).matches(loginDto.getPassword(), testUser.getPassword());
        verify(jwtUtil, never()).generateToken(any(), any());
        verify(logRabbitMsgService, never()).sendLogEvent(any());
    }

    @Test
    @DisplayName("Вход: проверка отправленного LogEvent")
    void testLoginUser_LogEventCorrectness() {
        // Arrange
        String token = "jwt_token_123";
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole()))).thenReturn(token);

        ArgumentCaptor<LogEvent> logEventCaptor = ArgumentCaptor.forClass(LogEvent.class);

        // Act
        authService.loginUser(loginDto);

        // Assert
        verify(logRabbitMsgService).sendLogEvent(logEventCaptor.capture());
        LogEvent logEvent = logEventCaptor.getValue();

        assertThat(logEvent)
                .isNotNull()
                .extracting("eventType", "userId", "serviceType", "details")
                .containsExactly(
                        EventType.LOGIN_EVENT,
                        // Исправлено: ожидаем String (UUID), а не "1"
                        expectedUuidString,
                        ServiceType.AUTH_SERVICE,
                        "User logged in successfully"
                );
    }

    @Test
    @DisplayName("Вход: проверка генерации токена с правильными параметрами")
    void testLoginUser_TokenGenerationParams() {
        // Arrange
        String token = "jwt_token_123";
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginDto.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getUsername(), List.of(testUser.getRole()))).thenReturn(token);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> rolesCaptor = ArgumentCaptor.forClass(List.class);

        // Act
        authService.loginUser(loginDto);

        // Assert
        verify(jwtUtil).generateToken(usernameCaptor.capture(), rolesCaptor.capture());

        assertThat(usernameCaptor.getValue()).isEqualTo("testuser");
        assertThat(rolesCaptor.getValue()).containsExactly(Role.USER);
    }
}