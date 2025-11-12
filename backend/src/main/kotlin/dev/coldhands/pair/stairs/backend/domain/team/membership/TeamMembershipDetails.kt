package dev.coldhands.pair.stairs.backend.domain.team.membership

import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.UserId

data class TeamMembershipDetails(
    val displayName: String,
    val userId: UserId,
    val teamId: TeamId,
)
