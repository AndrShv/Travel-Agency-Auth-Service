package com.example.project.controller;

import com.example.project.dto.ForgotPasswordDto;
import com.example.project.dto.ResetPasswordDto;
import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.interfaces.AuthService;
import com.example.project.interfaces.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    // ---------------- LOGIN ----------------
    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("userLoginDto", new UserLoginDto());
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String loginSubmit(@ModelAttribute UserLoginDto userLoginDto, Model model) {
        try {
            authService.loginUser(userLoginDto);
            return "redirect:http://localhost:8082/main";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    // ---------------- REGISTER ----------------
    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerSubmit(@ModelAttribute UserRegistrationDto userRegistrationDto, Model model) {
        try {
            authService.registerUser(userRegistrationDto);
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // ---------------- FORGOT PASSWORD ----------------
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@ModelAttribute("forgotPasswordDto") ForgotPasswordDto dto, Model model) {
        try {
            passwordResetService.sendResetToken(dto.getEmail());
            model.addAttribute("resetPasswordDto", new ResetPasswordDto());
            model.addAttribute("email", dto.getEmail());
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("forgotPasswordDto", dto);
            model.addAttribute("error", e.getMessage());
            return "auth/forgot-password";
        }
    }

    // ---------------- RESET PASSWORD ----------------
    @GetMapping("/auth/reset-password")
    public String resetPasswordForm(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDto dto = new ResetPasswordDto();
        model.addAttribute("resetPasswordDto", dto);
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String processResetPassword(@ModelAttribute("resetPasswordDto") ResetPasswordDto dto, Model model) {
        try {
            passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("resetPasswordDto", dto);
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }

}
