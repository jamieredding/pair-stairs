package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.coldhands.pair.stairs.backend.aDeveloperDetails
import dev.coldhands.pair.stairs.backend.domain.developer.Developer
import dev.coldhands.pair.stairs.backend.domain.developer.DeveloperDao
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
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
    private val testEntityManager: TestEntityManager, //todo stop using entity manager once streams are migrated from jpa
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
        fun whenNoDevelopersThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/developers"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleDevelopersThenReturnThem() {
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/developers"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${dev0Id.value},
                                    "name": "dev-0",
                                    "archived": false
                                },
                                {
                                    "id": ${dev1Id.value},
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
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id
            val dev1Id = developerDao.create(aDeveloperDetails("dev-1")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/developers/info"))
                .andExpect(status().isOk())
                .andExpect(
                    content().json(
                        """
                            [
                                {
                                    "id": ${dev0Id.value},
                                    "displayName": "dev-0",
                                    "archived": false
                                },
                                {
                                    "id": ${dev1Id.value},
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

            developerDao.findById(developer.id).shouldNotBeNull {
                id shouldBe developer.id
                name shouldBe "dev-0"
                archived shouldBe false
            }
        }
    }

    @Nested
    internal inner class Patch {

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun archived(newArchivedValue: Boolean) {
            val developer = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess()

            developer.archived shouldBe false

            val result = mockMvc.perform(
                patch("/api/v1/developers/${developer.id.value}")
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

            developerDao.findById(developer.id).shouldNotBeNull {
                id shouldBe developer.id
                name shouldBe "dev-0"
                archived shouldBe newArchivedValue
            }
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
            val dev0Id = developerDao.create(aDeveloperDetails("dev-0")).shouldBeSuccess().id

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id.value))
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
                              ],
                              "streamStats": []
                            }
                            """.trimIndent()
                    )
                )
        }

        @Test
        fun whenDeveloperExistsAndHasPairedThenReturnStatistics() {
            val developers = listOf(
                aDeveloperDetails("dev-0"),
                aDeveloperDetails("dev-1"),
                aDeveloperDetails("dev-2"),
                aDeveloperDetails("dev-3")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }

            val dev0Id = developerIds[0]
            val dev1Id = developerIds[1]
            val dev2Id = developerIds[2]
            val dev3Id = developerIds[3]

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

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id.value))
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
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id.value},
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
            val developers = listOf(
                aDeveloperDetails("dev-3"),
                aDeveloperDetails("dev-2"),
                aDeveloperDetails("dev-1"),
                aDeveloperDetails("dev-0"),
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }

            val dev3Id = developerIds[0]
            val dev2Id = developerIds[1]
            val dev1Id = developerIds[2]
            val dev0Id = developerIds[3]

            val streamCId = testEntityManager.persist(StreamEntity("stream-c")).id
            val streamBId = testEntityManager.persist(StreamEntity("stream-b")).id
            val streamAId = testEntityManager.persist(StreamEntity("stream-a")).id

            mockMvc.perform(get("/api/v1/developers/{id}/stats", dev0Id.value))
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
            val developers = listOf(
                aDeveloperDetails("dev-0"),
                aDeveloperDetails("dev-1"),
                aDeveloperDetails("dev-2"),
                aDeveloperDetails("dev-3")
            ).map { developerDao.create(it).shouldBeSuccess() }
            val developerIds = developers.map { it.id }

            val dev0Id = developerIds[0]
            val dev1Id = developerIds[1]
            val dev2Id = developerIds[2]
            val dev3Id = developerIds[3]

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
                get("/api/v1/developers/{id}/stats", dev0Id.value)
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
                                },
                                {
                                  "developer": {
                                    "id": ${dev0Id.value},
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