package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

data class TeamMembershipDto(
    val id: Long,
    val userId: Long,
    val displayName: String,
)
