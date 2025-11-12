package dev.coldhands.pair.stairs.backend.domain.team

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.forkhandles.result4k.Result

interface TeamDao {
    fun findBySlug(slug: Slug): Team?
    fun findAll(): List<Team>

    fun create(teamDetails: TeamDetails): Result<Team, TeamCreateError>
}

sealed class TeamCreateError {
    data class DuplicateSlug(val slug: Slug) : TeamCreateError()
    data class SlugTooLong(val slug: Slug) : TeamCreateError()
    data class NameTooLong(val name: String) : TeamCreateError()
}