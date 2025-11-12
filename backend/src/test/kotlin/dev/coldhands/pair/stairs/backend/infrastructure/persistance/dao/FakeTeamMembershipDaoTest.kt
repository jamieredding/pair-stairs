package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.TeamMembershipId
import dev.coldhands.pair.stairs.backend.domain.UserId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembership
import dev.coldhands.pair.stairs.backend.domain.team.membership.TeamMembershipDaoCdc
import dev.coldhands.pair.stairs.backend.domain.user.User
import dev.coldhands.pair.stairs.backend.domain.user.UserDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeTeamMembershipDaoTest : TeamMembershipDaoCdc<FakeTeamMembershipDao>() {
    val teamDao: FakeTeamDao = FakeTeamDao(dateProvider, precision)
    val userDao: FakeUserDao = FakeUserDao(dateProvider, precision)
    override val underTest: FakeTeamMembershipDao = FakeTeamMembershipDao(teamDao, userDao, dateProvider, precision)

    override fun assertNoTeamExistsWithId(teamId: TeamId) {
        teamDao.teamsView[teamId].shouldBeNull()
    }

    override fun assertNoUserExistsWithId(userId: UserId) {
        userDao.usersView[userId].shouldBeNull()
    }

    override fun assertNoTeamMembershipExistsForId(teamMembershipId: TeamMembershipId) {
        underTest.teamMembershipsView[teamMembershipId].shouldBeNull()
    }

    override fun assertNoTeamMembershipExistsForUserId(userId: UserId) {
        underTest.teamMembershipsView.values.find { it.userId == userId }.shouldBeNull()
    }

    override fun assertNoTeamMembershipExistsForTeamId(teamId: TeamId) {
        underTest.teamMembershipsView.values.find { it.teamId == teamId }.shouldBeNull()
    }

    override fun assertTeamMembershipExistsWithId(teamMembershipId: TeamMembershipId) {
        underTest.teamMembershipsView[teamMembershipId].shouldNotBeNull()
    }

    override fun assertTeamMembershipExists(teamMembership: TeamMembership) {
        underTest.teamMembershipsView[teamMembership.id].shouldNotBeNull {
            id shouldBe teamMembership.id
            displayName shouldBe teamMembership.displayName
            userId shouldBe teamMembership.userId
            teamId shouldBe teamMembership.teamId
            createdAt shouldBe teamMembership.createdAt.truncatedTo(precision)
            updatedAt shouldBe teamMembership.updatedAt.truncatedTo(precision)
        }
    }

    override fun createTeam(teamDetails: TeamDetails): Team = teamDao.create(teamDetails).shouldBeSuccess()

    override fun createUser(userDetails: UserDetails): User = userDao.create(userDetails).shouldBeSuccess()
}