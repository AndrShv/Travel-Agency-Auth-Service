package com.example.project.rest;

import com.example.project.dto.UserDTO;
import com.example.project.enums.Role;
import com.example.project.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/role")
    public void changeRole(@PathVariable UUID id,
                           @RequestParam Role role) {
        userService.changeRole(id, role);
    }

    @PutMapping("/{id}/active")
    public void changeStatus(@PathVariable UUID id,
                             @RequestParam boolean active) {
        userService.changeAccountStatus(id, active);
    }


}

