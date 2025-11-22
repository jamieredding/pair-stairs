package dev.coldhands.pair.stairs.backend

import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDetails
import java.util.*
import kotlin.random.Random

val alphanumeric = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun anOidcSub() = OidcSub(UUID.randomUUID().toString())

fun aUserId() = UserId(Random.nextLong())
fun aTeamId() = TeamId(Random.nextLong())
fun aTeamMembershipId() = TeamMembershipId(Random.nextLong())
fun aDeveloperId() = DeveloperId(Random.nextLong())

fun aSlug() = Slug((1..15).map { alphanumeric.random() }.joinToString(""))

fun List<Long?>.toDeveloperIds(): List<DeveloperId> = filterNotNull().map { DeveloperId(it) }
fun DeveloperId.asString(): String = value.toString()
fun aDeveloperDetails(name: String) = DeveloperDetails(name = name, archived = false)