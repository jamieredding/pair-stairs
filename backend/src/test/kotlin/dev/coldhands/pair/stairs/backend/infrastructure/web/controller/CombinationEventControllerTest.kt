package dev.coldhands.pair.stairs.backend.infrastructure.web.controller

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity
import dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds
import dev.coldhands.pair.stairs.backend.toDeveloperIds
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@TestPropertySource(
    properties = [
        "app.combinations.event.pageSize=2",
    ],
)
open class CombinationEventControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val testEntityManager: TestEntityManager,
    private val service: CombinationEventService,
) {

    @ParameterizedTest
    @MethodSource
    fun whenAnonymousUserThenReturnUnauthorized(httpMethod: HttpMethod, uri: String) {
        mockMvc.perform(
            request(httpMethod, URI.create(uri))
                .with(anonymous()),
        )
            .andExpect(status().isUnauthorized)
    }

    @Nested
    inner class Read {

        @Test
        fun whenNoEventsThenReturnEmptyArray() {
            mockMvc.perform(get("/api/v1/combinations/event"))
                .andExpect(status().isOk)
                .andExpect(content().json("[]"))
        }

        @Test
        fun whenMultipleEventsThenReturnThemInDescendingOrder() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            service.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev2Id).toDeveloperIds(), stream1Id),
                ),
            )
            service.saveEvent(
                LocalDate.of(2024, 5, 6),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev1Id).toDeveloperIds(), stream1Id),
                ),
            )

            val eventId0 = getEventIdByDate(LocalDate.of(2024, 5, 5))
            val eventId1 = getEventIdByDate(LocalDate.of(2024, 5, 6))

            mockMvc.perform(get("/api/v1/combinations/event"))
                .andExpect(status().isOk)
                .andExpect(
                    content().json(
                        """
                        [
                          {
                            "id": $eventId1,
                            "date": "2024-05-06",
                            "combination": [
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-0",
                                    "id": $dev0Id
                                  },
                                  {
                                    "displayName": "dev-2",
                                    "id": $dev2Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-a",
                                  "id": $stream0Id
                                }
                              },
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-1",
                                    "id": $dev1Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-b",
                                  "id": $stream1Id
                                }
                              }
                            ]
                          },
                          {
                            "id": $eventId0,
                            "date": "2024-05-05",
                            "combination": [
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-0",
                                    "id": $dev0Id
                                  },
                                  {
                                    "displayName": "dev-1",
                                    "id": $dev1Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-a",
                                  "id": $stream0Id
                                }
                              },
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-2",
                                    "id": $dev2Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-b",
                                  "id": $stream1Id
                                }
                              }
                            ]
                          }
                        ]
                        """.trimIndent()
                    ),
                )
        }

        @Test
        fun whenMultipleEventsThenReturnPage1() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            service.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev2Id).toDeveloperIds(), stream1Id),
                ),
            )
            service.saveEvent(
                LocalDate.of(2024, 5, 6),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev2Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev1Id).toDeveloperIds(), stream1Id),
                ),
            )
            service.saveEvent(
                LocalDate.of(2024, 5, 7),
                listOf(
                    PairStreamByIds(listOf(dev1Id, dev2Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev0Id).toDeveloperIds(), stream1Id),
                ),
            )

            val eventId = getEventIdByDate(LocalDate.of(2024, 5, 5))

            mockMvc.perform(
                get("/api/v1/combinations/event")
                    .queryParam("page", "1"),
            )
                .andExpect(status().isOk)
                .andExpect(
                    content().json(
                        """
                        [
                          {
                            "id": $eventId,
                            "date": "2024-05-05",
                            "combination": [
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-0",
                                    "id": $dev0Id
                                  },
                                  {
                                    "displayName": "dev-1",
                                    "id": $dev1Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-a",
                                  "id": $stream0Id
                                }
                              },
                              {
                                "developers": [
                                  {
                                    "displayName": "dev-2",
                                    "id": $dev2Id
                                  }
                                ],
                                "stream": {
                                  "displayName": "stream-b",
                                  "id": $stream1Id
                                }
                              }
                            ]
                          }
                        ]
                        """.trimIndent()
                    ),
                )
        }
    }

    @Nested
    inner class Write {

        @Test
        fun saveACombinationEvent() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            mockMvc.perform(
                post("/api/v1/combinations/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "date": "2024-04-27",
                          "combination": [
                            {
                              "developerIds": [$dev0Id, $dev1Id],
                              "streamId": $stream0Id
                            },
                            {
                              "developerIds": [$dev2Id],
                              "streamId": $stream1Id
                            }
                          ]
                        }
                        """.trimIndent()
                    ),
            )
                .andExpect(status().isCreated)

            val savedCombinationEvent =
                testEntityManager.entityManager
                    .createQuery(
                        "SELECT c FROM CombinationEventEntity c WHERE c.date = :date",
                        CombinationEventEntity::class.java,
                    )
                    .setParameter("date", LocalDate.parse("2024-04-27"))
                    .singleResult

            savedCombinationEvent.date shouldBe LocalDate.of(2024, 4, 27)

            val pairs = savedCombinationEvent.combination.pairs
            val pairStreams = pairs.toSimpleDomain()

            pairStreams.shouldContainExactly(
                PairStream(setOf("dev-0", "dev-1"), "stream-a"),
                PairStream(setOf("dev-2"), "stream-b"),
            )
        }

        @Test
        fun saveEventWithSoloMemberWhoWasNotSoloPreviously() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id
            val dev3Id = testEntityManager.persist(DeveloperEntity("dev-3")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            mockMvc.perform(
                post("/api/v1/combinations/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "date": "2024-04-27",
                          "combination": [
                            {
                              "developerIds": [$dev0Id, $dev1Id],
                              "streamId": $stream0Id
                            },
                            {
                              "developerIds": [$dev2Id, $dev3Id],
                              "streamId": $stream1Id
                            }
                          ]
                        }
                        """.trimIndent()
                    ),
            )
                .andExpect(status().isCreated)

            mockMvc.perform(
                post("/api/v1/combinations/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "date": "2024-04-28",
                          "combination": [
                            {
                              "developerIds": [$dev0Id, $dev1Id],
                              "streamId": $stream0Id
                            },
                            {
                              "developerIds": [$dev2Id],
                              "streamId": $stream1Id
                            }
                          ]
                        }
                        """.trimIndent()
                    ),
            )
                .andExpect(status().isCreated)

            val savedCombinationEvent =
                testEntityManager.entityManager
                    .createQuery(
                        "SELECT c FROM CombinationEventEntity c WHERE c.date = :date",
                        CombinationEventEntity::class.java,
                    )
                    .setParameter("date", LocalDate.parse("2024-04-28"))
                    .singleResult

            savedCombinationEvent.date shouldBe LocalDate.of(2024, 4, 28)

            val pairs = savedCombinationEvent.combination.pairs
            val pairStreams = pairs.toSimpleDomain()

            pairStreams.shouldContainExactly(
                PairStream(setOf("dev-0", "dev-1"), "stream-a"),
                PairStream(setOf("dev-2"), "stream-b"),
            )
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun deleteACombinationEvent() {
            val dev0Id = testEntityManager.persist(DeveloperEntity("dev-0")).id
            val dev1Id = testEntityManager.persist(DeveloperEntity("dev-1")).id
            val dev2Id = testEntityManager.persist(DeveloperEntity("dev-2")).id

            val stream0Id = testEntityManager.persist(StreamEntity("stream-a")).id
            val stream1Id = testEntityManager.persist(StreamEntity("stream-b")).id

            service.saveEvent(
                LocalDate.of(2024, 5, 5),
                listOf(
                    PairStreamByIds(listOf(dev0Id, dev1Id).toDeveloperIds(), stream0Id),
                    PairStreamByIds(listOf(dev2Id).toDeveloperIds(), stream1Id),
                ),
            )

            val eventId0 = getEventIdByDate(LocalDate.of(2024, 5, 5))

            mockMvc.perform(delete("/api/v1/combinations/event/{id}", eventId0))
                .andExpect(status().isNoContent)

            getEventIdByDate(LocalDate.of(2024, 5, 5)).shouldBeNull()
        }

        @Test
        fun returnNotFoundIfEventDoesNotExist() {
            mockMvc.perform(delete("/api/v1/combinations/event/{id}", 1))
                .andExpect(status().isNotFound)
        }
    }

    companion object {

        @JvmStatic
        fun whenAnonymousUserThenReturnUnauthorized(): List<Arguments> =
            listOf(
                Arguments.of(HttpMethod.GET, "/api/v1/combinations/event"),
                Arguments.of(HttpMethod.POST, "/api/v1/combinations/event"),
                Arguments.of(HttpMethod.DELETE, "/api/v1/combinations/event/1"),
            )
    }

    private fun List<PairStreamEntity>.toSimpleDomain(): List<PairStream> =
        map { pair -> PairStream(pair.developers.map { it.name }.toSet(), pair.stream.name) }

    private fun getEventIdByDate(date: LocalDate): Long? =
        testEntityManager.entityManager
            .createQuery(
                "SELECT c.id FROM CombinationEventEntity c WHERE c.date = :date",
                java.lang.Long::class.java,
            )
            .setParameter("date", date)
            .resultList
            .firstOrNull()
            ?.toLong()
}