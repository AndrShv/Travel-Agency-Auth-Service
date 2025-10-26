package com.example.project.interfaces;


import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.dto.UserResponseDto;

public interface AuthService {
    void registerUser(UserRegistrationDto dto) throws Exception;
    UserResponseDto loginUser(UserLoginDto dto) throws Exception;


}
