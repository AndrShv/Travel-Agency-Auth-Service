package rest;

import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;
import com.example.project.exceptions.InvalidPasswordException;
import com.example.project.exceptions.UserAlreadyExistsException;
import com.example.project.exceptions.UserNotFoundByEmailException;
import com.example.project.interfaces.AuthService;
import com.example.project.rest.AuthRestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = com.example.project.AuthApplication.class)
@AutoConfigureMockMvc
@DisplayName("AuthRestController - REST API тесты")
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDto registrationDto;
    private UserLoginDto loginDto;
    private UserResponseDto responseDto;

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

        responseDto = UserResponseDto.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .token("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.abc123")
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/register - Регистрация")
    class RegisterTests {

        @Test
        @DisplayName("✓ Успешная регистрация с корректными данными")
        void register_Success() throws Exception {
            // Arrange
            doNothing().when(authService).registerUser(any(UserRegistrationDto.class));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Пользователь успешно зарегистрирован"));

            verify(authService, times(1)).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: пользователь с таким email уже существует")
        void register_UserAlreadyExists() throws Exception {
            // Arrange
            doThrow(new UserAlreadyExistsException("Пользователь с таким email уже существует"))
                    .when(authService).registerUser(any(UserRegistrationDto.class));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("уже существует")));

            verify(authService, times(1)).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: некорректный формат email")
        void register_InvalidEmail() throws Exception {
            // Arrange
            registrationDto.setEmail("invalid-email");

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: отсутствует поле username")
        void register_MissingUsername() throws Exception {
            // Arrange
            registrationDto.setUsername(null);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: отсутствует поле email")
        void register_MissingEmail() throws Exception {
            // Arrange
            registrationDto.setEmail(null);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: отсутствует поле password")
        void register_MissingPassword() throws Exception {
            // Arrange
            registrationDto.setPassword(null);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 415: неподдерживаемый Content-Type")
        void register_UnsupportedMediaType() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("invalid"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: пустой request body")
        void register_EmptyBody() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✓ Пароль с минимальной длиной (6 символов)")
        void register_MinimalPassword() throws Exception {
            // Arrange
            registrationDto.setPassword("pass12");
            doNothing().when(authService).registerUser(any(UserRegistrationDto.class));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isOk());

            verify(authService, times(1)).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✓ Username с пробелами и спецсимволами")
        void register_UsernameWithSpecialChars() throws Exception {
            // Arrange
            registrationDto.setUsername("Test User 123_-.");
            doNothing().when(authService).registerUser(any(UserRegistrationDto.class));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isOk());

            verify(authService, times(1)).registerUser(any(UserRegistrationDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: неверный JSON")
        void register_InvalidJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).registerUser(any(UserRegistrationDto.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - Вход")
    class LoginTests {

        @Test
        @DisplayName("✓ Успешный вход с правильными учётными данными")
        void login_Success() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act & Assert
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.token").exists())
                    .andReturn();

            verify(authService, times(1)).loginUser(any(UserLoginDto.class));

            String responseBody = result.getResponse().getContentAsString();
            UserResponseDto actualResponse = objectMapper.readValue(responseBody, UserResponseDto.class);
            assert actualResponse.getId() != null;
            assert actualResponse.getToken() != null;
        }

        @Test
        @DisplayName("✗ Ошибка 401: пользователь не найден")
        void login_UserNotFound() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenThrow(new UserNotFoundByEmailException("Пользователь с таким email не найден."));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(containsString("не найден")));

            verify(authService, times(1)).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 401: неверный пароль")
        void login_InvalidPassword() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenThrow(new InvalidPasswordException("Неверный пароль."));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Неверный пароль."));

            verify(authService, times(1)).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: отсутствует поле email")
        void login_MissingEmail() throws Exception {
            // Arrange
            loginDto.setEmail(null);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: отсутствует поле password")
        void login_MissingPassword() throws Exception {
            // Arrange
            loginDto.setPassword(null);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: некорректный формат email")
        void login_InvalidEmail() throws Exception {
            // Arrange
            loginDto.setEmail("not-an-email");

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✓ Email и пароль переданы в сервис без изменений")
        void login_ParametersPassedCorrectly() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk());

            // Assert
            verify(authService).loginUser(argThat(dto ->
                    dto.getEmail().equals("test@example.com") &&
                            dto.getPassword().equals("password123")
            ));
        }

        @Test
        @DisplayName("✓ Ответ содержит ID пользователя")
        void login_ResponseContainsId() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.id").isNotEmpty());

            verify(authService, times(1)).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 415: неподдерживаемый Content-Type")
        void login_UnsupportedMediaType() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("invalid"))
                    .andExpect(status().isUnsupportedMediaType());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✗ Ошибка 400: пустой request body")
        void login_EmptyBody() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✓ JWT токен присутствует в ответе и не пуст")
        void login_TokenInResponse() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act & Assert
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            UserResponseDto actual = objectMapper.readValue(responseBody, UserResponseDto.class);
            assert actual.getToken().length() > 0;
        }

        @Test
        @DisplayName("✗ Ошибка 400: неверный JSON")
        void login_InvalidJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid"))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).loginUser(any(UserLoginDto.class));
        }

        @Test
        @DisplayName("✓ Роль USER правильно отображается в ответе")
        void login_RoleInResponse() throws Exception {
            // Arrange
            responseDto.setRole("ADMIN");
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            verify(authService, times(1)).loginUser(any(UserLoginDto.class));
        }
    }

    @Nested
    @DisplayName("Общие HTTP тесты")
    class HttpTests {

        @Test
        @DisplayName("✗ POST запрос на несуществующий endpoint")
        void notFound_InvalidEndpoint() throws Exception {
            mockMvc.perform(post("/api/auth/invalid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("✓ Правильный Content-Type в успешном ответе регистрации")
        void register_ResponseContentType() throws Exception {
            // Arrange
            doNothing().when(authService).registerUser(any(UserRegistrationDto.class));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"));
        }

        @Test
        @DisplayName("✓ Правильный Content-Type в ответе входа")
        void login_ResponseContentType() throws Exception {
            // Arrange
            when(authService.loginUser(any(UserLoginDto.class)))
                    .thenReturn(responseDto);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}