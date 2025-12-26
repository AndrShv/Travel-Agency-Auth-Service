package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.project")
public class AuthApplication {
    public static void main(String[] args) {
        System.out.println(">>> STARTING SPRING <<<");
        SpringApplication.run(AuthApplication.class, args);
        System.out.println(">>> SPRING STARTED <<<");
    }
}
