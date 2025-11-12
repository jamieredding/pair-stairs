package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.DateProvider
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembership
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipCreateError
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDao
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDetails
import dev.coldhands.pair.stairs.backend.domain.user.UserDao
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.time.temporal.TemporalUnit
import java.util.Collections.unmodifiableMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeTeamMembershipDao(
    private val teamDao: TeamDao,
    private val userDao: UserDao,
    private val dateProvider: DateProvider,
    private val precision: TemporalUnit
) : TeamMembershipDao {
    private val teamMemberships = ConcurrentHashMap<TeamMembershipId, TeamMembership>()
    private var nextId = AtomicLong(0L)
    val teamMembershipsView: Map<TeamMembershipId, TeamMembership> = unmodifiableMap(teamMemberships)

    override fun findById(teamMembershipId: TeamMembershipId): TeamMembership? = teamMemberships[teamMembershipId]

    override fun create(teamMembershipDetails: TeamMembershipDetails): Result<TeamMembership, TeamMembershipCreateError> {
        teamMembershipDetails.displayName.also {
            if (it.length > 255) return TeamMembershipCreateError.DisplayNameTooLongError(it).asFailure()
        }

        teamMembershipDetails.teamId.also { teamId ->
            teamDao.findById(teamId) ?: return TeamMembershipCreateError.TeamNotFoundError(teamId).asFailure()
        }
        teamMembershipDetails.userId.also { userId ->
            userDao.findById(userId) ?: return TeamMembershipCreateError.UserNotFoundError(userId).asFailure()
        }

        val teamMembershipId = TeamMembershipId(nextId.getAndIncrement())
        val teamMembership = TeamMembership(
            id = teamMembershipId,
            displayName = teamMembershipDetails.displayName,
            userId = teamMembershipDetails.userId,
            teamId = teamMembershipDetails.teamId,
            createdAt = dateProvider.instant().truncatedTo(precision),
            updatedAt = dateProvider.instant().truncatedTo(precision)
        )
        teamMemberships[teamMembershipId] = teamMembership
        return teamMembership.asSuccess()
    }
}