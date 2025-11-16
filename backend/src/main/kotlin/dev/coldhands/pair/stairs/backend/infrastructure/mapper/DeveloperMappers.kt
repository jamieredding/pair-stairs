package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.DeveloperId
import dev.coldhands.pair.stairs.backend.domain.DeveloperInfo
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity

fun DeveloperEntity.toDomain(): Developer =
    Developer(
        id = DeveloperId(id ?: error("DeveloperEntity has no id, likely it hasn't been persisted yet")),
        name = name,
        archived = archived
    )

fun Developer.toInfo(): DeveloperInfo = DeveloperInfo(
    id.value,
    name,
    archived
)