package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto

fun TeamEntity.toDto(): TeamDto = TeamDto(
    id = id!!,
    name = name,
    slug = slug
)