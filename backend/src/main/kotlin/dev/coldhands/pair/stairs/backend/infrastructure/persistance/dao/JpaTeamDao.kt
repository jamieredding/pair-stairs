package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit

class JpaTeamDao(
    private val teamRepository: TeamRepository,
    private val dateProvider: DateProvider,
    private val precision: TemporalUnit
) : TeamDao {
    override fun findBySlug(slug: Slug): Team? = teamRepository.findBySlug(slug.value)
        ?.toDomain()

    override fun findAll(): List<Team> = teamRepository.findAll().map { it.toDomain() }

    override fun create(teamDetails: TeamDetails): Result<Team, TeamCreateError> {
        findBySlug(teamDetails.slug)?.also { return TeamCreateError.DuplicateSlug(teamDetails.slug).asFailure() }

        teamDetails.slug.also {
            if (it.value.length > 255) return TeamCreateError.SlugTooLong(it).asFailure()
        }
        teamDetails.name.also {
            if (it.length > 255) return TeamCreateError.NameTooLong(it).asFailure()
        }

        return teamRepository.save<TeamEntity>(
            TeamEntity(
                slug = teamDetails.slug.value,
                name = teamDetails.name,
                createdAt = dateProvider.instant().truncatedTo(precision),
                updatedAt = dateProvider.instant().truncatedTo(precision)
            )
        ).toDomain().asSuccess()
    }

}