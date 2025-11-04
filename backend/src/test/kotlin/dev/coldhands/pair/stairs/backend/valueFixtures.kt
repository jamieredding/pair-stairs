package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.UserId
import java.util.*
import kotlin.random.Random

fun anOidcSub() = OidcSub(UUID.randomUUID().toString())

fun aUserId() = UserId(Random.nextLong())