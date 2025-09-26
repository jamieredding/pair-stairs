package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import io.kotest.matchers.date.shouldBeCloseTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI
import java.time.Instant
import java.util.stream.Stream
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class TeamControllerTest @Autowired constructor(
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
    inner class ReadTeam {

        @Test
        fun whenTeamDoesNotExistReturnNotFound() {
            mockMvc.perform(
                get("/api/v1/teams/team-0")
                    .accept(APPLICATION_JSON)
            )
                .andExpect(status().isNotFound())
        }

        @Test
        fun whenTeamDoesExistReturnTeamData() {
            val createdTeam = testEntityManager.persist(
                TeamEntity(
                    name = "Team 0",
                    slug = "team-0"
                )
            )

            mockMvc.perform(
                get("/api/v1/teams/team-0")
                    .accept(APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTeam.id))
                .andExpect(jsonPath("$.name").value(createdTeam.name))
                .andExpect(jsonPath("$.slug").value(createdTeam.slug))

        }
    }

    @Nested
    inner class ReadTeams {

        @Test
        fun whenNoTeamsExistReturnEmptyArray() {
            mockMvc.perform(
                get("/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleTeamsExistThenReturnThem() {
            val team0 = testEntityManager.persist(
                TeamEntity(
                    name = "Team 0",
                    slug = "team-0"
                )
            )
            val team1 = testEntityManager.persist(
                TeamEntity(
                    name = "Team 1",
                    slug = "team-1"
                )
            )


            mockMvc.perform(
                get("/api/v1/teams")
                    .accept(APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                    [
                      {
                        "id": ${team0.id},
                        "name": ${team0.name.asJsonString()},
                        "slug": ${team0.slug.asJsonString()}
                      },
                      {
                        "id": ${team1.id},
                        "name": ${team1.name.asJsonString()},
                        "slug": ${team1.slug.asJsonString()}
                      }
                    ]
                """.trimIndent()
                    )
                )
        }
    }


    @Nested
    inner class Write {

        @Test
        fun saveATeam() {
            val result = mockMvc.perform(
                post("/api/v1/teams")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "name": "Team 0",
                                      "slug": "team-0"
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Team 0"))
                .andExpect(jsonPath("$.slug").value("team-0"))
                .andReturn()

            val team = objectMapper.readValue<TeamDto>(result.response.contentAsString)
            val actualId = team.id

            val savedTeam = testEntityManager.find(TeamEntity::class.java, actualId)

            savedTeam.id shouldBe actualId
            savedTeam.name shouldBe "Team 0"
            savedTeam.slug shouldBe "team-0"
            savedTeam.createdAt.shouldNotBeNull {
                shouldBeCloseTo(Instant.now(), 1.seconds)
            }
            savedTeam.updatedAt.shouldNotBeNull {
                shouldBeCloseTo(Instant.now(), 1.seconds)
            }
        }

        @Nested
        inner class BadRequest {
            @Test
            fun whenBodyIsEmpty() {
                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content("{}")
                )
                    .andExpect(status().isBadRequest())
                    .andExpect { jsonPath("$.errorCode").value("INVALID_REQUEST_BODY") }
            }

            @ParameterizedTest
            @EmptySource
            fun whenNameIs(name: String) {
                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content(
                            """
                                        {
                                          "name": ${name.asJsonString()},
                                          "slug": "team-0"
                                        }
                            """.trimIndent()
                        )
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_NAME"))
            }

            @ParameterizedTest
            @EmptySource
            fun whenSlugIs(slug: String) {
                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content(
                            """
                                        {
                                          "name": "Team 0",
                                          "slug": ${slug.asJsonString()}
                                        }
                            """.trimIndent()
                        )
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SLUG"))
            }

            @Test
            fun whenSlugAlreadyExists() {
                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content(
                            """
                                        {
                                          "name": "Team 0",
                                          "slug": "team-0"
                                        }
                                        """.trimIndent()
                        )
                )
                    .andExpect(status().isCreated())

                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content(
                            """
                                        {
                                          "name": "Team 1",
                                          "slug": "team-0"
                                        }
                                        """.trimIndent()
                        )
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_SLUG"))
            }

            @ParameterizedTest
            @ValueSource(
                strings = ["A", "abc#", "abc "]
            )
            fun whenSlugIsInvalidFormat(slug: String?) {
                mockMvc.perform(
                    post("/api/v1/teams")
                        .contentType(APPLICATION_JSON)
                        .content(
                            """
                                        {
                                          "name": "Team 0",
                                          "slug": ${slug.asJsonString()}
                                        }
                            """.trimIndent()
                        )
                )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SLUG"))
            }
        }
    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/teams/team-0"),
                Arguments.of(HttpMethod.GET, "/api/v1/teams"),
                Arguments.of(HttpMethod.POST, "/api/v1/teams"),
            )
        }

        private fun String?.asJsonString(): String = this?.let { "\"${it}\"" } ?: "null"
    }
}