package com.library.flow.auth.dto;

import com.library.flow.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
        @NotBlank String username,
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @Size(min = 6) String password,
        @NotNull Role role
) {}
