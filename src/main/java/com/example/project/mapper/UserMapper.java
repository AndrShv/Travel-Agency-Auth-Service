package com.example.project.mapper;


import com.example.project.dto.UserDTO;
import com.example.project.dto.UserRegistrationDto;
import com.example.project.enums.Role;
import com.example.project.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "toRole")
    User toUser(UserDTO userDTO);

    @Mapping(target = "role", source = "role", qualifiedByName = "toRole")
    User toUser(UserRegistrationDto dto);

    @Mapping(target = "role", source = "role", qualifiedByName = "fromRole")
    UserDTO toUserDTO(User user);

    @Named("toRole")
    default Role toRole(String role) {
        return role == null ? null : Role.valueOf(role);
    }

    @Named("fromRole")
    default String fromRole(Role role) {
        return role == null ? null : role.name();
    }
}

