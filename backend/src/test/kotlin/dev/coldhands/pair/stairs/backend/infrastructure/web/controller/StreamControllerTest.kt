package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.aStreamDetails
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.domain.stream.Stream
import dev.coldhands.pair.stairs.backend.domain.stream.StreamDao
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.URI
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
open class StreamControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val streamDao: StreamDao,
    private val developerDao: DeveloperDao,
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
            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/streams"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${stream0Id.value},
                                    "name": "stream-0",
                                    "archived": false
                                },
                                {
                                    "id": ${stream1Id.value},
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
            val stream0Id = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess().id
            val stream1Id = streamDao.create(aStreamDetails("stream-1")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/streams/info"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${stream0Id.value},
                                    "displayName": "stream-0",
                                    "archived": false
                                },
                                {
                                    "id": ${stream1Id.value},
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

            streamDao.findById(stream.id).shouldNotBeNull {
                id shouldBe stream.id
                name shouldBe "stream-0"
                archived shouldBe false
            }
        }
    }

    @Nested
    internal inner class Patch {

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun archived(newArchivedValue: Boolean) {
            val stream = streamDao.create(aStreamDetails("stream-0")).shouldBeSuccess()

            stream.archived shouldBe false

            val result = mockMvc.perform(
                patch("/api/v1/streams/${stream.id.value}")
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

            val updatedStream =
                objectMapper.readValue(result.response.contentAsString, Stream::class.java)

            updatedStream.archived shouldBe newArchivedValue

            streamDao.findById(stream.id).shouldNotBeNull {
                id shouldBe stream.id
                name shouldBe "stream-0"
                archived shouldBe newArchivedValue
            }
        }

        @Test
        fun whenStreamDoesNotExistWithIdThenReturnNotFound() {
            mockMvc.perform(
                patch("/api/v1/streams/${Random.nextInt()}")
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
    internal inner class ReadStreamStats @Autowired constructor(private val combinationEventService: CombinationEventService) {

        @Test
        fun whenStreamDoesNotExistWithIdThenReturnNotFound() {
            mockMvc.perform(get("/api/v1/streams/1/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""))
        }

        @Test
        fun whenStreamExistsButNoPairsHaveHappenedThenReturnAllDevelopers() {
            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId.value))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev0Id.value},
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
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            val dev3Id = developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val streamBId = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

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

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId.value))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev0Id.value},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 3
                                },
                                {
                                  "developer": {
                                    "id": ${dev1Id.value},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 2
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id.value},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id.value},
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
            val dev3Id = developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/streams/{id}/stats", streamAId.value))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            {
                              "developerStats": [
                                {
                                  "developer": {
                                    "id": ${dev1Id.value},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id.value},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id.value},
                                    "displayName": "dev-3",
                                    "archived": false
                                  },
                                  "count": 0
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id.value},
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
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id
            val dev2Id = developerDao.create(aDeveloperDetails("dev-2")).shouldBeSuccess().id
            val dev3Id = developerDao.create(aDeveloperDetails("dev-3")).shouldBeSuccess().id

            val streamAId = streamDao.create(aStreamDetails("stream-a")).shouldBeSuccess().id
            val streamBId = streamDao.create(aStreamDetails("stream-b")).shouldBeSuccess().id

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
                get("/api/v1/streams/{id}/stats", streamAId.value)
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
                                    "id": ${dev0Id.value},
                                    "displayName": "dev-0",
                                    "archived": false
                                  },
                                  "count": 2
                                },
                                {
                                  "developer": {
                                    "id": ${dev1Id.value},
                                    "displayName": "dev-1",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev2Id.value},
                                    "displayName": "dev-2",
                                    "archived": false
                                  },
                                  "count": 1
                                },
                                {
                                  "developer": {
                                    "id": ${dev3Id.value},
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
            mockMvc.perform(builder.invoke(get("/api/v1/streams/1/stats")))
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
                Arguments.of(HttpMethod.GET, "/api/v1/streams/1/stats"),
                Arguments.of(HttpMethod.PATCH, "/api/v1/streams/1"),
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