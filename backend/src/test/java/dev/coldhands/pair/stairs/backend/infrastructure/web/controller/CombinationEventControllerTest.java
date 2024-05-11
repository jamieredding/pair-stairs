package dev.coldhands.pair.stairs.backend.infrastructure.web.controller;

import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.CombinationEventEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.DeveloperEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.PairStreamEntity;
import dev.coldhands.pair.stairs.backend.infrastructure.persistance.entity.StreamEntity;
import dev.coldhands.pair.stairs.backend.usecase.CombinationEventService;
import dev.coldhands.pair.stairs.core.domain.pairstream.PairStream;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.coldhands.pair.stairs.backend.infrastructure.web.dto.SaveCombinationEventDto.PairStreamByIds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@TestPropertySource(properties = {
        "app.combinations.event.pageSize=2"
})
class CombinationEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CombinationEventService service;

    @Nested
    class Read {

        @Test
        void whenNoEventsThenReturnEmptyArray() throws Exception {
            mockMvc.perform(get("/api/v1/combinations/event"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        void whenMultipleEventsThenReturnThemInDescendingOrder() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            service.saveEvent(LocalDate.of(2024, 5, 5), List.of(
                    new PairStreamByIds(List.of(dev0Id, dev1Id), stream0Id),
                    new PairStreamByIds(List.of(dev2Id), stream1Id)
            ));
            service.saveEvent(LocalDate.of(2024, 5, 6), List.of(
                    new PairStreamByIds(List.of(dev0Id, dev2Id), stream0Id),
                    new PairStreamByIds(List.of(dev1Id), stream1Id)
            ));

            final Long eventId0 = getEventIdByDate(LocalDate.of(2024, 5, 5)).getAsLong();
            final Long eventId1 = getEventIdByDate(LocalDate.of(2024, 5, 6)).getAsLong();


            mockMvc.perform(get("/api/v1/combinations/event"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                              [
                              {
                                "id": %s,
                                "date": "2024-05-06",
                                "combination": [
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-0",
                                        "id": %s
                                      },
                                      {
                                        "displayName": "dev-2",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-a",
                                      "id": %s
                                    }
                                  },
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-1",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-b",
                                      "id": %s
                                    }
                                  }
                                ]
                              },
                              {
                                "id": %s,
                                "date": "2024-05-05",
                                "combination": [
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-0",
                                        "id": %s
                                      },
                                      {
                                        "displayName": "dev-1",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-a",
                                      "id": %s
                                    }
                                  },
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-2",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-b",
                                      "id": %s
                                    }
                                  }
                                ]
                              }
                            ]
                            """
                            .formatted(eventId1,
                                    dev0Id, dev2Id, stream0Id,
                                    dev1Id, stream1Id,
                                    eventId0,
                                    dev0Id, dev1Id, stream0Id,
                                    dev2Id, stream1Id
                            )));
        }

        @Test
        void whenMultipleEventsThenReturnPage1() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            service.saveEvent(LocalDate.of(2024, 5, 5), List.of(
                    new PairStreamByIds(List.of(dev0Id, dev1Id), stream0Id),
                    new PairStreamByIds(List.of(dev2Id), stream1Id)
            ));
            service.saveEvent(LocalDate.of(2024, 5, 6), List.of(
                    new PairStreamByIds(List.of(dev0Id, dev2Id), stream0Id),
                    new PairStreamByIds(List.of(dev1Id), stream1Id)
            ));
            service.saveEvent(LocalDate.of(2024, 5, 7), List.of(
                    new PairStreamByIds(List.of(dev1Id, dev2Id), stream0Id),
                    new PairStreamByIds(List.of(dev0Id), stream1Id)
            ));

            final long eventId = getEventIdByDate(LocalDate.of(2024, 5, 5)).getAsLong();

            mockMvc.perform(get("/api/v1/combinations/event")
                            .queryParam("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                              [
                              {
                                "id": %s,
                                "date": "2024-05-05",
                                "combination": [
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-0",
                                        "id": %s
                                      },
                                      {
                                        "displayName": "dev-1",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-a",
                                      "id": %s
                                    }
                                  },
                                  {
                                    "developers": [
                                      {
                                        "displayName": "dev-2",
                                        "id": %s
                                      }
                                    ],
                                    "stream": {
                                      "displayName": "stream-b",
                                      "id": %s
                                    }
                                  }
                                ]
                              }
                            ]
                            """
                            .formatted(eventId,
                                    dev0Id, dev1Id, stream0Id,
                                    dev2Id, stream1Id
                            )));
        }
    }

    @Nested
    class Write {

        @Test
        void saveACombinationEvent() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            mockMvc.perform(post("/api/v1/combinations/event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "date": "2024-04-27",
                                      "combination": [
                                        {
                                          "developerIds": [%s, %s],
                                          "streamId": %s
                                        },
                                        {
                                          "developerIds": [%s],
                                          "streamId": %s
                                        }
                                      ]
                                    }""".formatted(dev0Id, dev1Id, stream0Id, dev2Id, stream1Id))
                    )
                    .andExpect(status().isCreated());

            final CombinationEventEntity savedCombinationEvent = testEntityManager.getEntityManager()
                    .createQuery("SELECT c FROM CombinationEventEntity c WHERE c.date = :date", CombinationEventEntity.class)
                    .setParameter("date", LocalDate.parse("2024-04-27"))
                    .getSingleResult();

            assertThat(savedCombinationEvent.getDate()).isEqualTo(LocalDate.of(2024, 4, 27));

            final List<PairStreamEntity> pairs = savedCombinationEvent.getCombination().getPairs();

            final List<PairStream> pairStreams = toSimpleDomain(pairs);

            assertThat(pairStreams).containsExactly(
                    new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
                    new PairStream(Set.of("dev-2"), "stream-b")
            );
        }

@Test
void saveEventWithSoloMemberWhoWasNotSoloPreviously() throws Exception {
    final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
    final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
    final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();
    final Long dev3Id = testEntityManager.persist(new DeveloperEntity("dev-3")).getId();

    final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
    final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

    mockMvc.perform(post("/api/v1/combinations/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "date": "2024-04-27",
                              "combination": [
                                {
                                  "developerIds": [%s, %s],
                                  "streamId": %s
                                },
                                {
                                  "developerIds": [%s, %s],
                                  "streamId": %s
                                }
                              ]
                            }""".formatted(dev0Id, dev1Id, stream0Id, dev2Id, dev3Id, stream1Id))
            )
            .andExpect(status().isCreated());

    mockMvc.perform(post("/api/v1/combinations/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "date": "2024-04-28",
                              "combination": [
                                {
                                  "developerIds": [%s, %s],
                                  "streamId": %s
                                },
                                {
                                  "developerIds": [%s],
                                  "streamId": %s
                                }
                              ]
                            }""".formatted(dev0Id, dev1Id, stream0Id, dev2Id, stream1Id))
            )
            .andExpect(status().isCreated());

    final CombinationEventEntity savedCombinationEvent = testEntityManager.getEntityManager()
            .createQuery("SELECT c FROM CombinationEventEntity c WHERE c.date = :date", CombinationEventEntity.class)
            .setParameter("date", LocalDate.parse("2024-04-28"))
            .getSingleResult();

    assertThat(savedCombinationEvent.getDate()).isEqualTo(LocalDate.of(2024, 4, 28));

    final List<PairStreamEntity> pairs = savedCombinationEvent.getCombination().getPairs();

    final List<PairStream> pairStreams = toSimpleDomain(pairs);

    assertThat(pairStreams).containsExactly(
            new PairStream(Set.of("dev-0", "dev-1"), "stream-a"),
            new PairStream(Set.of("dev-2"), "stream-b")
    );
}
}

    @Nested
    class Delete {

        @Test
        void deleteACombinationEvent() throws Exception {
            final Long dev0Id = testEntityManager.persist(new DeveloperEntity("dev-0")).getId();
            final Long dev1Id = testEntityManager.persist(new DeveloperEntity("dev-1")).getId();
            final Long dev2Id = testEntityManager.persist(new DeveloperEntity("dev-2")).getId();

            final Long stream0Id = testEntityManager.persist(new StreamEntity("stream-a")).getId();
            final Long stream1Id = testEntityManager.persist(new StreamEntity("stream-b")).getId();

            service.saveEvent(LocalDate.of(2024, 5, 5), List.of(
                    new PairStreamByIds(List.of(dev0Id, dev1Id), stream0Id),
                    new PairStreamByIds(List.of(dev2Id), stream1Id)
            ));

            final Long eventId0 = getEventIdByDate(LocalDate.of(2024, 5, 5)).getAsLong();

            mockMvc.perform(delete("/api/v1/combinations/event/{id}", eventId0))
                    .andExpect(status().isNoContent());

            assertThat(getEventIdByDate(LocalDate.of(2024, 5, 5))).isEmpty();
        }

        @Test
        void returnNotFoundIfEventDoesNotExist() throws Exception {
            mockMvc.perform(delete("/api/v1/combinations/event/{id}", 1))
                    .andExpect(status().isNotFound());
        }
    }

    private static List<PairStream> toSimpleDomain(List<PairStreamEntity> pairs) {
        return pairs.stream()
                .map(ps -> {
                    final Set<String> developers = ps.getDevelopers().stream()
                            .map(DeveloperEntity::getName)
                            .collect(Collectors.toSet());
                    return new PairStream(developers, ps.getStream().getName());
                })
                .toList();
    }

    private OptionalLong getEventIdByDate(LocalDate date) {
        return testEntityManager.getEntityManager()
                .createQuery("SELECT c.id FROM CombinationEventEntity c WHERE c.date = :date", Long.class)
                .setParameter("date", date)
                .getResultStream()
                .mapToLong(Long::longValue)
                .findFirst();
    }
}