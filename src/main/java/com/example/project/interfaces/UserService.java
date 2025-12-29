package com.example.project.interfaces;


import com.example.project.dto.UserDTO;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.enums.Role;
import com.example.project.model.User;

import java.util.List;
import java.util.UUID;



public interface UserService {
    UserDTO register(UserRegistrationDto userRegistrationDto);

    UserDTO updateUser(String username, UserDTO userDTO);

    UserDTO getUserByUsername(String username);

    UserDTO getUserById(UUID id);

    List<UserDTO> searchUsers(String query);

    User findByEmail(String email);

    User findByEmailOrName(String query);

    List<UserDTO> getAllUsers();

    UserDTO changeAccountStatus(UUID id, boolean active);

    UserDTO changeRole(UUID id, Role role);


}

