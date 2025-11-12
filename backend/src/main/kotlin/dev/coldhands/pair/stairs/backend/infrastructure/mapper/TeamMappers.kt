package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto

fun Team.toDto(): TeamDto = TeamDto(
    id = id.value,
    name = name,
    slug = slug.value
)

fun TeamEntity.toDomain(): Team = Team(
    id = TeamId(id ?: error("TeamEntity has no id, likely it hasn't been persisted yet")),
    name = name,
    slug = Slug(slug),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Team.toEntity(): TeamEntity = TeamEntity(
    id = id.value,
    name = name,
    slug = slug.value,
    createdAt = createdAt,
    updatedAt = updatedAt
)