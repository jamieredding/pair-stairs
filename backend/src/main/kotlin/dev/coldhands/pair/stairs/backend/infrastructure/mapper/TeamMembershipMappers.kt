package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamMembershipDto

fun TeamMembershipEntity.toDto() = TeamMembershipDto(
    id = id
        ?: throw IllegalArgumentException("Cannot create TeamMembershipDto when id is null. Likely, the TeamMembershipEntity hasn't been persisted yet."),
    userId = user.id
        ?: throw IllegalArgumentException("Cannot create TeamMembershipDto when user has null id. Likely, the UserEntity hasn't been persisted yet."),
    displayName = displayName,
)