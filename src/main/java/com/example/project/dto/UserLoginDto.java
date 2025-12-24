package com.example.project.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String password;
}
