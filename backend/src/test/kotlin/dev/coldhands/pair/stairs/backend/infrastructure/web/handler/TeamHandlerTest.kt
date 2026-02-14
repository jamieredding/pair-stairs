package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Path
import org.http4k.testing.Approver
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ParameterizedJsonApprovalTest::class)
class TeamHandlerTest {
    private val pathTeamSlugLens = Path.of("slug")

    @Test
    @Disabled
    fun `when anonymous user then return unauthorized`() {

    }

    @Nested
    inner class ReadTeam {

        @Test
        fun `when team does not exist then return not found`(approver: Approver) = testContext {
            val slug = Slug("team-0")

            teamDao.findBySlug(slug).shouldBeNull()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/teams/{slug}",
                ).with(pathTeamSlugLens of slug.value)
            )

            response shouldHaveStatus NOT_FOUND
            approver.assertApproved(response)
        }

        @Test
        fun `when team does exist then return team data`(approver: Approver) = testContext {
            val createdTeam = teamDao.create(
                TeamDetails(
                    name = "Team 0",
                    slug = Slug("team-0"),
                )
            ).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/teams/{slug}",
                ).with(pathTeamSlugLens of createdTeam.slug.value)
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }
    }

    @Nested
    inner class ReadTeams {

        @Test
        fun `when no teams exist return empty array`(approver: Approver) = testContext {
            teamDao.findAll().shouldBeEmpty()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/teams",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

        @Test
        fun `when multiple teams exist then return them`(approver: Approver) = testContext {
            teamDao.create(
                TeamDetails(
                    name = "Team 0",
                    slug = Slug("team-0"),
                )
            ).shouldBeSuccess()
            teamDao.create(
                TeamDetails(
                    name = "Team 1",
                    slug = Slug("team-1"),
                )
            ).shouldBeSuccess()

            val response = underTest(
                Request(
                    method = GET,
                    uri = "/api/v1/teams",
                )
            )

            response shouldHaveStatus OK
            approver.assertApproved(response)
        }

    }

}