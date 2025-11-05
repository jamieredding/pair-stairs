package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamMembershipEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.UserEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamMembershipDto
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
            val createdTeam = testEntityManager.persist(
                TeamEntity(
                    name = "Team 0",
                    slug = "team-0"
                )
            )
            val createdUser = testEntityManager.persist(
                UserEntity(
                    oidcSub = "some-oidc-sub",
                    displayName = "some-display-name",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )

            val result = mockMvc.perform(
                post("/api/v1/teams/${createdTeam.slug}/memberships")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "userId": ${createdUser.id}
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(createdUser.id))
                .andExpect(jsonPath("$.displayName").value("some-display-name"))
                .andReturn()

            val teamMembership = objectMapper.readValue<TeamMembershipDto>(result.response.contentAsString)
            val actualId = teamMembership.id

            val savedTeamMembership = testEntityManager.find(TeamMembershipEntity::class.java, actualId)

            savedTeamMembership.id shouldBe actualId
            savedTeamMembership.displayName shouldBe "some-display-name"
            savedTeamMembership.user should {
                it.id == createdUser.id
                it.displayName shouldBe createdUser.displayName
                it.oidcSub shouldBe createdUser.oidcSub
            }
            savedTeamMembership.team should {
                it.id == createdTeam.id
                it.name shouldBe createdTeam.name
                it.slug shouldBe createdTeam.slug
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