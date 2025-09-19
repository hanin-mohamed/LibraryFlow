package com.library.flow.common.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(
        @NotBlank(message = "Author name is required")
        String name
) {}
