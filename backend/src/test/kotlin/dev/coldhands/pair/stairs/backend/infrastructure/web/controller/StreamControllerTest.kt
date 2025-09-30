package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.coldhands.pair.stairs.backend.domain.Stream
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class StreamControllerTest @Autowired constructor(
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
        fun whenNoStreamThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/streams"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleStreamsThenReturnThem() {
            val stream0Id = testEntityManager.persist(StreamEntity("stream-0")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-1")).id

            mockMvc.perform(get("/api/v1/streams"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${stream0Id},
                                    "name": "stream-0",
                                    "archived": false
                                },
                                {
                                    "id": ${stream1Id},
                                    "name": "stream-1",
                                    "archived": false
                                }
                            ]
                            """.trimIndent()
                    )
                )
        }
    }

    @Nested
    internal inner class ReadStreamInfo {
        @Test
        fun whenNoStreamThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/streams/info"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleStreamsThenReturnThem() {
            val stream0Id = testEntityManager.persist(StreamEntity("stream-0")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-1")).id

            mockMvc.perform(get("/api/v1/streams/info"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${stream0Id},
                                    "displayName": "stream-0",
                                    "archived": false
                                },
                                {
                                    "id": ${stream1Id},
                                    "displayName": "stream-1",
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
        fun saveAStream() {
            val result = mockMvc.perform(
                post("/api/v1/streams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                    {
                                      "name": "stream-0"
                                    }
                                    """.trimIndent()
                    )
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("stream-0"))
                .andReturn()

            val stream = objectMapper.readValue(result.response.contentAsString, Stream::class.java)
            val actualId = stream.id

            val savedStream = testEntityManager.find(StreamEntity::class.java, actualId)

            savedStream.id shouldBe actualId
            savedStream.name shouldBe "stream-0"
        }
    }

    @Nested
    internal inner class ReadStreamStats @Autowired constructor(private val combinationEventService: CombinationEventService) {

        @Test
        fun whenStreamDoesNotExistWithIdThenReturnNotFound() {
            mockMvc.perform(get("/api/v1/streams/1/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""))
        }

        @Test
        fun whenStreamExistsButNoPairsHaveHappenedThenReturnAllDevelopers() {
            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
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
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun whenStreamExistsAndHasBeenPairedOnThenReturnStatistics() {
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

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
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
                                  "count": 3
                                },
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
                                }
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun whenStreamExistsAndDevelopersButNoEventsHaveHappenedThenSortAlphabetically() {
            val dev3Id = testEntityManager.persist(DeveloperEntity("dev-3")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id

            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId))
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
                get("/api/v1/streams/{id}/stats", streamAId)
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
                                    "id": ${dev0Id},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 2
                                },
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
                                }
                              ]
                            }
                            """.trimIndent()
                    )
                )
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("dev.coldhands.pair.stairs.backend.infrastructure.web.controller.StreamControllerTest#badRequestIfInvalidRange")
        fun badRequestIfInvalidRange(
            testName: String,
            builder: (MockHttpServletRequestBuilder) -> MockHttpServletRequestBuilder
        ) {
            mockMvc.perform(builder.invoke(get("/api/v1/streams/1/stats"))!!)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""))
        }
    }

    companion object {
        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): java.util.stream.Stream<Arguments?> {
            return java.util.stream.Stream.of<Arguments?>(
                Arguments.of(HttpMethod.GET, "/api/v1/streams"),
                Arguments.of(HttpMethod.GET, "/api/v1/streams/info"),
                Arguments.of(HttpMethod.POST, "/api/v1/streams"),
                Arguments.of(HttpMethod.GET, "/api/v1/streams/1/stats")
            )
        }

        @JvmStatic
        fun badRequestIfInvalidRange(): java.util.stream.Stream<Arguments?> {
            return java.util.stream.Stream.of<Arguments?>(
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