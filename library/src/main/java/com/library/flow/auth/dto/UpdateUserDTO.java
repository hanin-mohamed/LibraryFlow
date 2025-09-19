package com.library.flow.auth.dto;


import com.library.flow.auth.model.Role;

public record UpdateUserDTO(
        String fullName,
        String username,
        Role role
){}