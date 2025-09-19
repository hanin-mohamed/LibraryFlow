package com.library.flow.common.dto;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

public record CreateBookRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Publication year is required")
        @Min(value = 1450, message = "Publication year must be valid")
        @Max(value = 2100, message = "Publication year must be valid")
        Integer publicationYear,

        @NotBlank(message = "Language is required")
        String language,

        Integer edition,

        @Size(max = 2000, message = "Summary must not exceed 2000 characters")
        String summary,

        String coverImageUrl,

        @NotNull(message = "Total copies is required")
        @Min(value = 0, message = "Total copies must be >= 0")
        Integer totalCopies,

        @Min(value = 0, message = "Available copies must be >= 0")
        Integer availableCopies,

        UUID publisherId,

        Set<UUID> authorIds,

        Set<UUID> categoryIds
) {}
