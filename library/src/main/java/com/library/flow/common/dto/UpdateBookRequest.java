package com.library.flow.common.dto;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

public record UpdateBookRequest(

        String title,

        @Min(value = 1450, message = "Publication year must be valid")
        @Max(value = 2100, message = "Publication year must be valid")
        Integer publicationYear,

        String language,


        Integer edition,

        @Size(max = 2000, message = "Summary must not exceed 2000 characters")
        String summary,

        String coverImageUrl,

        Integer totalCopies,

        Integer availableCopies,

        UUID publisherId,

        Set<UUID> authorIds,

        Set<UUID> categoryIds
) {}
