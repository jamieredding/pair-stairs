package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.TeamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.TeamDto
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Instant
import java.util.stream.Stream
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
    internal inner class Write {

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

            assertThat(savedTeam.id).isEqualTo(actualId)
            assertThat(savedTeam.name).isEqualTo("Team 0")
            assertThat(savedTeam.slug).isEqualTo("team-0")
            assertThat(savedTeam.createdAt)
                .isCloseTo(Instant.now(), within(1.seconds.toJavaDuration()))
            assertThat(savedTeam.updatedAt)
                .isCloseTo(Instant.now(), within(1.seconds.toJavaDuration()))
        }

        @Nested
        internal inner class BadRequest {
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
                //                Arguments.of(HttpMethod.GET, "/api/v1/teams"),
                //                Arguments.of(HttpMethod.GET, "/api/v1/developers/info"),
                Arguments.of(HttpMethod.POST, "/api/v1/teams",
                ) //                Arguments.of(HttpMethod.GET, "/api/v1/developers/1/stats")
            )
        }

        private fun String?.asJsonString(): String = this?.let { "\"${it}\"" } ?: "null"
    }
}