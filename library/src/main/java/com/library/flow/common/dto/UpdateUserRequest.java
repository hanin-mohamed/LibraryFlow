package com.library.flow.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank String fullName,
        @NotBlank String username,
        @Email @NotBlank String email,
        Boolean enabled
) {}
