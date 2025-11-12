package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.user.User
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity

fun User.toEntity(): UserEntity = UserEntity(
    id = id.value,
    oidcSub = oidcSub.value,
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserEntity.toDomain(): User = User(
    id = UserId(id ?: error("UserEntity has no id, likely it hasn't been persisted yet")),
    oidcSub = OidcSub(oidcSub),
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)