package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.OidcSub
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.UserId
import java.util.*
import kotlin.random.Random

val alphanumeric = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun anOidcSub() = OidcSub(UUID.randomUUID().toString())

fun aUserId() = UserId(Random.nextLong())

fun aSlug() = Slug((1..15).map { alphanumeric.random() }.joinToString(""))