package com.example.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;
}

