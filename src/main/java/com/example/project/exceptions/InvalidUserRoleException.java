package com.example.project.exceptions;

public class InvalidUserRoleException extends RuntimeException{
    public InvalidUserRoleException(String message) {
        super(message);
    }
}
