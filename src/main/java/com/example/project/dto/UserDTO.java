package com.example.project.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class UserDTO {


    private String id;

    @NotNull(message = "Username cannot be null")
    private String username;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email must be in format")
    @NotNull(message = "Email cannot be null")
    private String email;

    private String role;

    @Pattern(regexp = "\\+380\\d{9}", message = "Номер должен быть в формате +380XXXXXXXXX")
    private String phoneNumber;

    private Double balance;

    private boolean active;


}

