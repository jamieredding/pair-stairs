package dev.coldhands.pair.stairs.backend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateTeamDto(
        @NotBlank(message = "INVALID_NAME")
        String name,

        @NotBlank(message = "INVALID_SLUG")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "INVALID_SLUG")
        String slug) {
}
