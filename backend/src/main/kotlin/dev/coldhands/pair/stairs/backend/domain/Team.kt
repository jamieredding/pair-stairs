package dev.coldhands.pair.stairs.backend.domain

import java.time.Instant

data class Team(
    val id: TeamId,
    val name: String,
    val slug: Slug,

    val createdAt: Instant,
    val updatedAt: Instant,
)