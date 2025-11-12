package dev.coldhands.pair.stairs.backend.domain.team

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import java.time.Instant

data class Team(
    val id: TeamId,
    val name: String,
    val slug: Slug,

    val createdAt: Instant,
    val updatedAt: Instant,
)