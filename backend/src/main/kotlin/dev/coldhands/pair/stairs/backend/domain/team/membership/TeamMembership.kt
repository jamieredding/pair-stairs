package dev.coldhands.pair.stairs.backend.domain.team.membership

import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.UserId
import java.time.Instant

data class TeamMembership(
    val id: TeamMembershipId,
    val displayName: String,
    val userId: UserId,
    val teamId: TeamId,

    val createdAt: Instant,
    val updatedAt: Instant,
)