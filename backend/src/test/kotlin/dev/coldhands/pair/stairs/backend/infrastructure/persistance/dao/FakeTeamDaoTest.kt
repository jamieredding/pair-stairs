package dev.coldhands.pair.stairs.backend.infrastructure.persistance.dao

import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.team.Team
import dev.coldhands.pair.stairs.backend.domain.team.TeamDaoCdc
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class FakeTeamDaoTest : TeamDaoCdc<FakeTeamDao>(){
    override val underTest: FakeTeamDao = FakeTeamDao(dateProvider, precision)

    override fun assertNoTeamExistsWithId(teamId: TeamId) {
        underTest.teamsView[teamId].shouldBeNull()
    }

    override fun assertTeamExistsWithId(teamId: TeamId) {
        underTest.teamsView[teamId].shouldNotBeNull()
    }

    override fun ensureNoTeamsExist() {
        underTest.teamsView.shouldBeEmpty()
    }

    override fun assertNoTeamExistsWithSlug(slug: Slug) {
        underTest.teamsView.values.map { it.slug }.shouldNotContain(slug)
    }

    override fun assertTeamExists(team: Team) {
        underTest.teamsView[team.id].shouldNotBeNull {
            id shouldBe team.id
            name shouldBe team.name
            slug shouldBe team.slug
            createdAt shouldBe team.createdAt.truncatedTo(precision)
            updatedAt shouldBe team.updatedAt.truncatedTo(precision)
        }
    }

    override fun createTeam(teamDetails: TeamDetails): Team = underTest.create(teamDetails).shouldBeSuccess()

}