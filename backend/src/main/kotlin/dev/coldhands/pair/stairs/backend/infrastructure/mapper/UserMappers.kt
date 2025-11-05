package dev.coldhands.pair.stairs.backend.infrastructure.mapper

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.User
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity

fun User.toEntity(): UserEntity  = UserEntity(
    id = id.value,
    oidcSub = oidcSub.value,
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserEntity.toDomain(): User = User(
    id = UserId(id!!), // todo http4k-vertical-slice this nullpointer should be exposed as a result return type
    oidcSub = OidcSub(oidcSub),
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt,
)