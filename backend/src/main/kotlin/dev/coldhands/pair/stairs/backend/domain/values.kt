package dev.coldhands.pair.stairs.backend.domain

@JvmInline
value class UserId(val value: Long)
@JvmInline
value class TeamId(val value: Long)
@JvmInline
value class TeamMembershipId(val value: Long)
@JvmInline
value class DeveloperId(val value: Long)

@JvmInline
value class OidcSub(val value: String)
@JvmInline
value class Slug(val value: String)