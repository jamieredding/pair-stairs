package dev.coldhands.pair.stairs.backend.infrastructure.web.handler

import dev.coldhands.pair.stairs.backend.ParameterizedJsonApprovalTest
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.TeamId
import dev.coldhands.pair.stairs.backend.domain.team.TeamDaoCdc.TestFixtures.someTeamDetails
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.web.testContext
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Path
import org.http4k.lens.contentType
import org.http4k.testing.Approver
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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

    @Nested
    inner class Write {

        private val postTeamLens = Body.auto<PostTeam>().toLens()
        private val withIdLens = Body.auto<WithId>().toLens()

        @Test
        fun `save a team`(approver: Approver) = testContext {
            teamDao.findAll().shouldBeEmpty()

            val response = underTest(
                Request(
                    method = POST,
                    uri = "/api/v1/teams",
                ).with(
                    postTeamLens of PostTeam(
                        name = "Team 0",
                        slug = "team-0",
                    )
                )
            )

            response shouldHaveStatus CREATED
            approver.assertApproved(response)

            val id = TeamId(withIdLens(response).id)

            teamDao.findById(id) shouldNotBeNull {
                this.id shouldBe id
                name shouldBe "Team 0"
                slug shouldBe Slug("team-0")
            }
        }

        @Nested
        inner class BadRequest {

            @Test
            fun `when body is empty`(approver: Approver) = testContext {
                val response = underTest(
                    Request(
                        method = POST,
                        uri = "/api/v1/teams",
                    ).contentType(APPLICATION_JSON)
                        .body("{}")
                )

                response shouldHaveStatus BAD_REQUEST
                approver.assertApproved(response)
            }

            // todo approval test
            @TestFactory
            fun `when name is`() = listOf(
                WhenNameIs("blank", "", "INVALID_NAME"),
                WhenNameIs("too long", "a".repeat(256), "NAME_TOO_LONG"),
            ).map { (description, name, errorCode) ->
                DynamicTest.dynamicTest("when name is $description") {
                    testContext {
                        val response = underTest(
                            Request(
                                method = POST,
                                uri = "/api/v1/teams",
                            ).with(
                                postTeamLens of PostTeam(
                                    name = name,
                                    slug = "team-0",
                                )
                            )
                        )

                        response shouldHaveStatus BAD_REQUEST
//                    approver.assertApproved(response)
                    }
                }
            }

            // todo approval test
            @TestFactory
            fun `when slug is`() = listOf(
                WhenSlugIs("blank", "", "INVALID_SLUG"),
                WhenSlugIs("too long", "a".repeat(256), "SLUG_TOO_LONG"),
            ).map { (description, slug, errorCode) ->
                DynamicTest.dynamicTest("when slug is $description") {
                    testContext {
                        val response = underTest(
                            Request(
                                method = POST,
                                uri = "/api/v1/teams",
                            ).with(
                                postTeamLens of PostTeam(
                                    name = "Team 0",
                                    slug = slug,
                                )
                            )
                        )

                        response shouldHaveStatus BAD_REQUEST
//                    approver.assertApproved(response)
                    }
                }
            }

            @Test
            fun `when slug already exists`(approver: Approver) = testContext {
                teamDao.create(someTeamDetails { copy(slug = Slug("team-0")) }).shouldBeSuccess()

                val response = underTest(
                    Request(
                        method = POST,
                        uri = "/api/v1/teams",
                    ).with(
                        postTeamLens of PostTeam(
                            name = "Team 0",
                            slug = "team-0",
                        )
                    )
                )

                response shouldHaveStatus BAD_REQUEST
                approver.assertApproved(response)
            }


            @ParameterizedTest(name = "slug is {0}")
            @ValueSource(
                strings = ["A", "abc#", "abc "]
            )
            fun `when slug is invalid format`(slug: String, approver: Approver) = testContext {
                val response = underTest(
                    Request(
                        method = POST,
                        uri = "/api/v1/teams",
                    ).with(
                        postTeamLens of PostTeam(
                            name = "Team 0",
                            slug = slug,
                        )
                    )
                )

                response shouldHaveStatus BAD_REQUEST
                approver.assertApproved(response)
            }
        }

    }

    data class PostTeam(
        val name: String,
        val slug: String,
    )

    data class WithId(val id: Long)

    data class WhenNameIs(
        val description: String,
        val name: String,
        val errorCode: String,
    )

    data class WhenSlugIs(
        val description: String,
        val slug: String,
        val errorCode: String,
    )

}