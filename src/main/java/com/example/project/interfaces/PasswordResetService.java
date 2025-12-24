package com.example.project.interfaces;

public interface PasswordResetService {

    String sendResetToken(String email);

    void resetPassword(String token, String newPassword);
}
