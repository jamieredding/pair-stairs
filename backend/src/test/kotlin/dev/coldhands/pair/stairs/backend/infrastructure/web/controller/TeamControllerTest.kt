package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.domain.Slug
import dev.coldhands.pair.stairs.backend.domain.team.TeamDao
import dev.coldhands.pair.stairs.backend.domain.team.TeamDetails
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class TeamControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val teamDao: TeamDao,
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
            val createdTeam = teamDao.create(TeamDetails(
                    name = "Team 0",
                    slug = Slug("team-0"),
                )
            ).shouldBeSuccess()

            mockMvc.perform(
                get("/api/v1/teams/team-0")
                    .accept(APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTeam.id.value))
                .andExpect(jsonPath("$.name").value(createdTeam.name))
                .andExpect(jsonPath("$.slug").value(createdTeam.slug.value))

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
            val team0 = teamDao.create(TeamDetails(
                    name = "Team 0",
                    slug = Slug("team-0"),
                )
            ).shouldBeSuccess()

            val team1 = teamDao.create(TeamDetails(
                    name = "Team 1",
                    slug = Slug("team-1"),
                )
            ).shouldBeSuccess()


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
                        "id": ${team0.id.value},
                        "name": ${team0.name.asJsonString()},
                        "slug": ${team0.slug.value.asJsonString()}
                      },
                      {
                        "id": ${team1.id.value},
                        "name": ${team1.name.asJsonString()},
                        "slug": ${team1.slug.value.asJsonString()}
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

            val savedTeam = teamDao.findBySlug(Slug(team.slug)).shouldNotBeNull()

            savedTeam.id.value shouldBe actualId
            savedTeam.name shouldBe "Team 0"
            savedTeam.slug.value shouldBe "team-0"
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
            @MethodSource("dev.coldhands.pair.stairs.backend.infrastructure.web.controller.TeamControllerTest#whenNameIs")
            fun whenNameIs(name: String, errorCode: String) {
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
                    .andExpect(jsonPath("$.errorCode").value(errorCode))
            }

            @ParameterizedTest
            @MethodSource("dev.coldhands.pair.stairs.backend.infrastructure.web.controller.TeamControllerTest#whenSlugIs")
            fun whenSlugIs(slug: String, errorCode: String) {
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
                    .andExpect(jsonPath("$.errorCode").value(errorCode))
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

        @JvmStatic
        fun whenNameIs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("", "INVALID_NAME"),
                Arguments.of("a".repeat(256), "NAME_TOO_LONG"),
            )
        }

        @JvmStatic
        fun whenSlugIs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("", "INVALID_SLUG"),
                Arguments.of("a".repeat(256), "SLUG_TOO_LONG"),
            )
        }

        private fun String?.asJsonString(): String = this?.let { "\"${it}\"" } ?: "null"
    }
}