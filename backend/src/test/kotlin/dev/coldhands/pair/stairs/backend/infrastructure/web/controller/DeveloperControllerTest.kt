package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import io.kotest.matchers.shouldBe
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class DeveloperControllerTest @Autowired constructor(
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
    internal inner class Read {
        @Test
        fun whenNoDevelopersThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/developers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleDevelopersThenReturnThem() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id

            mockMvc.perform(get("/api/v1/developers"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${dev0Id},
                                    "name": "dev-0",
                                    "archived": false
                                },
                                {
                                    "id": ${dev1Id},
                                    "name": "dev-1",
                                    "archived": false
                                }
                            ]
                            """.trimIndent()
                    )
                )
        }
    }

    @Nested
    internal inner class ReadDeveloperInfo {
        @Test
        fun whenNoDevelopersThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/developers/info"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleDevelopersThenReturnThem() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id

            mockMvc.perform(get("/api/v1/developers/info"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                },
                                {
                                    "id": ${dev1Id},
                                    "displayName": "dev-1",
                                    "archived": false
                                }
                            ]
                            """.trimIndent()
                    )
                )
        }
    }

    @Nested
    internal inner class Write {
        @Test
        fun saveADeveloper() {
            val result = mockMvc.perform(
                post("/api/v1/developers")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "name": "dev-0"
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("dev-0"))
                .andReturn()

            val developer =
                objectMapper.readValue(result.response.contentAsString, Developer::class.java)
            val actualId = developer.id

            val savedDeveloper = testEntityManager.find(DeveloperEntity::class.java, actualId.value)

            savedDeveloper.id shouldBe actualId.value
            savedDeveloper.name shouldBe "dev-0"
        }
    }

    @Nested
    internal inner class Patch {

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun archived(newArchivedValue: Boolean) {
            val developer = testEntityManager.persist(DeveloperEntity("dev-0"))

            developer.archived shouldBe false

            val result = mockMvc.perform(
                patch("/api/v1/developers/${developer.id}")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "archived": $newArchivedValue
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isOk())
                .andReturn()

            val updatedDeveloper =
                objectMapper.readValue(result.response.contentAsString, Developer::class.java)

            updatedDeveloper.archived shouldBe newArchivedValue

            val savedDeveloper = testEntityManager.find(DeveloperEntity::class.java, developer.id)

            savedDeveloper.id shouldBe developer.id
            savedDeveloper.name shouldBe "dev-0"
            savedDeveloper.archived shouldBe newArchivedValue
        }

        @Test
        fun whenDeveloperDoesNotExistWithIdThenReturnNotFound() {
            mockMvc.perform(
                patch("/api/v1/developers/${Random.nextInt()}")
                    .contentType(APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "archived": true
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isNotFound())
                .andExpect(content().string(""))
        }
    }

    @Nested
    internal inner class ReadDeveloperStats @Autowired constructor(private val combinationEventService: CombinationEventService) {

        @Test
        fun whenDeveloperDoesNotExistWithIdThenReturnNotFound() {
            mockMvc.perform(get("/api/v1/developers/1/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""))
        }

        @Test
        fun whenDeveloperExistsButNoPairsHaveHappenedThenReturnJustThemselves() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ],
                              "streamStats": []
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun whenDeveloperExistsAndHasPairedThenReturnStatistics() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id
            val dev3Id = testEntityManager.persist(DeveloperEntity("dev-3")).id

            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id
            val streamBId = testEntityManager.persist(StreamEntity("stream-b")).id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6), listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), streamAId),
                    PairStreamByIds(listOf(dev1Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev1Id},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 2
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id},
                                    "displayName": "dev-3",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ],
                              "streamStats": [
                                {
                                  "stream": {
                                    "id": ${streamAId},
                                    "displayName": "stream-a",
                                    "archived": false
                                  },
                                  "count": 3
                                },
                                {
                                  "stream": {
                                    "id": ${streamBId},
                                    "displayName": "stream-b",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun whenDeveloperExistsAndOtherDevelopersAndStreamsButNoEventsHaveHappenedThenSortAlphabetically() {
            val dev3Id = testEntityManager.persist(DeveloperEntity("dev-3")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id

            val streamCId = testEntityManager.persist(StreamEntity("stream-c")).id
            val streamBId = testEntityManager.persist(StreamEntity("stream-b")).id
            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev1Id},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id},
                                    "displayName": "dev-3",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ],
                              "streamStats": [
                                {
                                  "stream": {
                                    "id": ${streamAId},
                                    "displayName": "stream-a",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "stream": {
                                    "id": ${streamBId},
                                    "displayName": "stream-b",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "stream": {
                                    "id": ${streamCId},
                                    "displayName": "stream-c",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun allowsFilteringByDateRange() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id
            val dev3Id = testEntityManager.persist(DeveloperEntity("dev-3")).id

            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id
            val streamBId = testEntityManager.persist(StreamEntity("stream-b")).id

            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 5), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 6), listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), streamAId),
                    PairStreamByIds(listOf(dev1Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 7), listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id), streamAId),
                    PairStreamByIds(listOf(dev2Id), streamBId)
                )
            )
            combinationEventService.saveEvent(
                LocalDate.of(2024, 5, 8), listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id), streamAId),
                    PairStreamByIds(listOf(dev1Id), streamBId)
                )
            )

            mockMvc.perform(
                get("/api/v1/developers/{id}/stats", dev0Id)
                    .queryParam("startDate", "2024-05-06")
                    .queryParam("endDate", "2024-05-07")
            )
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev1Id},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id},
                                    "displayName": "dev-3",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ],
                              "streamStats": [
                                {
                                  "stream": {
                                    "id": ${streamAId},
                                    "displayName": "stream-a",
                                    "archived": false
                                  },
                                  "count": 2
                                },
                                {
                                  "stream": {
                                    "id": ${streamBId},
                                    "displayName": "stream-b",
                                    "archived": false
                                  },
                                  "count": 0
                                }
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dev.coldhands.pair.stairs.backend.infrastructure.web.controller.DeveloperControllerTest#badRequestIfInvalidRange")
        fun badRequestIfInvalidRange(
            testName: String,
            builder: (MockHttpServletRequestBuilder) -> MockHttpServletRequestBuilder
        ) {
            mockMvc.perform(builder.invoke(get("/api/v1/developers/1/stats")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""))
        }

    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): Stream<Arguments?> {
            return Stream.of<Arguments?>(
                Arguments.of(HttpMethod.GET, "/api/v1/developers"),
                Arguments.of(HttpMethod.GET, "/api/v1/developers/info"),
                Arguments.of(HttpMethod.POST, "/api/v1/developers"),
                Arguments.of(HttpMethod.GET, "/api/v1/developers/1/stats"),
                Arguments.of(HttpMethod.PATCH, "/api/v1/developers/1"),
            )
        }

        @JvmStatic
        fun badRequestIfInvalidRange(): Stream<Arguments?> {
            return Stream.of<Arguments?>(
                Arguments.of(
                    "startDate only",
                    { builder: MockHttpServletRequestBuilder ->
                        builder.queryParam(
                            "startDate",
                            "2024-05-06"
                        )
                    }),
                Arguments.of(
                    "endDate only",
                    { builder: MockHttpServletRequestBuilder ->
                        builder.queryParam(
                            "endDate",
                            "2024-05-06"
                        )
                    }),
                Arguments.of(
                    "startDate after endDate",
                    { builder: MockHttpServletRequestBuilder ->
                        builder
                            .queryParam("startDate", "2024-05-07")
                            .queryParam("endDate", "2024-05-06")
                    })
            )
        }

    }
}