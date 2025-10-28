package com.example.project.controller;


import com.example.project.dto.UserLoginDto;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ---------------- LOGIN ----------------
    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("userLoginDto", new UserLoginDto());
        return "login";
    }

    @PostMapping("/auth/login")
    public String loginSubmit(@ModelAttribute UserLoginDto userLoginDto, Model model) {
        try {
            authService.loginUser(userLoginDto);
            return "redirect:/home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // ---------------- REGISTER ----------------
    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerSubmit(@ModelAttribute UserRegistrationDto userRegistrationDto, Model model) {
        try {
            authService.registerUser(userRegistrationDto);
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}

