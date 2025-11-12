package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembership
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipCreateError
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDetails
import dev.coldhands.pair.stairs.backend.infrastructure.mapper.toDomain
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamMembershipRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.TeamRepository
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.repository.UserRepository
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit
import kotlin.jvm.optionals.getOrNull

class JpaTeamMembershipDao(
    private val teamMembershipRepository: TeamMembershipRepository,
    private val teamRepository: TeamRepository,
    private val userRepository: UserRepository,
    private val dateProvider: DateProvider,
    private val precision: TemporalUnit
) : TeamMembershipDao {
    override fun findById(teamMembershipId: TeamMembershipId): TeamMembership? =
        teamMembershipRepository.findById(teamMembershipId.value)
            .getOrNull()
            ?.toDomain()

    override fun create(teamMembershipDetails: TeamMembershipDetails): Result<TeamMembership, TeamMembershipCreateError> {
        teamMembershipDetails.displayName.also {
            if (it.length > 255) return TeamMembershipCreateError.DisplayNameTooLongError(it).asFailure()
        }

        val userEntity = teamMembershipDetails.userId.let { userId ->
            userRepository.findById(userId.value).getOrNull()
                ?: return TeamMembershipCreateError.UserNotFoundError(userId).asFailure()
        }
        val teamEntity = teamMembershipDetails.teamId.let { teamId ->
            teamRepository.findById(teamMembershipDetails.teamId.value).getOrNull()
                ?: return TeamMembershipCreateError.TeamNotFoundError(teamId).asFailure()
        }

        return teamMembershipRepository.save(
            TeamMembershipEntity(
                displayName = teamMembershipDetails.displayName,
                user = userEntity,
                team = teamEntity,
                createdAt = dateProvider.instant().truncatedTo(precision),
                updatedAt = dateProvider.instant().truncatedTo(precision)
            )
        ).toDomain().asSuccess()
    }
}