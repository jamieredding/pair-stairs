package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateTeamDto(
    @NotBlank(message = "INVALID_NAME")
    val name: String,

    @NotBlank(message = "INVALID_SLUG")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "INVALID_SLUG")
    val slug: String
)