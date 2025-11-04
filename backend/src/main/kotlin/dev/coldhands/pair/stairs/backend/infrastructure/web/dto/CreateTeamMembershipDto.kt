package dev.coldhands.pair.stairs.backend.infrastructure.web.dto

import dev.coldhands.pair.stairs.backend.domain.UserId

data class CreateTeamMembershipDto(
    val userId: UserId
)
