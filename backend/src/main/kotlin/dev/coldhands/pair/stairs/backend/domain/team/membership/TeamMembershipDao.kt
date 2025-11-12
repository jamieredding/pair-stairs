package dev.coldhands.pair.stairs.backend.domain.team.membership

import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.forkhandles.result4k.Result

interface TeamMembershipDao {
    fun findById(teamMembershipId: TeamMembershipId): TeamMembership?

    fun create(teamMembershipDetails: TeamMembershipDetails): Result<TeamMembership, TeamMembershipCreateError>
}

sealed class TeamMembershipCreateError {
    data class UserNotFoundError(val userId: UserId) : TeamMembershipCreateError()
    data class TeamNotFoundError(val teamId: TeamId) : TeamMembershipCreateError()
    data class DisplayNameTooLongError(val displayName: String) : TeamMembershipCreateError()
}