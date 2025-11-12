package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.domain.team.TeamCreateError
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit
import java.util.Collections.unmodifiableMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeTeamDao(private val dateProvider: DateProvider,
                  private val precision: TemporalUnit) : TeamDao {
    private val teams = ConcurrentHashMap<TeamId, Team>()
    val teamsView: Map<TeamId, Team> = unmodifiableMap(teams)
    private var nextId = AtomicLong(0L);

    override fun findById(teamId: TeamId): Team? = teams[teamId]

    override fun findBySlug(slug: Slug): Team? = teams.values.find { it.slug == slug }

    override fun findAll(): List<Team> = teams.values.toList()

    override fun create(teamDetails: TeamDetails): Result<Team, TeamCreateError> {
        findBySlug(teamDetails.slug)?.also { return TeamCreateError.DuplicateSlug(teamDetails.slug).asFailure() }

        teamDetails.slug.also {
            if (it.value.length > 255) return TeamCreateError.SlugTooLong(it).asFailure()
        }
        teamDetails.name.also {
            if (it.length > 255) return TeamCreateError.NameTooLong(it).asFailure()
        }

        val teamId = TeamId(nextId.getAndIncrement())
        val team = Team(
            id = teamId,
            name = teamDetails.name,
            slug = teamDetails.slug,
            createdAt = dateProvider.instant().truncatedTo(precision),
            updatedAt = dateProvider.instant().truncatedTo(precision)
        )
        teams[teamId] = team
        return team.asSuccess()
    }
}