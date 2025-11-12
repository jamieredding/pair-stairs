package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembership
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamMembershipDto

fun TeamMembership.toDto() = TeamMembershipDto(
    id = id.value,
    userId = userId.value,
    displayName = displayName
)

fun TeamMembershipEntity.toDomain() = TeamMembership(
    id = TeamMembershipId( id ?: error("TeamMembershipEntity has no id, likely it hasn't been persisted yet")),
    displayName = displayName,
    userId = user.toDomain().id,
    teamId = team.toDomain().id,
    createdAt = createdAt,
    updatedAt = updatedAt
)