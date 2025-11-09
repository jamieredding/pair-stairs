package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.domain.*
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamMembershipDto
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.date.shouldBeCloseTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Instant
import java.util.stream.Stream
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class TeamMembershipControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val testEntityManager: TestEntityManager,
    private val objectMapper: ObjectMapper,
    private val teamDao: TeamDao,
    private val userDao: UserDao,
) {

    @ParameterizedTest
    @MethodSource
    fun whenAnonymousUserThenReturnUnauthorized(httpMethod: HttpMethod, uri: String) {
        mockMvc.perform(
            request(httpMethod, URI.create(uri))
                .with(anonymous())
        )
            .andExpect(status().isUnauthorized())
    }

    @Nested
    inner class Write {

        @Test
        fun saveATeamMembership() {
            val createdTeam = teamDao.create(
                TeamDetails(
                    name = "Team 0",
                    slug = Slug("team-0"),
                )
            ).shouldBeSuccess()
            val createdUser = userDao.create(
                UserDetails(
                    oidcSub = OidcSub("some-oidc-sub"),
                    displayName = "some-display-name",
                )
            ).shouldBeSuccess()

            val result = mockMvc.perform(
                post("/api/v1/teams/${createdTeam.slug.value}/memberships")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "userId": ${createdUser.id.value}
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(createdUser.id.value))
                .andExpect(jsonPath("$.displayName").value("some-display-name"))
                .andReturn()

            val teamMembership = objectMapper.readValue<TeamMembershipDto>(result.response.contentAsString)
            val actualId = teamMembership.id

            val savedTeamMembership = testEntityManager.find(TeamMembershipEntity::class.java, actualId)

            savedTeamMembership.id shouldBe actualId
            savedTeamMembership.displayName shouldBe "some-display-name"
            savedTeamMembership.user should {
                it.id shouldBe createdUser.id.value
                it.displayName shouldBe createdUser.displayName
                it.oidcSub shouldBe createdUser.oidcSub.value
            }
            savedTeamMembership.team should {
                it.id shouldBe createdTeam.id.value
                it.name shouldBe createdTeam.name
                it.slug shouldBe createdTeam.slug.value
            }

            savedTeamMembership.createdAt.shouldNotBeNull {
                shouldBeCloseTo(Instant.now(), 1.seconds)
            }
            savedTeamMembership.updatedAt.shouldNotBeNull {
                shouldBeCloseTo(Instant.now(), 1.seconds)
            }
        }

        /*
        todo
            - team doesn't exist with slug
            - userid doesn't exist
            - membership already exists (200) but don't create new
         */
    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(HttpMethod.POST, "/api/v1/teams/team-0/memberships"),
            )
        }

        private fun String?.asJsonString(): String = this?.let { "\"${it}\"" } ?: "null"
    }
}